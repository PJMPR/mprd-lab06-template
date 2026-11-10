package pl.edu.banklab.accounts;

import java.util.Objects;

public final class FundsReservation {
    private final ReservationId id;
    private final Money amount;
    private ReservationStatus status;

    public FundsReservation(ReservationId id, Money amount) {
        this.id = Objects.requireNonNull(id, "Identyfikator rezerwacji jest wymagany");
        this.amount = Objects.requireNonNull(amount, "Kwota rezerwacji jest wymagana");
        if (amount.isZero()) {
            throw new IllegalArgumentException("Kwota rezerwacji musi być dodatnia");
        }
        this.status = ReservationStatus.ACTIVE;
    }

    public ReservationId id() {
        return id;
    }

    public Money amount() {
        return amount;
    }

    public ReservationStatus status() {
        return status;
    }

    public void cancel() {
        requireActive();
        status = ReservationStatus.CANCELLED;
    }

    public void settle() {
        requireActive();
        status = ReservationStatus.SETTLED;
    }

    private void requireActive() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Tylko aktywna rezerwacja może zmienić stan");
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof FundsReservation reservation
            && id.equals(reservation.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
