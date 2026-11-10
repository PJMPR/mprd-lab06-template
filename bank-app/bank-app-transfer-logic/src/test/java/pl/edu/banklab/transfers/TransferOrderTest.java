package pl.edu.banklab.transfers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsIn.in;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    @Disabled("Etap 7: identyfikatory są wymagane i nie mogą być puste")
    void rejectsMissingOrBlankIdentifiersAtAggregateBoundary() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> TransferOrder.request(null, "ACC-1", "ACC-2", 10_000L)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> TransferOrder.request(new TransferId(" "), "ACC-1", "ACC-2", 10_000L)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> request(10_000L, null, "ACC-2")),
            () -> assertThrows(IllegalArgumentException.class,
                () -> request(10_000L, " ", "ACC-2")),
            () -> assertThrows(IllegalArgumentException.class,
                () -> request(10_000L, "ACC-1", null)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> request(10_000L, "ACC-1", " "))
        );
    }

    @Test
    @Disabled("Etap 8: agregat przechowuje ID, a nie obiekty z innych granic")
    void doesNotKeepReferencesToObjectsFromOtherBoundaries() {
        var forbiddenTypes = List.of(
            AccountFunds.class,
            DailyTransferLimit.class,
            TrustedBeneficiaries.class,
            TransferService.class
        );
        var fieldTypes = Arrays.stream(TransferOrder.class.getDeclaredFields())
            .map(field -> field.getType())
            .toList();

        assertThat(fieldTypes, everyItem(not(in(forbiddenTypes))));
    }

    @ParameterizedTest
    @EnumSource(value = TransferDecision.class, names = "ACCEPTED",
        mode = EnumSource.Mode.EXCLUDE)
    @Disabled("Etap 9: root zapisuje każdy konkretny powód odmowy")
    void keepsEveryRejectionReason(TransferDecision reason) {
        var order = requestedTransfer();

        order.reject(reason);

        assertAll(
            () -> assertEquals(TransferStatus.REJECTED, order.status()),
            () -> assertEquals(reason, order.rejectionReason().orElseThrow())
        );
    }

    @Test
    @Disabled("Etap 10: brak powodu nie może odrzucić ani zmienić dyspozycji")
    void rejectsNullReasonWithoutChangingAggregate() {
        var order = requestedTransfer();

        assertThrows(IllegalArgumentException.class, () -> order.reject(null));
        assertAll(
            () -> assertEquals(TransferStatus.REQUESTED, order.status()),
            () -> assertTrue(order.rejectionReason().isEmpty())
        );
    }

    @Test
    @Disabled("Etap 11: decyzja serwisu zostaje zapisana przez Aggregate Root")
    void recordsDecisionCalculatedByDomainService() {
        var trustedBeneficiaries = mock(TrustedBeneficiaries.class);
        when(trustedBeneficiaries.isTrusted("ACC-2")).thenReturn(false);
        var service = new TransferService(
            new AccountFunds("ACC-1", true, 100_000L),
            new DailyTransferLimit(60_000L),
            trustedBeneficiaries
        );
        var order = request(20_001L, "ACC-1", "ACC-2");

        var decision = service.accept(
            new RequestTransferCommand(order.targetAccountId(), order.amountInCents()));
        order.reject(decision);

        assertAll(
            () -> assertEquals(TransferStatus.REJECTED, order.status()),
            () -> assertEquals(TransferDecision.UNTRUSTED_BENEFICIARY_LIMIT_EXCEEDED,
                order.rejectionReason().orElseThrow())
        );
        verify(trustedBeneficiaries).isTrusted("ACC-2");
    }

    private TransferOrder requestedTransfer() {
        return request(25_000L, "ACC-1", "ACC-2");
    }

    private TransferOrder request(long amount, String source, String target) {
        return TransferOrder.request(new TransferId("TR-1"), source, target, amount);
    }
}

