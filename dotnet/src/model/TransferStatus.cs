using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class TransferStatus(int progress, string transferState, string workflowStatus, DepositResponse depositResponse, int approvalTime)
{
    [JsonPropertyName("progressPercentage")]
    public int ProgressPercentage { get; set; } = progress;
    [JsonPropertyName("transferState")]
    public string TransferState { get; set; } = transferState;
    [JsonPropertyName("workflowStatus")]
    public string WorkflowStatus { get; set; } = workflowStatus;
    [JsonPropertyName("chargeResult")]
    public DepositResponse DepositResponse { get; set; } = depositResponse;
    [JsonPropertyName("approvalTime")]
    public int ApprovalTime { get; set; } = approvalTime;
}
