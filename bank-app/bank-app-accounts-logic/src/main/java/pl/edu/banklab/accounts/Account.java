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
        Money available = bookedBalance;
        for (FundsReservation reservation : reservations) {
            if (reservation.status() == ReservationStatus.ACTIVE) {
                available = available.minus(reservation.amount());
            }
        }
        return available;
    }

    public List<AccountOperation> history() {
        return List.copyOf(history);
    }

    public List<FundsReservation> reservations() {
        return List.copyOf(reservations);
    }

    public void credit(OperationId operationId, Money amount, String description) {
        requireUniqueOperationId(operationId);
        AccountOperation operation = new AccountOperation(
            operationId, OperationType.CREDIT, amount, description);
        Money newBalance = bookedBalance.plus(amount);
        bookedBalance = newBalance;
        history.add(operation);
    }

    public void reserve(ReservationId reservationId, Money amount) {
        requireUniqueReservationId(reservationId);
        FundsReservation reservation = new FundsReservation(reservationId, amount);
        if (!amount.currency().equals(bookedBalance.currency())
            || amount.amountInCents() > availableFunds().amountInCents()) {
            throw new IllegalStateException("Brak wystarczających dostępnych środków");
        }
        reservations.add(reservation);
    }

    public void cancel(ReservationId reservationId) {
        findReservation(reservationId).cancel();
    }

    public void settle(
        ReservationId reservationId,
        OperationId operationId,
        String description
    ) {
        requireUniqueOperationId(operationId);
        FundsReservation reservation = findReservation(reservationId);
        if (reservation.status() != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Rozliczyć można tylko aktywną rezerwację");
        }
        AccountOperation operation = new AccountOperation(
            operationId, OperationType.DEBIT, reservation.amount(), description);
        Money newBalance = bookedBalance.minus(reservation.amount());
        reservation.settle();
        bookedBalance = newBalance;
        history.add(operation);
    }

    private FundsReservation findReservation(ReservationId reservationId) {
        Objects.requireNonNull(reservationId, "Identyfikator rezerwacji jest wymagany");
        return reservations.stream()
            .filter(reservation -> reservation.id().equals(reservationId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Nie znaleziono rezerwacji: " + reservationId.value()));
    }

    private void requireUniqueOperationId(OperationId operationId) {
        Objects.requireNonNull(operationId, "Identyfikator operacji jest wymagany");
        if (history.stream().anyMatch(operation -> operation.id().equals(operationId))) {
            throw new IllegalArgumentException("Operacja o tym identyfikatorze już istnieje");
        }
    }

    private void requireUniqueReservationId(ReservationId reservationId) {
        Objects.requireNonNull(reservationId, "Identyfikator rezerwacji jest wymagany");
        if (reservations.stream().anyMatch(reservation -> reservation.id().equals(reservationId))) {
            throw new IllegalArgumentException("Rezerwacja o tym identyfikatorze już istnieje");
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Account account && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
