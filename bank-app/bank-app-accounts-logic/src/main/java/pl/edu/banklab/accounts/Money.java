package pl.edu.banklab.accounts;

import java.util.Currency;
import java.util.Objects;

public record Money(long amountInCents, Currency currency) {
    private static final Currency PLN = Currency.getInstance("PLN");

    public Money {
        Objects.requireNonNull(currency, "Waluta jest wymagana");
    }

    public static Money pln(long amountInCents) {
        return new Money(amountInCents, PLN);
    }

    public Money plus(Money other) {
        throw new UnsupportedOperationException("TODO: dodawanie kwot w tej samej walucie");
    }

    public Money minus(Money other) {
        throw new UnsupportedOperationException("TODO: odejmowanie bez tworzenia kwoty ujemnej");
    }

    public boolean isZero() {
        return amountInCents == 0;
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
