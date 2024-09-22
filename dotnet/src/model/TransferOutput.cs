using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class TransferOutput
{
    [JsonPropertyName("depositResponse")]
    public DepositResponse DepositResponse { get; set; }

    public TransferOutput()
    {
        DepositResponse = new DepositResponse();
    }

    public TransferOutput(DepositResponse response)
    {
        DepositResponse = response;
    }
}
