package pl.edu.banklab.accounts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {
    private final Account account = Account.open(new AccountId("PL-001"));

    @Test
    void entityEqualityDependsOnlyOnAccountId() {
        var sameAccount = Account.open(new AccountId("PL-001"));
        var anotherAccount = Account.open(new AccountId("PL-002"));

        assertEquals(account, sameAccount);
        assertNotEquals(account, anotherAccount);
    }

    @Test
    void creditsAccountAndAppendsOperationHistory() {
        account.credit(new OperationId("OP-1"), Money.pln(10_000), "Wpłata");

        assertAll(
            () -> assertEquals(Money.pln(10_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(10_000), account.availableFunds()),
            () -> assertEquals(1, account.history().size()),
            () -> assertEquals(OperationType.CREDIT, account.history().get(0).type())
        );
    }

    @Test
    void reservationReducesAvailableFundsButNotBookedBalance() {
        creditTenThousand();

        account.reserve(new ReservationId("RES-1"), Money.pln(4_000));

        assertAll(
            () -> assertEquals(Money.pln(10_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(6_000), account.availableFunds()),
            () -> assertEquals(1, account.history().size()),
            () -> assertEquals(ReservationStatus.ACTIVE, account.reservations().get(0).status())
        );
    }

    @Test
    void rejectsReservationAboveAvailableFundsWithoutChangingState() {
        creditTenThousand();

        assertThrows(IllegalStateException.class,
            () -> account.reserve(new ReservationId("RES-1"), Money.pln(10_001)));
        assertAll(
            () -> assertEquals(Money.pln(10_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(10_000), account.availableFunds()),
            () -> assertEquals(0, account.reservations().size()),
            () -> assertEquals(1, account.history().size())
        );
    }

    @Test
    void cancellationRestoresAvailableFundsWithoutAddingBookedOperation() {
        creditTenThousand();
        account.reserve(new ReservationId("RES-1"), Money.pln(4_000));

        account.cancel(new ReservationId("RES-1"));

        assertAll(
            () -> assertEquals(Money.pln(10_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(10_000), account.availableFunds()),
            () -> assertEquals(1, account.history().size()),
            () -> assertEquals(ReservationStatus.CANCELLED, account.reservations().get(0).status())
        );
    }

    @Test
    void settlementDebitsBookedBalanceAndAppendsHistory() {
        creditTenThousand();
        account.reserve(new ReservationId("RES-1"), Money.pln(4_000));

        account.settle(new ReservationId("RES-1"), new OperationId("OP-2"), "Płatność kartą");

        assertAll(
            () -> assertEquals(Money.pln(6_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(6_000), account.availableFunds()),
            () -> assertEquals(2, account.history().size()),
            () -> assertEquals(OperationType.DEBIT, account.history().get(1).type()),
            () -> assertEquals(ReservationStatus.SETTLED, account.reservations().get(0).status())
        );
    }

    @Test
    void rejectsDuplicateIdentifiersAndSecondReservationTransition() {
        creditTenThousand();
        account.reserve(new ReservationId("RES-1"), Money.pln(1_000));

        assertThrows(IllegalArgumentException.class,
            () -> account.credit(new OperationId("OP-1"), Money.pln(500), "Duplikat"));
        assertThrows(IllegalArgumentException.class,
            () -> account.reserve(new ReservationId("RES-1"), Money.pln(500)));

        account.cancel(new ReservationId("RES-1"));
        assertThrows(IllegalStateException.class,
            () -> account.cancel(new ReservationId("RES-1")));
        assertThrows(IllegalStateException.class,
            () -> account.settle(new ReservationId("RES-1"), new OperationId("OP-2"), "Za późno"));
    }

    @Test
    void exposesUnmodifiableHistoryAndReservations() {
        creditTenThousand();
        account.reserve(new ReservationId("RES-1"), Money.pln(1_000));

        assertThrows(UnsupportedOperationException.class, () -> account.history().clear());
        assertThrows(UnsupportedOperationException.class, () -> account.reservations().clear());
    }

    private void creditTenThousand() {
        account.credit(new OperationId("OP-1"), Money.pln(10_000), "Wpłata");
    }
}
