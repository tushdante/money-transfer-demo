using System.Text.Json.Serialization;

namespace MoneyTransfer;

public class TransferInput
{
    [JsonPropertyName("amount")]
    public int Amount { get; set; }
    [JsonPropertyName("fromAccount")]
    public string FromAccount { get; set; }
    [JsonPropertyName("toAccount")]
    public string ToAccount { get; set; }

    public TransferInput()
    {
        Amount = 0;
        FromAccount = "";
        ToAccount = "";
    }

    public TransferInput(int amount, string fromAccount, string toAccount)
    {
        Amount = amount;
        FromAccount = fromAccount;
        ToAccount = toAccount;
    }
}
