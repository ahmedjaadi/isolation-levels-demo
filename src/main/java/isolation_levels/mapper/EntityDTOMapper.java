package isolation_levels.mapper;

import isolation_levels.dto.AccountDTO;
import isolation_levels.dto.TransactionDTO;
import isolation_levels.model.Account;
import isolation_levels.model.Transaction;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between entities and DTOs.
 * This class provides methods to convert Account entities to AccountDTOs
 * and Transaction entities to TransactionDTOs.
 *
 * @author JetBrains Junie
 */
public class EntityDTOMapper {

    /**
     * Converts an Account entity to an AccountDTO.
     *
     * @param account the Account entity to convert
     * @return the corresponding AccountDTO
     */
    public static AccountDTO toAccountDTO(Account account) {
        if (account == null) {
            return null;
        }
        
        return new AccountDTO(
            account.getId(),
            account.getAccountNumber(),
            account.getOwnerName(),
            account.getBalance(),
            account.getVersion()
        );
    }
    
    /**
     * Converts a list of Account entities to a list of AccountDTOs.
     *
     * @param accounts the list of Account entities to convert
     * @return the corresponding list of AccountDTOs
     */
    public static List<AccountDTO> toAccountDTOs(List<Account> accounts) {
        if (accounts == null) {
            return List.of();
        }
        
        return accounts.stream()
            .map(EntityDTOMapper::toAccountDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Converts a Transaction entity to a TransactionDTO.
     *
     * @param transaction the Transaction entity to convert
     * @return the corresponding TransactionDTO
     */
    public static TransactionDTO toTransactionDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return new TransactionDTO(
            transaction.getId(),
            transaction.getAccount() != null ? transaction.getAccount().getId() : null,
            transaction.getAccount() != null ? transaction.getAccount().getAccountNumber() : null,
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getTimestamp(),
            transaction.getType()
        );
    }
    
    /**
     * Converts a list of Transaction entities to a list of TransactionDTOs.
     *
     * @param transactions the list of Transaction entities to convert
     * @return the corresponding list of TransactionDTOs
     */
    public static List<TransactionDTO> toTransactionDTOs(List<Transaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        
        return transactions.stream()
            .map(EntityDTOMapper::toTransactionDTO)
            .collect(Collectors.toList());
    }
}