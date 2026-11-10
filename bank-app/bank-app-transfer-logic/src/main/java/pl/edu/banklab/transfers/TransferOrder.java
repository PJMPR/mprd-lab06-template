package pl.edu.banklab.transfers;

import java.util.Optional;

/**
 * Celowo niezaimplementowany szkielet Aggregate Root.
 * Kontrakt i kolejne kroki implementacji opisuje TransferOrderTest.
 */
public final class TransferOrder {
    private TransferOrder() {
    }

    public static TransferOrder request(
        TransferId id,
        String sourceAccountId,
        String targetAccountId,
        long amountInCents
    ) {
        throw new UnsupportedOperationException("Zaimplementuj Aggregate Root TransferOrder");
    }

    public TransferId id() {
        throw new UnsupportedOperationException("Zaimplementuj odczyt identyfikatora");
    }

    public String sourceAccountId() {
        throw new UnsupportedOperationException("Zaimplementuj odczyt rachunku źródłowego");
    }

    public String targetAccountId() {
        throw new UnsupportedOperationException("Zaimplementuj odczyt rachunku docelowego");
    }

    public long amountInCents() {
        throw new UnsupportedOperationException("Zaimplementuj odczyt kwoty");
    }

    public TransferStatus status() {
        throw new UnsupportedOperationException("Zaimplementuj odczyt stanu");
    }

    public Optional<TransferDecision> rejectionReason() {
        throw new UnsupportedOperationException("Zaimplementuj odczyt powodu odmowy");
    }

    public void accept() {
        throw new UnsupportedOperationException("Zaimplementuj przyjęcie dyspozycji");
    }

    public void reject(TransferDecision reason) {
        throw new UnsupportedOperationException("Zaimplementuj odrzucenie dyspozycji");
    }
}

