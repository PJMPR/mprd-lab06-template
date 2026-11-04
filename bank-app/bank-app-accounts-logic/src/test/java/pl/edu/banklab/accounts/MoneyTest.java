package pl.edu.banklab.accounts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {
    @Test
    @Disabled("Etap 1: sprawdza, że nie można utworzyć Money z ujemną kwotą")
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.pln(-1));
    }

    @Test
    @Disabled("Etap 2: sprawdza równość Money po kwocie i walucie, a nie po tożsamości obiektu")
    void isEqualByAmountAndCurrency() {
        assertEquals(Money.pln(1_000), Money.pln(1_000));
        assertNotEquals(Money.pln(1_000), Money.pln(1_001));
    }

    @Test
    @Disabled("Etap 3: sprawdza niemutowalne dodawanie i odejmowanie kwot w tej samej walucie")
    void addsAndSubtractsMoneyWithoutChangingOperands() {
        var ten = Money.pln(1_000);
        var three = Money.pln(300);

        assertEquals(Money.pln(1_300), ten.plus(three));
        assertEquals(Money.pln(700), ten.minus(three));
        assertEquals(Money.pln(1_000), ten);
    }

    @Test
    @Disabled("Etap 4: sprawdza odrzucenie działań na różnych walutach oraz wyniku poniżej zera")
    void rejectsArithmeticForDifferentCurrenciesAndNegativeResult() {
        var eur = new Money(100, Currency.getInstance("EUR"));

        assertThrows(IllegalArgumentException.class, () -> Money.pln(100).plus(eur));
        assertThrows(IllegalArgumentException.class, () -> Money.pln(99).minus(Money.pln(100)));
    }
}
