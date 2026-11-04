package pl.edu.banklab.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Account {
    private final AccountId id;
    private Money bookedBalance;
    private final List<AccountOperation> history = new ArrayList<>();
    private final List<FundsReservation> reservations = new ArrayList<>();

    private Account(AccountId id) {
        this.id = Objects.requireNonNull(id, "Identyfikator rachunku jest wymagany");
        this.bookedBalance = Money.pln(0);
    }

    public static Account open(AccountId id) {
        return new Account(id);
    }

    public AccountId id() {
        return id;
    }

    public Money bookedBalance() {
        return bookedBalance;
    }

    public Money availableFunds() {
        throw new UnsupportedOperationException("TODO: saldo pomniejszone o aktywne rezerwacje");
    }

    public List<AccountOperation> history() {
        return history;
    }

    public List<FundsReservation> reservations() {
        return reservations;
    }

    public void credit(OperationId operationId, Money amount, String description) {
        throw new UnsupportedOperationException("TODO: uznanie rachunku i zapis w historii");
    }

    public void reserve(ReservationId reservationId, Money amount) {
        throw new UnsupportedOperationException("TODO: rezerwacja dostępnych środków");
    }

    public void cancel(ReservationId reservationId) {
        throw new UnsupportedOperationException("TODO: anulowanie aktywnej rezerwacji");
    }

    public void settle(
        ReservationId reservationId,
        OperationId operationId,
        String description
    ) {
        throw new UnsupportedOperationException("TODO: rozliczenie rezerwacji i zapis obciążenia");
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
