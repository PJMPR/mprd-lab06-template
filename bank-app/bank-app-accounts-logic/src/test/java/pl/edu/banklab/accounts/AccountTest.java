package pl.edu.banklab.accounts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {
    private final Account account = Account.open(new AccountId("PL-001"));

    @Test
    @Disabled("Etap 5: sprawdza, że równość encji Account zależy wyłącznie od AccountId")
    void entityEqualityDependsOnlyOnAccountId() {
        var sameAccount = Account.open(new AccountId("PL-001"));
        var anotherAccount = Account.open(new AccountId("PL-002"));

        assertEquals(account, sameAccount);
        assertNotEquals(account, anotherAccount);
    }

    @Test
    @Disabled("Etap 6: sprawdza, że uznanie zwiększa saldo i dopisuje operację CREDIT do historii")
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
    @Disabled("Etap 7: sprawdza, że rezerwacja zmniejsza dostępne środki, ale nie saldo księgowe")
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
    @Disabled("Etap 8: sprawdza odrzucenie rezerwacji większej niż dostępne środki bez zmiany stanu")
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
    @Disabled("Etap 9: sprawdza, że anulowanie rezerwacji przywraca dostępne środki bez nowego księgowania")
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
    @Disabled("Etap 10: sprawdza, że rozliczenie rezerwacji zmniejsza saldo i zapisuje operację DEBIT")
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
    @Disabled("Etap 11: sprawdza odrzucenie powtórzonych identyfikatorów i ponownej zmiany zakończonej rezerwacji")
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
    @Disabled("Etap 12: sprawdza, że historia operacji i lista rezerwacji są tylko do odczytu")
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
