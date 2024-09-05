namespace MoneyTransfer;

#pragma warning disable IDE1006 // Naming Styles
public record TransferStatus(int approvalTime, int progressPercentage, string transferState, string workflowStatus, ChargeResponse chargeResult);

#pragma warning restore IDE1006 // Naming Styles