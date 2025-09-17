import sys
import os

from temporalio.workflow import payload_converter
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pytest
import asyncio
from temporalio import activity
from temporalio.testing import WorkflowEnvironment
from temporalio.worker import Worker
from temporalio.exceptions import ApplicationError
from temporalio.converter import DataConverter, DefaultPayloadConverter, JSONPlainPayloadConverter, CompositePayloadConverter
from temporalio.client import WorkflowFailureError
from temporalio.common import SearchAttributeKey
from account_transfer_workflow_scenarios import AccountTransferWorkflowScenarios
from activities import AccountTransferActivities
from shared_objects import TransferInput, TransferOutput, TransferStatus, DepositResponse

# Mock activities for scenarios testing
@activity.defn(name="validate")
async def mock_validate(input_data):
    print("validate called")
    return "SUCCESS"

@activity.defn(name="withdraw")
async def mock_withdraw(idempotency_key, amount, workflow_type):
    print("withdraw called")
    return "SUCCESS"

@activity.defn(name="deposit")
async def mock_deposit(idempotency_key, amount, workflow_type):
    print("deposit called")
    if workflow_type == "AccountTransferWorkflowInvalidAccount":
        raise ApplicationError("Deposit failed - invalid account", type="InvalidAccount", non_retryable=True)
    return DepositResponse('test-charge-id')

@activity.defn(name="sendNotification")
async def mock_send_notification(input_data):
    return "SUCCESS"

@activity.defn(name="undoWithdraw")
async def mock_undo_withdraw(amount):
    return True

@pytest.mark.asyncio
async def test_normal_scenario_execution():
    """Test normal workflow execution without any special scenarios"""
    async with await WorkflowEnvironment.start_time_skipping() as env:
        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[mock_validate, mock_withdraw, mock_deposit, mock_send_notification, mock_undo_withdraw],
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )

            # Execute workflow with normal workflow type
            result = await env.client.execute_workflow(
                "AccountTransferWorkflowScenarios",
                transfer_input,
                id="test-normal-scenario",
                task_queue="test-queue"
            )

            # Convert dict result back to object for testing
            if isinstance(result, dict):
                result = TransferOutput(result["depositResponse"]["chargeId"])

            assert isinstance(result, TransferOutput)
            assert result.depositResponse == "test-charge-id"

@pytest.mark.asyncio
async def test_human_in_loop_approval_success():
    """Test workflow that requires human approval - approval granted"""
    async with await WorkflowEnvironment.start_time_skipping() as env:
        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[mock_validate, mock_withdraw, mock_deposit, mock_send_notification, mock_undo_withdraw],
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )
            handle = await env.client.start_workflow(
                AccountTransferWorkflowScenarios.NEEDS_APPROVAL,
                transfer_input,
                id="test-approval-success",
                task_queue="test-queue",
            )

            # Wait for workflow to reach approval stage
            await env.sleep(5)

            # Query status to verify it's waiting for approval
            status = await handle.query("transferStatus")
            assert status['progressPercentage'] >= 0
            assert status['transferState'] == "waiting"

            # Send approval signal
            await handle.signal("approveTransfer")

            # Wait for completion
            result = await handle.result()
            # assert isinstance(result, TransferOutput)
            assert result['depositResponse']['chargeId'] == 'test-charge-id'

@pytest.mark.asyncio
async def test_human_in_loop_approval_timeout():
    """Test workflow that requires human approval - timeout occurs"""
    async with await WorkflowEnvironment.start_time_skipping() as env:
        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[mock_validate, mock_withdraw, mock_deposit, mock_send_notification, mock_undo_withdraw]
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )

            handle = await env.client.start_workflow(
                AccountTransferWorkflowScenarios.NEEDS_APPROVAL,
                transfer_input,
                id="test-approval-timeout",
                task_queue="test-queue",
            )

            # Skip ahead to trigger timeout
            await env.sleep(35)  # Approval timeout is 30 seconds

            with pytest.raises(WorkflowFailureError) as err:
                await handle.result()

            assert "Approval not received within 30 seconds" in str(err.value.cause.message)
            assert err.value.cause.type == "ApprovalTimeout"

# TODO: Ask the team on how to actually test this appropriately
@pytest.mark.asyncio
async def test_bug_scenario_with_recovery():
    """Test workflow with simulated bug that should fail"""
    async with await WorkflowEnvironment.start_time_skipping() as env:
        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[mock_validate, mock_withdraw, mock_deposit, mock_send_notification, mock_undo_withdraw]
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )
            handle = await env.client.start_workflow(
                AccountTransferWorkflowScenarios.BUG,
                transfer_input,
                id="test-bug-scenario",
                task_queue="test-queue",
            )

            # await env.sleep(4.1)
            # status: TransferStatus = await handle.query("transferStatus")

            # assert status['progressPercentage'] >= 50

            # assert status['transferState'] in {"running", ""}
            # desc = await handle.describe()
            # print(desc)
            # print(desc.pending_workflow_task.state)
            # handle.cancel
            try:
                await asyncio.wait_for(handle.result(), timeout=10)
                pytest.fail("Workflow unexpectedly completed")
            except asyncio.TimeoutError:
                pass

            desc = await handle.describe()
            assert not desc.close_time  # still open

            await handle.cancel()
            # assert "Simulated bug - fix me!" in str(exc_info.value)
            # assert "Simulated bug - fix me!" in str(handle.run_id)

@pytest.mark.asyncio
async def test_advanced_visibility_scenario():
    """Test workflow with advanced visibility features"""
    async with await WorkflowEnvironment.start_local(search_attributes=[AccountTransferWorkflowScenarios.WORKFLOW_STEP]) as env:
        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[mock_validate, mock_withdraw, mock_deposit, mock_send_notification, mock_undo_withdraw]
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )

            handle = await env.client.start_workflow(
                AccountTransferWorkflowScenarios.ADVANCED_VISIBILITY,
                transfer_input,
                id="test-advanced-visibility",
                task_queue="test-queue",
            )

            await env.sleep(10)
            desc = await handle.describe()
            result = await handle.result()
            assert desc.search_attributes.get("Step") == ["Send notification"]
            assert isinstance(desc.search_attributes, dict )

            assert isinstance(result, dict)

@pytest.mark.asyncio
async def test_deposit_failure_with_rollback():
    """Test workflow handles deposit failure with proper rollback"""
    async with await WorkflowEnvironment.start_time_skipping() as env:
        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[mock_validate, mock_withdraw, mock_deposit, mock_send_notification, mock_undo_withdraw]
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )

            with pytest.raises(WorkflowFailureError) as exc_info:
                await env.client.execute_workflow(
                    "AccountTransferWorkflowInvalidAccount",
                    transfer_input,
                    id="test-deposit-failure",
                    task_queue="test-queue",
                    # workflow_type="AccountTransferWorkflowInvalidAccount"
                )

            assert exc_info.value.cause.type == "DepositFailed"

@pytest.mark.asyncio
async def test_workflow_query_during_execution():
    """Test querying workflow status during execution"""
    async with await WorkflowEnvironment.start_time_skipping() as env:
        @activity.defn(name="validate")
        async def slow_validate(input_data):
            await asyncio.sleep(1)
            return "SUCCESS"

        @activity.defn(name="withdraw")
        async def slow_withdraw(idempotency_key, amount, workflow_type):
            await asyncio.sleep(1)
            return "SUCCESS"

        @activity.defn(name="deposit")
        async def slow_deposit(idempotency_key, amount, workflow_type):
            await asyncio.sleep(1)
            return DepositResponse('test-charge-id')

        @activity.defn(name="sendNotification")
        async def slow_notification(input_data):
            await asyncio.sleep(1)
            return "SUCCESS"

        worker = Worker(
            client=env.client,
            task_queue="test-queue",
            workflows=[AccountTransferWorkflowScenarios],
            activities=[slow_validate, slow_withdraw, slow_deposit, slow_notification]
        )

        async with worker:
            transfer_input = TransferInput(
                amount=100,
                fromAccount="account1",
                toAccount="account2"
            )

            handle = await env.client.start_workflow(
                "AccountTransferWorkflowScenarios",
                transfer_input,
                id="test-query-during-execution",
                task_queue="test-queue",
            )

            # Query status during execution
            await asyncio.sleep(0.05)
            status = await handle.query("transferStatus")
            assert isinstance(status, dict)
            assert status['progressPercentage'] >= 0

            # Wait for completion
            result = await handle.result()
            assert isinstance(result, dict)

            # Query final status
            final_status = await handle.query("transferStatus")
            assert final_status['progressPercentage'] == 100
            assert final_status['transferState'] == "finished"

@pytest.mark.asyncio
async def test_workflow_initialization():
    """Test workflow scenarios initialization and default values"""
    workflow = AccountTransferWorkflowScenarios()

    assert workflow.progress == 0
    assert workflow.transferState == ""
    assert isinstance(workflow.depositResponse, DepositResponse)
    assert workflow.depositResponse.get_chargeId() == ""
    assert workflow.start_to_close_timeout.total_seconds() == 5
    assert workflow.retry_policy is not None
    assert workflow.approvalTime == 30
    assert workflow.approved == False
