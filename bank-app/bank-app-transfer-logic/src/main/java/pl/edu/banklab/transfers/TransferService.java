package pl.edu.banklab.transfers;

public final class TransferService {
    private static final long UNTRUSTED_BENEFICIARY_LIMIT_IN_CENTS = 20_000L;

    private final AccountFunds funds;
    private final DailyTransferLimit limit;
    private final TrustedBeneficiaries trustedBeneficiaries;

    public TransferService(AccountFunds funds, DailyTransferLimit limit,
                           TrustedBeneficiaries trustedBeneficiaries) {
        this.funds = funds;
        this.limit = limit;
        this.trustedBeneficiaries = trustedBeneficiaries;
    }

    public TransferDecision accept(RequestTransferCommand command) {
        long amount = command.amountInCents();

        if (amount <= 0) {
            return TransferDecision.INVALID_AMOUNT;
        }
        if (funds.accountId().equals(command.targetAccountId())) {
            return TransferDecision.SAME_ACCOUNT;
        }
        if (!funds.active()) {
            return TransferDecision.ACCOUNT_INACTIVE;
        }
        if (amount > funds.availableInCents()) {
            return TransferDecision.INSUFFICIENT_FUNDS;
        }
        if (amount > limit.remainingInCents()) {
            return TransferDecision.DAILY_LIMIT_EXCEEDED;
        }
        if (amount > UNTRUSTED_BENEFICIARY_LIMIT_IN_CENTS
                && !trustedBeneficiaries.isTrusted(command.targetAccountId())) {
            return TransferDecision.UNTRUSTED_BENEFICIARY_LIMIT_EXCEEDED;
        }

        funds.reserve(amount);
        limit.consume(amount);
        return TransferDecision.ACCEPTED;
    }
}
