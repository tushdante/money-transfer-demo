using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class TransferOutput(DepositResponse response)
{
    [JsonPropertyName("depositResponse")]
    public DepositResponse DepositResponse { get; set; } = response;

    // public TransferOutput()
    // {
    //     DepositResponse = new DepositResponse();
    // }
}
