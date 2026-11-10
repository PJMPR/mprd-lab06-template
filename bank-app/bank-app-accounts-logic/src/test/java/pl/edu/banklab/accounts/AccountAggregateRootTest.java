package pl.edu.banklab.accounts;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testy startowe do laboratorium o agregatach.
 * Usuń @Disabled i odblokowuj reguły pojedynczo.
 */
class AccountAggregateRootTest {

    @Test
    @Disabled("Etap 1: root jest jedyną publiczną bramą zmiany rezerwacji")
    void reservationStateCanChangeOnlyThroughAggregateRoot() throws Exception {
        var cancel = FundsReservation.class.getDeclaredMethod("cancel");
        var settle = FundsReservation.class.getDeclaredMethod("settle");

        assertAll(
            () -> assertFalse(Modifier.isPublic(cancel.getModifiers())),
            () -> assertFalse(Modifier.isPublic(settle.getModifiers()))
        );
    }

    @Test
    @Disabled("Etap 2: encje wewnętrzne tworzy Aggregate Root")
    void internalEntitiesAreNotCreatedThroughPublicConstructors() {
        assertAll(
            () -> assertFalse(hasPublicConstructor(FundsReservation.class)),
            () -> assertFalse(hasPublicConstructor(AccountOperation.class))
        );
    }

    @Test
    @Disabled("Etap 3: anulowanie odbywa się przez Aggregate Root")
    void rootCancelsReservationAndRestoresAvailableFunds() {
        var account = creditedAccount();
        var reservationId = new ReservationId("RES-1");
        account.reserve(reservationId, Money.pln(4_000));

        account.cancel(reservationId);

        assertAll(
            () -> assertEquals(Money.pln(10_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(10_000), account.availableFunds()),
            () -> assertEquals(1, account.history().size()),
            () -> assertEquals(ReservationStatus.CANCELLED,
                account.reservations().get(0).status())
        );
    }

    @Test
    @Disabled("Etap 4: rozliczenie zmienia spójnie cały agregat")
    void rootSettlesReservationAndChangesAllRelatedStateTogether() {
        var account = creditedAccount();
        var reservationId = new ReservationId("RES-1");
        account.reserve(reservationId, Money.pln(4_000));

        account.settle(reservationId, new OperationId("OP-2"), "Płatność kartą");

        var settlement = account.history().get(1);

        assertAll(
            () -> assertEquals(Money.pln(6_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(6_000), account.availableFunds()),
            () -> assertEquals(2, account.history().size()),
            () -> assertEquals(new OperationId("OP-2"), settlement.id()),
            () -> assertEquals(OperationType.DEBIT, settlement.type()),
            () -> assertEquals(Money.pln(4_000), settlement.amount()),
            () -> assertEquals("Płatność kartą", settlement.description()),
            () -> assertEquals(ReservationStatus.SETTLED,
                account.reservations().get(0).status())
        );
    }

    @Test
    @Disabled("Etap 5: odrzucona operacja nie zmienia agregatu")
    void rejectedTransitionLeavesWholeAggregateUnchanged() {
        var account = creditedAccount();
        var reservationId = new ReservationId("RES-1");
        account.reserve(reservationId, Money.pln(4_000));
        account.cancel(reservationId);

        assertThrows(IllegalStateException.class,
            () -> account.settle(reservationId, new OperationId("OP-2"), "Za późno"));

        assertAll(
            () -> assertEquals(Money.pln(10_000), account.bookedBalance()),
            () -> assertEquals(Money.pln(10_000), account.availableFunds()),
            () -> assertEquals(1, account.history().size()),
            () -> assertEquals(ReservationStatus.CANCELLED,
                account.reservations().get(0).status())
        );
    }

    private Account creditedAccount() {
        var account = Account.open(new AccountId("PL-001"));
        account.credit(new OperationId("OP-1"), Money.pln(10_000), "Wpłata");
        return account;
    }

    private boolean hasPublicConstructor(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors())
            .anyMatch(constructor -> Modifier.isPublic(constructor.getModifiers()));
    }
}

