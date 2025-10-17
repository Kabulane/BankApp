package fr.exalt.bankaccount.domain.model.exception;

public sealed class DomainException extends RuntimeException
        permits InvariantViolationException, BusinessRuleViolationException {
    public DomainException(String message) { super(message); }
    public DomainException(String message, Throwable cause) { super(message, cause); }
}
