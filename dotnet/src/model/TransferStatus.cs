using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class TransferStatus
{
    [JsonPropertyName("progressPercentage")]
    public int ProgressPercentage { get; set; }
    [JsonPropertyName("transferState")]
    public string TransferState { get; set; }
    [JsonPropertyName("workflowStatus")]
    public string WorkflowStatus { get; set; }
    [JsonPropertyName("chargeResult")]
    public DepositResponse DepositResponse { get; set; }
    [JsonPropertyName("approvalTime")]
    public int ApprovalTime { get; set; }

    public TransferStatus()
    {
        ProgressPercentage = 0;
        TransferState = "";
        WorkflowStatus = "";
        DepositResponse = new DepositResponse();
        ApprovalTime = 0;
    }

    public TransferStatus(int progressPercentage, string transferState, string workflowStatus, DepositResponse depositResponse, int approvalTime)
    {
        ProgressPercentage = progressPercentage;
        TransferState = transferState;
        WorkflowStatus = workflowStatus;
        DepositResponse = depositResponse;
        ApprovalTime = approvalTime;
    }
}
