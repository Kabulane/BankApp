package fr.exalt.bankaccount.application.port.in;

import fr.exalt.bankaccount.application.dto.account.openaccount.OpenAccountResult;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountCommand;

public interface OpenCurrentAccountUseCase {
    OpenAccountResult handle(OpenCurrentAccountCommand openCurrentAccountCommand);
}
