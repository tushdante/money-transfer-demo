package io.temporal.samples.moneytransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.common.converter.EncodedValues;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.dataclasses.*;
import io.temporal.samples.moneytransfer.helper.ServerInfo;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

public class AccountTransferWorkflowScenarios implements DynamicWorkflow {

  private static final String BUG = "AccountTransferWorkflowRecoverableFailure";
  private static final String NEEDS_APPROVAL = "AccountTransferWorkflowHumanInLoop";
  private static final String ADVANCED_VISIBILITY = "AccountTransferWorkflowAdvancedVisibility";
  private static final String API_DOWNTIME = "AccountTransferWorkflowAPIDowntime";

  private static final String INVALID_ACCOUNT = "AccountTransferWorkflowInvalidAccount";

  private static final Logger log = Workflow.getLogger(AccountTransferWorkflowScenarios.class);
  static final SearchAttributeKey<String> WORKFLOW_STEP = SearchAttributeKey.forKeyword("Step");
  private final ActivityOptions options =
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .setRetryOptions(
              RetryOptions.newBuilder()
                  .setDoNotRetry(
                      AccountTransferActivitiesImpl.InvalidAccountException.class.getName())
                  .build())
          .build();

  // activity stub
  private final AccountTransferActivities accountTransferActivities =
      Workflow.newActivityStub(AccountTransferActivities.class, options);

  // these variables are reflected in the UI
  private int progressPercentage = 10;
  private String transferState = "starting";

  // workflow response object
  private ChargeResponseObj chargeResult = new ChargeResponseObj("");

  // time to allow for transfer approval
  private int approvalTime = 30;

  private boolean approved = false;

  @Override
  public Object execute(EncodedValues args) {
    Workflow.registerListener(new AccountTransferDynamicListenerImpl());
    TransferInput input = args.get(0, TransferInput.class);
    String type = Workflow.getInfo().getWorkflowType();
    log.info(
        "Dynamic Account Transfer workflow started, type = {}, fromAccount = {}, toAccount = {}",
        type,
        input.getFromAccount(),
        input.getToAccount());

    transferState = "starting";
    progressPercentage = 25;

    Workflow.sleep(Duration.ofSeconds(ServerInfo.getWorkflowSleepDuration()));

    progressPercentage = 50;
    transferState = "running";

    if (NEEDS_APPROVAL.equals(type)) {
      log.info(
          "\n\nWaiting on 'approveTransfer' Signal or Update for workflow ID: "
              + Workflow.getInfo().getWorkflowId()
              + "\n\n");
      transferState = "waiting";

      // Wait for the approval signal for up to approvalTime
      boolean receivedSignal = Workflow.await(Duration.ofSeconds(approvalTime), () -> approved);

      // If the signal was not received within the timeout, fail the workflow
      if (!receivedSignal) {
        log.error(
            "Approval not received within the "
                + approvalTime
                + "-second time window: "
                + "Failing the workflow.");
        throw ApplicationFailure.newFailure(
            "Approval not received within " + approvalTime + " seconds", "ApprovalTimeout");
      }
    }

    // these variables are reflected in the UI
    progressPercentage = 60;
    transferState = "running";

    // withdraw activity
    if (ADVANCED_VISIBILITY.equals(type)) {
      Workflow.upsertTypedSearchAttributes(WORKFLOW_STEP.valueSet("Withdraw"));
      Workflow.sleep(Duration.ofSeconds(5)); // for dramatic effect
    }

    boolean simulateDelay = API_DOWNTIME.equals(type);
    accountTransferActivities.withdraw(input.getAmount(), simulateDelay);
    Workflow.sleep(Duration.ofSeconds(2)); // for dramatic effect

    // Simulate bug in workflow
    if (BUG.equals(type)) {
      // throw an error to simulate a bug in the workflow
      // uncomment the following line and restart workers to 'fix' the bug
      log.info("\n\nSimulating workflow task failure.\n\n");

      throw new RuntimeException("Workflow Bug!");
    }

    if (ADVANCED_VISIBILITY.equals(type)) {
      Workflow.upsertTypedSearchAttributes(WORKFLOW_STEP.valueSet("Deposit"));
    }

    try {
      String idempotencyKey = Workflow.randomUUID().toString();
      boolean invalidAccount = INVALID_ACCOUNT.equals(type);
      // deposit activity
      chargeResult =
          accountTransferActivities.deposit(idempotencyKey, input.getAmount(), invalidAccount);
    }
    // if deposit() fails in an unrecoverable way, rollback the withdrawal and fail the workflow
    catch (ActivityFailure e) {
      log.info("\n\nDeposit failed unrecoverable error, reverting withdraw\n\n");

      // undoWithdraw activity (rollback)
      accountTransferActivities.undoWithdraw(input.getAmount());

      // return failure message
      String message = ((ApplicationFailure) e.getCause()).getOriginalMessage();
      throw ApplicationFailure.newNonRetryableFailure(message, "DepositFailed");
    }

    // these variables are reflected in the UI
    progressPercentage = 80;
    Workflow.sleep(Duration.ofSeconds(6));
    progressPercentage = 100;
    transferState = "finished";

    return new TransferOutput(chargeResult);
  }

  class AccountTransferDynamicListenerImpl implements AccountTransferMessages {

    @Override
    public StateObj getStateQuery() throws JsonProcessingException {
      return new StateObj(progressPercentage, transferState, "", chargeResult, approvalTime);
    }

    @Override
    public void approveTransfer() {
      log.info("\n\nApprove Signal Received\n\n");

      if (transferState.equals("waiting")) {
        approved = true;
      } else {
        log.info("\n\nSignal not applied: Transfer is not waiting for approval.\n\n");
      }
    }

    @Override
    public String approveTransferUpdate() {
      log.info("\n\nApprove Update Validated: Approving Transfer\n\n");
      approved = true;
      return "successfully approved transfer";
    }

    @Override
    public void approveTransferUpdateValidator() {
      log.info("\n\nApprove Update Received: Validating\n\n");
      if (approved) {
        throw new IllegalStateException("Validation Failed: Transfer already approved");
      }
      if (!transferState.equals("waiting")) {
        throw new IllegalStateException("Validation Failed: Transfer doesn't require approval");
      }
    }
  }
}
