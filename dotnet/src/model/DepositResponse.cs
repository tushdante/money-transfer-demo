using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class DepositResponse(string depositId)
{
    [JsonPropertyName("chargeId")]
    public string DepositId { get; set; } = depositId;
}
