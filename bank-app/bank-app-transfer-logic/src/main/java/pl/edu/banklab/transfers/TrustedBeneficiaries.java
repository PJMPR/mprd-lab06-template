package pl.edu.banklab.transfers;

@FunctionalInterface
public interface TrustedBeneficiaries {
    boolean isTrusted(String targetAccountId);
}
