using Microsoft.Extensions.Logging;
using Temporalio.Common;
using Temporalio.Converters;
using Temporalio.Exceptions;
using Temporalio.Workflows;

namespace MoneyTransfer;

[Workflow(Dynamic = true)]
public class AccountTransferWorkflowScenarios
{
    [WorkflowRun]
    public async Task<TransferOutput> RunAsync(IRawValue[] args)
    {
        String type = Workflow.Info.WorkflowType;
        var input = Workflow.PayloadConverter.ToValue<TransferInput>(args[0]);
        Workflow.Logger.LogInformation(
            string.Join("", "Dynamic Account Transfer workflow started, ",
                $"type: {type}. From: {input.FromAccount} To: ",
                $"{input.ToAccount} Amount: {input.Amount}"));

       transferState = "starting";
        progressPercentage = 25;

        await Workflow.DelayAsync(TimeSpan.FromSeconds(ServerInfo.WorkflowSleepDuration));

        progressPercentage = 50;
        transferState = "running";

        if (NEEDS_APPROVAL.Equals(type))
        {
            Workflow.Logger.LogInformation(
                "\n\nWaiting on 'approveTransfer' Signal or Update for workflow ID: " +
                Workflow.Info.WorkflowId + "\n\n");
            
            transferState = "waiting";
            var received = await Workflow.WaitConditionAsync(() => approved, TimeSpan.FromSeconds(approvalTime));
            if (!received)
            {
                Workflow.Logger.LogInformation(
                    "Approval not received within the " + 
                    approvalTime + 
                    " -second time window: Failing the workflow.");

                throw new ApplicationFailureException("Approval not recieved wihin " + approvalTime + " seconds");
            }
        }

        // These variables are reflected in the UI
        progressPercentage = 60;
        transferState = "running";

        if (ADVANCED_VISIBILITY.Equals(type))
        {
            Workflow.UpsertTypedSearchAttributes(stepAttributKey.ValueSet("Withdraw"));
            // Pause for dramatic effect
            await Workflow.DelayAsync(TimeSpan.FromSeconds(5));            
        }

        // Withdraw activity
        var simulateDelay = API_DOWNTIME.Equals(type);
        await Workflow.ExecuteActivityAsync((AccountTransferActivities act) => act.WithdrawAsync(input.Amount, simulateDelay), options);

        // Pause for dramatic effect
        await Workflow.DelayAsync(TimeSpan.FromSeconds(2));

        // Simulate bug in workflow
        if (BUG.Equals(type))
        {
            // Throw an error to simulate a bug in the workflow
            // uncomment the following line and restart workers to fix the bug
            Workflow.Logger.LogInformation("\nSimulating workflow task failure.\n");
            throw new InvalidOperationException("Simulating workflow bug!");
        }

        if (ADVANCED_VISIBILITY.Equals(type))
        {
            Workflow.UpsertTypedSearchAttributes(stepAttributKey.ValueSet("Deposit")); 
        }

        try 
        {
            var idempotencyKey = Workflow.Random.Next().ToString();
            var invalidAccount = INVALID_ACCOUNT.Equals(type);
            chargeResult = await Workflow.ExecuteActivityAsync(
                (AccountTransferActivities act) => 
                    act.Deposit(idempotencyKey, input.Amount, invalidAccount), options);
        }
        catch (ActivityFailureException exception)
        {
            Workflow.Logger.LogInformation("\n\nDeposit failed unrecoverably, reverting withdraw\n\n");
            // Undo activity (rollback)
            await Workflow.ExecuteActivityAsync(
                (AccountTransferActivities act) => 
                    act.UndoWithdraw(input.Amount), options);

            // Return failure message
            throw new ApplicationFailureException(exception.Message);
        }

        // These variables are reflected in the UI
        progressPercentage = 80;
        await Workflow.DelayAsync(TimeSpan.FromSeconds(6));
        progressPercentage = 100;
        transferState = "finished";            
        
        return new TransferOutput(chargeResult);
    }

    [WorkflowSignal("approveTransfer")]
    public async Task ApproveTransfer()
    {
        Workflow.Logger.LogInformation("\nApprove Signal Received\n");
        if (transferState == "waiting")
        {
            approved = true;
        }
        else
        {
            Workflow.Logger.LogInformation($"\nSignal not applied: Transfer is not waiting for approval. {transferState}\n");
        }
    }
    
    [WorkflowQuery("transferStatus")]
    public TransferStatus TransferStatus
    {
        get
        {
            return new TransferStatus(approvalTime, progressPercentage, transferState, string.Empty, chargeResult);
        }
    }

    [WorkflowUpdate("approveTransferUpdate")]
    public Task<string> approveTransferUpdate()
    {
        Workflow.Logger.LogInformation("\n\nApprove Update Validated: Approving Transfer\n\n");
        approved = true;
        return Task.FromResult("successfully approved transfer");
    }

    [WorkflowUpdateValidator("approveTransferUpdate")]
    public void approveTransferUpdateValidator() 
    {
        Workflow.Logger.LogInformation("\n\nApprove Update Validated: Approving Transfer\n\n");
        if (approved) 
        {
            throw new InvalidOperationException("Validation Failed: Transfer already approved");
        }
        if (transferState != "waiting")
        {
            throw new InvalidOperationException("Validation Failed: Transfer doesn't require approval");
        }
    }

    private static String BUG = "AccountTransferWorkflowRecoverableFailure";
    private static String NEEDS_APPROVAL = "AccountTransferWorkflowHumanInLoop";
    private static String ADVANCED_VISIBILITY = "AccountTransferWorkflowAdvancedVisibility";
    private static String API_DOWNTIME = "AccountTransferWorkflowAPIDowntime";
    private static String INVALID_ACCOUNT = "AccountTransferWorkflowInvalidAccount";

    // These variables are reflected in the UI
    private int progressPercentage = 10;
    private string transferState = "starting";
    // Time to allow for transfer approval
    private int approvalTime = 30;
    private bool approved = false;
    private ChargeResponse chargeResult = new(string.Empty);

    ActivityOptions options = new () 
    {
        StartToCloseTimeout = TimeSpan.FromSeconds(5),
        RetryPolicy = new() {
            NonRetryableErrorTypes = [ nameof(InvalidAccountException), ],
        }
    };

    private readonly static SearchAttributeKey<string> stepAttributKey = SearchAttributeKey.CreateKeyword("Step");
}