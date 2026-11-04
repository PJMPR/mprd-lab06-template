package pl.edu.banklab.accounts;

import java.util.Objects;

public final class AccountOperation {
    private final OperationId id;
    private final OperationType type;
    private final Money amount;
    private final String description;

    public AccountOperation(
        OperationId id,
        OperationType type,
        Money amount,
        String description
    ) {
        this.id = Objects.requireNonNull(id, "Identyfikator operacji jest wymagany");
        this.type = Objects.requireNonNull(type, "Rodzaj operacji jest wymagany");
        this.amount = Objects.requireNonNull(amount, "Kwota operacji jest wymagana");
        if (amount.isZero()) {
            throw new IllegalArgumentException("Kwota operacji musi być dodatnia");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Opis operacji nie może być pusty");
        }
        this.description = description;
    }

    public OperationId id() {
        return id;
    }

    public OperationType type() {
        return type;
    }

    public Money amount() {
        return amount;
    }

    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof AccountOperation operation
            && id.equals(operation.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
