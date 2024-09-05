using Microsoft.Extensions.Logging;
using Temporalio.Exceptions;
using Temporalio.Workflows;

namespace MoneyTransfer;

[Workflow]
public class AccountTransferWorkflow
{
    ActivityOptions options = new () 
    {
        StartToCloseTimeout = TimeSpan.FromSeconds(5),
        RetryPolicy = new() {
            NonRetryableErrorTypes = [ nameof(InvalidAccountException), ],
        }
    };

    [WorkflowRun]
    public async Task<TransferOutput> Transfer(TransferInput input)
    {
        Workflow.Logger.LogInformation($"Running normal Transfer workflow fromAccount: {input.FromAccount}, toAccount: {input.ToAccount}, amount: {input.Amount}");

        transferState = "starting";
        progressPercentage = 25;

        await Workflow.DelayAsync(TimeSpan.FromSeconds(ServerInfo.WorkflowSleepDuration));

        progressPercentage = 50;
        transferState = "running";

                // These variables are reflected in the UI
        progressPercentage = 60;
        transferState = "running";

       // Withdraw activity
        await Workflow.ExecuteActivityAsync((AccountTransferActivities act) => act.WithdrawAsync(input.Amount, false), options);

        // Pause for dramatic effect
        await Workflow.DelayAsync(TimeSpan.FromSeconds(2));

        try 
        {
            var idempotencyKey = Workflow.Random.Next().ToString();
            chargeResult = await Workflow.ExecuteActivityAsync(
                (AccountTransferActivities act) => 
                    act.Deposit(idempotencyKey, input.Amount, false), options);
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
    
    [WorkflowQuery("transferStatus")]
    public TransferStatus TransferStatus
    {
        get
        {
            return new TransferStatus(approvalTime, progressPercentage, transferState, string.Empty, chargeResult);
        }
    }

    // These variables are reflected in the UI
    private int progressPercentage = 10;
    private string transferState = "starting";
    // Time to allow for transfer approval
    private int approvalTime = 30;
    private ChargeResponse chargeResult = new(string.Empty);
}