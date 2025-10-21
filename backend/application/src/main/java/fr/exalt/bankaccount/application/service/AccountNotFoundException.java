package fr.exalt.bankaccount.application.service;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) { super(message); }
}
