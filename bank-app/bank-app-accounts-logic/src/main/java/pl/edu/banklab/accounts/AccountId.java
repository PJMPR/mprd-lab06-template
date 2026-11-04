package pl.edu.banklab.accounts;

public record AccountId(String value) {
    public AccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identyfikator rachunku nie może być pusty");
        }
    }
}
