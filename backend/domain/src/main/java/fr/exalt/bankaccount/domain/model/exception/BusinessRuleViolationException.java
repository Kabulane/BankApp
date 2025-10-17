package fr.exalt.bankaccount.domain.model.exception;

// Regroupe les “erreurs métier récupérables”
public sealed class BusinessRuleViolationException extends DomainException
        permits CeilingExceededException, InsufficientFundsException {
    public BusinessRuleViolationException(String message) { super(message); }
}
