package fr.exalt.bankaccount.domain.model.exception;

public final class InvariantViolationException extends DomainException {
    public InvariantViolationException(String message) { super(message); }
}
