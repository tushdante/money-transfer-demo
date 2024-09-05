using System.Text.Json.Serialization;

namespace MoneyTransfer;

public record TransferInput
{
    [JsonPropertyName("amount")]
    public required int Amount { get; init; }
    [JsonPropertyName("fromAccount")]
    public required string FromAccount { get; init;}
    [JsonPropertyName("toAccount")]
    public required string ToAccount { get; init; }
}