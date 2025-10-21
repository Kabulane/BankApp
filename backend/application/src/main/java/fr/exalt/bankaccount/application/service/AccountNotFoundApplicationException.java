package fr.exalt.bankaccount.application.service;

public class AccountNotFoundApplicationException extends RuntimeException {
    public AccountNotFoundApplicationException(String message) { super(message); }
}
