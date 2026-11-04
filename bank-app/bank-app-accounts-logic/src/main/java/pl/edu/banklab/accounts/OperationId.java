package pl.edu.banklab.accounts;

public record OperationId(String value) {
    public OperationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identyfikator operacji nie może być pusty");
        }
    }
}
