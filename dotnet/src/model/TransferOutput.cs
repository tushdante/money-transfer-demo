using System.Text.Json.Serialization;

namespace MoneyTransfer;

public record TransferOutput
{
     [JsonPropertyName("chargeResponse")]
    public ChargeResponse ChargeResponse { get; init; }

    public TransferOutput(ChargeResponse response) => ChargeResponse = response;

    public TransferOutput() 
    {
        ChargeResponse = new ChargeResponse("");
    }
}