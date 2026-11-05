package pl.edu.banklab.transfers;

public record RequestTransferCommand(String targetAccountId, long amountInCents) {
}
