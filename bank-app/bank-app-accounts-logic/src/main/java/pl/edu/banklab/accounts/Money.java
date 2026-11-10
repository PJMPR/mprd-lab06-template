package pl.edu.banklab.accounts;

import java.util.Currency;
import java.util.Objects;

public record Money(long amountInCents, Currency currency) {
    private static final Currency PLN = Currency.getInstance("PLN");

    public Money {
        Objects.requireNonNull(currency, "Waluta jest wymagana");
        if (amountInCents < 0) {
            throw new IllegalArgumentException("Kwota nie może być ujemna");
        }
    }

    public static Money pln(long amountInCents) {
        return new Money(amountInCents, PLN);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(amountInCents, other.amountInCents), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        if (amountInCents < other.amountInCents) {
            throw new IllegalArgumentException("Wynik odejmowania nie może być ujemny");
        }
        return new Money(amountInCents - other.amountInCents, currency);
    }

    public boolean isZero() {
        return amountInCents == 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Druga kwota jest wymagana");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Nie można wykonywać działań na kwotach w różnych walutach");
        }
    }
}
