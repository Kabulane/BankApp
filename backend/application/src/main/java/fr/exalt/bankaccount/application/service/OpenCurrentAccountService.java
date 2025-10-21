package fr.exalt.bankaccount.application.service;

public class OpenCurrentAccountService {

    private final AccountRepository repository;

    public OpenCurrentAccountService (AccountRepository repository) {
        this.repository = repository;
    }

    public OpenCurrentAccountResult handle(OpenCurrentAccountCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
