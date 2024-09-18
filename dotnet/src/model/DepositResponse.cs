using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class DepositResponse
{
    [JsonPropertyName("chargeId")]
    public string DepositId { get; set; }

    public DepositResponse()
    {
        DepositId = "";
    }

    public DepositResponse(string id)
    {
        DepositId = id;
    }
}
