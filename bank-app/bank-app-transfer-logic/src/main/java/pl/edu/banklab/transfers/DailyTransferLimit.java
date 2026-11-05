package pl.edu.banklab.transfers;

public final class DailyTransferLimit {
    private long remainingInCents;

    public DailyTransferLimit(long remainingInCents) {
        this.remainingInCents = remainingInCents;
    }

    public long remainingInCents() {
        return remainingInCents;
    }

    public void consume(long amountInCents) {
        remainingInCents -= amountInCents;
    }
}
