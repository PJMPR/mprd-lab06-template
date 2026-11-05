package pl.edu.banklab.transfers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferServiceTest {
    private final AccountFunds funds = new AccountFunds("ACC-1", true, 100_000L);
    private final DailyTransferLimit limit = new DailyTransferLimit(60_000L);
    private final TrustedBeneficiaries trusted = accountId -> true;
    private final TransferService service = new TransferService(funds, limit, trusted);

    private RequestTransferCommand transfer(long amount) {
        return new RequestTransferCommand("ACC-2", amount);
    }

    private void assertUnchanged() {
        assertAll(
            () -> assertEquals(100_000L, funds.availableInCents()),
            () -> assertEquals(60_000L, limit.remainingInCents())
        );
    }

    @Test
    void acceptsTransferAndReservesFundsAndLimit() {
        assertEquals(TransferDecision.ACCEPTED, service.accept(transfer(25_000L)));
        assertAll(
            () -> assertEquals(75_000L, funds.availableInCents()),
            () -> assertEquals(35_000L, limit.remainingInCents())
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10_000L})
    void rejectsNonPositiveAmountWithoutChangingState(long amount) {
        assertEquals(TransferDecision.INVALID_AMOUNT, service.accept(transfer(amount)));
        assertUnchanged();
    }

    @Test
    void rejectsTransferToTheSameAccountWithoutChangingState() {
        assertEquals(TransferDecision.SAME_ACCOUNT,
            service.accept(new RequestTransferCommand("ACC-1", 10_000L)));
        assertUnchanged();
    }

    @Test
    void rejectsInactiveSourceAccountWithoutChangingState() {
        var inactive = new AccountFunds("ACC-1", false, 100_000L);
        var otherService = new TransferService(inactive, limit, trusted);
        assertEquals(TransferDecision.ACCOUNT_INACTIVE, otherService.accept(transfer(10_000L)));
        assertAll(
            () -> assertEquals(100_000L, inactive.availableInCents()),
            () -> assertEquals(60_000L, limit.remainingInCents())
        );
    }

    @Test
    void rejectsInsufficientFundsBeforeCheckingLimit() {
        assertEquals(TransferDecision.INSUFFICIENT_FUNDS, service.accept(transfer(100_001L)));
        assertUnchanged();
    }

    @Test
    void rejectsExceededDailyLimitWithoutReservingFunds() {
        assertEquals(TransferDecision.DAILY_LIMIT_EXCEEDED, service.accept(transfer(60_001L)));
        assertUnchanged();
    }

    @Test
    void acceptsAmountsEqualToRemainingLimitAndAvailableFunds() {
        assertEquals(TransferDecision.ACCEPTED, service.accept(transfer(60_000L)));
        assertAll(
            () -> assertEquals(40_000L, funds.availableInCents()),
            () -> assertEquals(0L, limit.remainingInCents())
        );

        var exactFunds = new AccountFunds("ACC-1", true, 20_000L);
        var exactLimit = new DailyTransferLimit(60_000L);
        var otherService = new TransferService(exactFunds, exactLimit, trusted);
        assertEquals(TransferDecision.ACCEPTED, otherService.accept(transfer(20_000L)));
        assertEquals(0L, exactFunds.availableInCents());
    }

    @Test
    void subsequentTransfersUseRemainingState() {
        assertEquals(TransferDecision.ACCEPTED, service.accept(transfer(40_000L)));
        assertEquals(TransferDecision.DAILY_LIMIT_EXCEEDED, service.accept(transfer(25_000L)));
        assertAll(
            () -> assertEquals(60_000L, funds.availableInCents()),
            () -> assertEquals(20_000L, limit.remainingInCents())
        );

        var smallFunds = new AccountFunds("ACC-1", true, 30_000L);
        var freshLimit = new DailyTransferLimit(60_000L);
        var otherService = new TransferService(smallFunds, freshLimit, trusted);
        assertEquals(TransferDecision.ACCEPTED, otherService.accept(transfer(20_000L)));
        assertEquals(TransferDecision.INSUFFICIENT_FUNDS, otherService.accept(transfer(15_000L)));
    }

    @Test
    void validatesRulesInSpecifiedOrder() {
        var inactive = new AccountFunds("ACC-1", false, 100_000L);
        var otherService = new TransferService(inactive, limit, accountId -> false);
        assertAll(
            () -> assertEquals(TransferDecision.INVALID_AMOUNT,
                otherService.accept(new RequestTransferCommand("ACC-1", 0L))),
            () -> assertEquals(TransferDecision.SAME_ACCOUNT,
                otherService.accept(new RequestTransferCommand("ACC-1", 100_001L))),
            () -> assertEquals(TransferDecision.ACCOUNT_INACTIVE,
                otherService.accept(new RequestTransferCommand("ACC-2", 100_001L)))
        );
        assertEquals(60_000L, limit.remainingInCents());
    }

    @Test
    void acceptsExactlyTwoHundredEurosForUntrustedBeneficiary() {
        var untrustedService = new TransferService(funds, limit, accountId -> false);
        assertEquals(TransferDecision.ACCEPTED, untrustedService.accept(transfer(20_000L)));
        assertAll(
            () -> assertEquals(80_000L, funds.availableInCents()),
            () -> assertEquals(40_000L, limit.remainingInCents())
        );
    }

    @Test
    void rejectsMoreThanTwoHundredEurosForUntrustedBeneficiaryWithoutChangingState() {
        var untrustedService = new TransferService(funds, limit, accountId -> false);
        assertEquals(TransferDecision.UNTRUSTED_BENEFICIARY_LIMIT_EXCEEDED,
            untrustedService.accept(transfer(20_001L)));
        assertUnchanged();
    }

    @Test
    void acceptsMoreThanTwoHundredEurosForTrustedBeneficiary() {
        assertEquals(TransferDecision.ACCEPTED, service.accept(transfer(20_001L)));
        assertAll(
            () -> assertEquals(79_999L, funds.availableInCents()),
            () -> assertEquals(39_999L, limit.remainingInCents())
        );
    }

    @Test
    void checksDailyLimitBeforeUntrustedBeneficiaryLimit() {
        var smallLimit = new DailyTransferLimit(20_000L);
        var untrustedService = new TransferService(funds, smallLimit, accountId -> false);
        assertEquals(TransferDecision.DAILY_LIMIT_EXCEEDED,
            untrustedService.accept(transfer(20_001L)));
        assertAll(
            () -> assertEquals(100_000L, funds.availableInCents()),
            () -> assertEquals(20_000L, smallLimit.remainingInCents())
        );
    }
}
