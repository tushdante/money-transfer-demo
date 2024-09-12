using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class TransferInput(int amount, string fromAccount, string toAccount)
{
    [JsonPropertyName("amount")]
    public int Amount { get; set; } = amount;
    [JsonPropertyName("fromAccount")]
    public string FromAccount { get; set; } = fromAccount;
    [JsonPropertyName("toAccount")]
    public string ToAccount { get; set; } = toAccount;
}
