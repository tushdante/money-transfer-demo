import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pytest
from temporalio.testing import ActivityEnvironment
from activities import AccountTransferActivities
from shared_objects import TransferInput, DepositResponse
from temporalio.exceptions import ApplicationError

@pytest.mark.asyncio
async def test_validate():
    activity_env = ActivityEnvironment()
    input_data = TransferInput(amount=100, fromAccount="acc1", toAccount="acc2")

    result = await activity_env.run(
        AccountTransferActivities().validate,
        input_data
    )

    assert result == "SUCCESS"

@pytest.mark.asyncio
async def test_withdraw_success():
    activity_env = ActivityEnvironment()
    result = await activity_env.run(
        AccountTransferActivities().withdraw,
        "test-key",
        100.0,
        "TestWorkflow"
    )

    assert result == "SUCCESS"

@pytest.mark.asyncio
async def test_withdraw_api_downtime():
    activity_env = ActivityEnvironment()
    with pytest.raises(ApplicationError) as exc_info:
        await activity_env.run(
            AccountTransferActivities().withdraw,
            "test-key",
            100.0,
            AccountTransferActivities.API_DOWNTIME
        )

    assert "Withdraw activity failed, API unavailable" in str(exc_info.value)

@pytest.mark.asyncio
async def test_deposit_success():
    activity_env = ActivityEnvironment()
    result = await activity_env.run(
        AccountTransferActivities().deposit,
        "test-key",
        100.0,
        "TestWorkflow"
    )

    assert isinstance(result, DepositResponse)
    assert result.get_chargeId() == "example-transfer-id"

@pytest.mark.asyncio
async def test_deposit_invalid_account():
    activity_env = ActivityEnvironment()
    with pytest.raises(ApplicationError) as exc_info:
        await activity_env.run(
            AccountTransferActivities().deposit,
            "test-key",
            100.0,
            AccountTransferActivities.INVALID_ACCOUNT
        )

    assert "Deposit activity failed, account is invalid" in str(exc_info.value)
    assert exc_info.value.type == "InvalidAccount"
    assert exc_info.value.non_retryable == True

@pytest.mark.asyncio
async def test_send_notification():
    activity_env = ActivityEnvironment()
    input_data = TransferInput(amount=100, fromAccount="acc1", toAccount="acc2")

    result = await activity_env.run(
        AccountTransferActivities().sendNotification,
        input_data
    )

    assert result == "SUCCESS"

@pytest.mark.asyncio
async def test_undo_withdraw():
    activity_env = ActivityEnvironment()
    result = await activity_env.run(
        AccountTransferActivities().undoWithdraw,
        100.0
    )

    assert result == True
