package isolation_levels.dto;

import isolation_levels.mapper.EntityDTOMapper;
import isolation_levels.model.Account;
import isolation_levels.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DTOs and the EntityDTOMapper.
 * This class tests the conversion between entities and DTOs,
 * and verifies that circular references are broken.
 */
public class DTOTest {

    @Test
    public void testAccountToDTO() {
        // Create an account
        Account account = new Account("ACC001", "John Doe", new BigDecimal("1000.00"));
        account.setId(1L);
        account.setVersion(0L);
        
        // Convert to DTO
        AccountDTO dto = EntityDTOMapper.toAccountDTO(account);
        
        // Verify conversion
        assertEquals(account.getId(), dto.getId());
        assertEquals(account.getAccountNumber(), dto.getAccountNumber());
        assertEquals(account.getOwnerName(), dto.getOwnerName());
        assertEquals(account.getBalance(), dto.getBalance());
        assertEquals(account.getVersion(), dto.getVersion());
    }
    
    @Test
    public void testTransactionToDTO() {
        // Create an account
        Account account = new Account("ACC001", "John Doe", new BigDecimal("1000.00"));
        account.setId(1L);
        
        // Create a transaction
        Transaction transaction = new Transaction(
            new BigDecimal("100.00"),
            "Test transaction",
            Transaction.TransactionType.CREDIT
        );
        transaction.setId(1L);
        transaction.setAccount(account);
        
        // Convert to DTO
        TransactionDTO dto = EntityDTOMapper.toTransactionDTO(transaction);
        
        // Verify conversion
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(transaction.getAccount().getId(), dto.getAccountId());
        assertEquals(transaction.getAccount().getAccountNumber(), dto.getAccountNumber());
        assertEquals(transaction.getAmount(), dto.getAmount());
        assertEquals(transaction.getDescription(), dto.getDescription());
        assertEquals(transaction.getTimestamp(), dto.getTimestamp());
        assertEquals(transaction.getType(), dto.getType());
    }
    
    @Test
    public void testCircularReferenceBreaking() {
        // Create an account
        Account account = new Account("ACC001", "John Doe", new BigDecimal("1000.00"));
        account.setId(1L);
        
        // Create a transaction
        Transaction transaction = new Transaction(
            new BigDecimal("100.00"),
            "Test transaction",
            Transaction.TransactionType.CREDIT
        );
        transaction.setId(1L);
        
        // Set up circular reference
        account.addTransaction(transaction);
        
        // Convert to DTOs
        AccountDTO accountDTO = EntityDTOMapper.toAccountDTO(account);
        TransactionDTO transactionDTO = EntityDTOMapper.toTransactionDTO(transaction);
        
        // Verify that the circular reference is broken
        // AccountDTO doesn't have transactions list
        assertNotNull(accountDTO);
        
        // TransactionDTO has accountId and accountNumber, but not the full account object
        assertNotNull(transactionDTO);
        assertEquals(account.getId(), transactionDTO.getAccountId());
        assertEquals(account.getAccountNumber(), transactionDTO.getAccountNumber());
    }
    
    @Test
    public void testListConversion() {
        // Create accounts
        List<Account> accounts = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Account account = new Account("ACC00" + i, "User " + i, new BigDecimal(i * 1000));
            account.setId((long) i);
            accounts.add(account);
        }
        
        // Convert to DTOs
        List<AccountDTO> dtos = EntityDTOMapper.toAccountDTOs(accounts);
        
        // Verify conversion
        assertEquals(accounts.size(), dtos.size());
        for (int i = 0; i < accounts.size(); i++) {
            assertEquals(accounts.get(i).getId(), dtos.get(i).getId());
            assertEquals(accounts.get(i).getAccountNumber(), dtos.get(i).getAccountNumber());
            assertEquals(accounts.get(i).getOwnerName(), dtos.get(i).getOwnerName());
            assertEquals(accounts.get(i).getBalance(), dtos.get(i).getBalance());
        }
    }
}