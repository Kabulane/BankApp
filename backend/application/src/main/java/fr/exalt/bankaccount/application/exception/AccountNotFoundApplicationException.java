package fr.exalt.bankaccount.application.exception;

public class AccountNotFoundApplicationException extends RuntimeException {
    public AccountNotFoundApplicationException(String message) { super(message); }
}
