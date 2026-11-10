package pl.edu.banklab.transfers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testy startowe do laboratorium o agregatach.
 * Usuń @Disabled i odblokowuj reguły pojedynczo.
 */
class TransferOrderTest {

    @Test
    @Disabled("Etap 1: tożsamość, referencje przez ID i stan początkowy")
    void createsRequestedTransferAndKeepsItsIdentityAndReferences() {
        var order = requestedTransfer();

        assertAll(
            () -> assertEquals(new TransferId("TR-1"), order.id()),
            () -> assertEquals("ACC-1", order.sourceAccountId()),
            () -> assertEquals("ACC-2", order.targetAccountId()),
            () -> assertEquals(25_000L, order.amountInCents()),
            () -> assertEquals(TransferStatus.REQUESTED, order.status()),
            () -> assertTrue(order.rejectionReason().isEmpty())
        );
    }

    @Test
    @Disabled("Etap 2: dodatnia kwota jest niezmiennikiem agregatu")
    void rejectsNonPositiveAmountAtAggregateBoundary() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> request(0L, "ACC-1", "ACC-2")),
            () -> assertThrows(IllegalArgumentException.class,
                () -> request(-1L, "ACC-1", "ACC-2"))
        );
    }

    @Test
    @Disabled("Etap 3: rachunki źródłowy i docelowy muszą być różne")
    void rejectsTransferBetweenTheSameAccountAtAggregateBoundary() {
        assertThrows(IllegalArgumentException.class,
            () -> request(10_000L, "ACC-1", "ACC-1"));
    }

    @Test
    @Disabled("Etap 4: przyjęcie dyspozycji jest jednokrotnym przejściem stanu")
    void acceptsRequestedTransferExactlyOnce() {
        var order = requestedTransfer();

        order.accept();

        assertAll(
            () -> assertEquals(TransferStatus.ACCEPTED, order.status()),
            () -> assertTrue(order.rejectionReason().isEmpty()),
            () -> assertThrows(IllegalStateException.class, order::accept),
            () -> assertThrows(IllegalStateException.class,
                () -> order.reject(TransferDecision.INSUFFICIENT_FUNDS))
        );
    }

    @Test
    @Disabled("Etap 5: odrzucenie zapisuje powód i zamyka cykl decyzji")
    void rejectsRequestedTransferExactlyOnceAndKeepsReason() {
        var order = requestedTransfer();

        order.reject(TransferDecision.INSUFFICIENT_FUNDS);

        assertAll(
            () -> assertEquals(TransferStatus.REJECTED, order.status()),
            () -> assertEquals(TransferDecision.INSUFFICIENT_FUNDS,
                order.rejectionReason().orElseThrow()),
            () -> assertThrows(IllegalStateException.class, order::accept),
            () -> assertThrows(IllegalStateException.class,
                () -> order.reject(TransferDecision.DAILY_LIMIT_EXCEEDED))
        );
    }

    @Test
    @Disabled("Etap 6: ACCEPTED nie jest powodem odrzucenia")
    void acceptedDecisionCannotBeUsedAsRejectionReason() {
        var order = requestedTransfer();

        assertThrows(IllegalArgumentException.class,
            () -> order.reject(TransferDecision.ACCEPTED));
        assertAll(
            () -> assertEquals(TransferStatus.REQUESTED, order.status()),
            () -> assertTrue(order.rejectionReason().isEmpty())
        );
    }

    private TransferOrder requestedTransfer() {
        return request(25_000L, "ACC-1", "ACC-2");
    }

    private TransferOrder request(long amount, String source, String target) {
        return TransferOrder.request(new TransferId("TR-1"), source, target, amount);
    }
}

