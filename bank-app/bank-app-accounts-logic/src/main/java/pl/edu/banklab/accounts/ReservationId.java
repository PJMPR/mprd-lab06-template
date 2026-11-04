package pl.edu.banklab.accounts;

public record ReservationId(String value) {
    public ReservationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identyfikator rezerwacji nie może być pusty");
        }
    }
}
