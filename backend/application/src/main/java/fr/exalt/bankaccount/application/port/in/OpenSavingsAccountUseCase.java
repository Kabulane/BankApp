package fr.exalt.bankaccount.application.port.in;

import fr.exalt.bankaccount.application.dto.account.openaccount.OpenAccountResult;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountCommand;

public interface OpenSavingsAccountUseCase {
    OpenAccountResult handle(OpenSavingsAccountCommand openSavingsAccountCommand);
}
