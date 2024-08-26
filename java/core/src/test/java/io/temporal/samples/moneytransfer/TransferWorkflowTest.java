package io.temporal.samples.moneytransfer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.samples.moneytransfer.dataclasses.ChargeResponseObj;
import io.temporal.samples.moneytransfer.dataclasses.TransferInput;
import io.temporal.samples.moneytransfer.dataclasses.TransferOutput;
import io.temporal.testing.TestWorkflowRule;
import java.time.Duration;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class TransferWorkflowTest {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          .setWorkflowTypes(AccountTransferWorkflowImpl.class)
          .setWorkflowTypes(AccountTransferWorkflowScenarios.class)
          .setDoNotStart(true)
          .build();

  /** Test workflow with real activities */
  @Test
  public void testWorkflowHappyPath() {
    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new AccountTransferActivitiesImpl());
    testWorkflowRule.getTestEnvironment().start();

    // Get a workflow stub using the same task queue the worker uses.
    AccountTransferWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                AccountTransferWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    // Execute a workflow waiting for it to complete.
    TransferInput transferInput = new TransferInput();
    transferInput.setAmount(100);
    transferInput.setFromAccount("account1");
    transferInput.setToAccount("account2");

    // HAPPY_PATH

    TransferOutput result = workflow.transfer(transferInput);
    assertEquals(
        new TransferOutput(new ChargeResponseObj("example-charge-id"))
            .getChargeResponseObj()
            .getChargeId(),
        result.getChargeResponseObj().getChargeId());
  }

  /** Test human in the loop scenario */
  @Test
  public void testWorkflowHumanInLoop() {
    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new AccountTransferActivitiesImpl());
    testWorkflowRule.getTestEnvironment().start();

    String WORKFLOW_ID = "HumanInLoopWorkflow";

    WorkflowStub workflowStub =
        testWorkflowRule
            .getWorkflowClient()
            .newUntypedWorkflowStub(
                "AccountTransferWorkflowHumanInLoop",
                WorkflowOptions.newBuilder()
                    .setWorkflowId(WORKFLOW_ID)
                    .setTaskQueue(testWorkflowRule.getTaskQueue())
                    .build());
    // Execute a workflow waiting for it to complete.
    TransferInput transferInput = new TransferInput();
    transferInput.setAmount(100);
    transferInput.setFromAccount("account1");
    transferInput.setToAccount("account2");

    workflowStub.start(transferInput);

    // Skip time so we're waiting for a signal
    testWorkflowRule.getTestEnvironment().sleep(Duration.ofSeconds(15));
    // signal the workflow

    workflowStub.signal("approveTransfer");

    TransferOutput transferOutput = workflowStub.getResult(TransferOutput.class);

    assertEquals(
        new TransferOutput(new ChargeResponseObj("example-charge-id"))
            .getChargeResponseObj()
            .getChargeId(),
        transferOutput.getChargeResponseObj().getChargeId());
  }

  /** Test workflow with mocked activities */
  @Test
  public void testMockedActivity() {
    AccountTransferActivities activities =
        mock(AccountTransferActivities.class, withSettings().withoutAnnotations());

    ChargeResponseObj chargeResponseObj = new ChargeResponseObj("example-charge-id");

    when(activities.withdraw(100.0f, false)).thenReturn("SUCCESS");
    when(activities.deposit(anyString(), eq(100.0f), eq(false))).thenReturn(chargeResponseObj);
    testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
    testWorkflowRule.getTestEnvironment().start();

    // Get a workflow stub using the same task queue the worker uses.
    AccountTransferWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                AccountTransferWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    // Execute a workflow waiting for it to complete.
    TransferInput transferInput = new TransferInput();
    transferInput.setAmount(100);
    transferInput.setFromAccount("account1");
    transferInput.setToAccount("account2");

    // Happy Path

    TransferOutput result = workflow.transfer(transferInput);
    assertEquals(
        new TransferOutput(new ChargeResponseObj("example-charge-id"))
            .getChargeResponseObj()
            .getChargeId(),
        result.getChargeResponseObj().getChargeId());
  }

  // Clean up test environment after tests are completed
  @After
  public void tearDown() {
    testWorkflowRule.getTestEnvironment().shutdown();
  }
}
