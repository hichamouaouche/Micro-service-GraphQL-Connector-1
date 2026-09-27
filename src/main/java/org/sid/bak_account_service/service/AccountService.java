package org.sid.bak_account_service.service;

import org.sid.bak_account_service.dto.BankAccountRequestDTO;
import org.sid.bak_account_service.dto.BankAccountResponseDTO;
import org.sid.bak_account_service.entities.BankAccount;

public interface AccountService {
    BankAccountResponseDTO addAccount(BankAccountRequestDTO bankAccountDTO);

    BankAccountResponseDTO updateAccount(String id, BankAccountRequestDTO bankAccountDTO);
}
