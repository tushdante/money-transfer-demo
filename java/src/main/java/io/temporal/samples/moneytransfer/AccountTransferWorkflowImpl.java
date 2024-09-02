package io.temporal.samples.moneytransfer;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.helper.ServerInfo;
import io.temporal.samples.moneytransfer.model.ChargeResponse;
import io.temporal.samples.moneytransfer.model.TransferInput;
import io.temporal.samples.moneytransfer.model.TransferOutput;
import io.temporal.samples.moneytransfer.model.TransferStatus;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class AccountTransferWorkflowImpl implements AccountTransferWorkflow {

    static final SearchAttributeKey<String> WORKFLOW_STEP =
            SearchAttributeKey.forKeyword("Step");

    private static final Logger log = LoggerFactory.getLogger(
            AccountTransferWorkflowImpl.class
    );

    // activity retry policy
    private final ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(5))
            .setRetryOptions(
                    RetryOptions.newBuilder()
                            .setDoNotRetry(
                                    AccountTransferActivitiesImpl.InvalidAccountException.class.getName()
                            )
                            .build()
            )
            .build();

    // activity stub
    private final AccountTransferActivities accountTransferActivities =
            Workflow.newActivityStub(AccountTransferActivities.class, options);

    // these variables are reflected in the UI
    private int progressPercentage = 10;
    private String transferState = "starting";

    // workflow response object
    private ChargeResponse chargeResult = new ChargeResponse("");

    // time to allow for transfer approval
    private int approvalTime = 30;

    private boolean approved = false;

    // workflow
    @Override
    public TransferOutput transfer(TransferInput params) {
        transferState = "starting";
        progressPercentage = 25;

        Workflow.sleep(
                Duration.ofSeconds(ServerInfo.getWorkflowSleepDuration())
        );

        // these variables are reflected in the UI
        progressPercentage = 60;
        transferState = "running";

        accountTransferActivities.withdraw(params.getAmount(), false);
        Workflow.sleep(Duration.ofSeconds(2)); // for dramatic effect

        try {
            String idempotencyKey = Workflow.randomUUID().toString();
            // deposit activity
            chargeResult = accountTransferActivities.deposit(
                    idempotencyKey,
                    params.getAmount(),
                    false
            );
        } // if deposit() fails in an unrecoverable way, rollback the withdrawal and fail the workflow
        catch (ActivityFailure e) {
            log.info(
                    "\n\nDeposit failed unrecoverably, reverting withdraw\n\n"
            );

            // undoWithdraw activity (rollback)
            accountTransferActivities.undoWithdraw(params.getAmount());

            // return failure message
            String message =
                    ((ApplicationFailure) e.getCause()).getOriginalMessage();
            throw ApplicationFailure.newNonRetryableFailure(
                    message,
                    "DepositFailed"
            );
        }

        // these variables are reflected in the UI
        progressPercentage = 80;
        Workflow.sleep(Duration.ofSeconds(6));
        progressPercentage = 100;
        transferState = "finished";

        return new TransferOutput(chargeResult);
    }

    @Override
    public TransferStatus getStateQuery() {
        TransferStatus transferStatus = new TransferStatus(
                progressPercentage,
                transferState,
                "",
                chargeResult,
                approvalTime
        );
        return transferStatus;
    }

    @Override
    public void approveTransfer() {
        log.info("\n\nApprove Signal Received\n\n");

        if (this.transferState.equals("waiting")) {
            this.approved = true;
        } else {
            log.info(
                    "\n\nSignal not applied: Transfer is not waiting for approval.\n\n"
            );
        }
    }

    @Override
    public String approveTransferUpdate() {
        log.info("\n\nApprove Update Validated: Approving Transfer\n\n");
        this.approved = true;
        return "successfully approved transfer";
    }

    @Override
    public void approveTransferUpdateValidator() {
        log.info("\n\nApprove Update Received: Validating\n\n");
        if (this.approved) {
            throw new IllegalStateException(
                    "Validation Failed: Transfer already approved"
            );
        }
        if (!transferState.equals("waiting")) {
            throw new IllegalStateException(
                    "Validation Failed: Transfer doesn't require approval"
            );
        }
    }
}
