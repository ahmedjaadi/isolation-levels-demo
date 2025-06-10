package isolation_levels.config;

import isolation_levels.model.Account;
import isolation_levels.model.Transaction;
import isolation_levels.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Configuration class to initialize sample data for the application.
 * This class creates sample accounts and transactions when the application starts.
 *
 * @author JetBrains Junie
 */
@Configuration
public class DataInitializer {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Creates sample data when the application starts.
     *
     * @return a CommandLineRunner that initializes the data
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Only initialize if no accounts exist
            if (accountRepository.count() == 0) {
                createSampleData();
            }
        };
    }

    /**
     * Creates sample accounts and transactions.
     */
    @Transactional
    public void createSampleData() {
        // Create accounts
        Account account1 = new Account("ACC001", "John Doe", new BigDecimal("1000.00"));
        Account account2 = new Account("ACC002", "Jane Smith", new BigDecimal("2000.00"));
        Account account3 = new Account("ACC003", "Bob Johnson", new BigDecimal("3000.00"));
        
        // Create transactions for account1
        Transaction deposit1 = new Transaction(
                new BigDecimal("500.00"),
                "Initial deposit",
                Transaction.TransactionType.CREDIT
        );
        Transaction withdrawal1 = new Transaction(
                new BigDecimal("200.00"),
                "ATM withdrawal",
                Transaction.TransactionType.DEBIT
        );
        
        // Create transactions for account2
        Transaction deposit2 = new Transaction(
                new BigDecimal("1000.00"),
                "Salary deposit",
                Transaction.TransactionType.CREDIT
        );
        Transaction payment2 = new Transaction(
                new BigDecimal("300.00"),
                "Bill payment",
                Transaction.TransactionType.DEBIT
        );
        
        // Create transactions for account3
        Transaction deposit3 = new Transaction(
                new BigDecimal("1500.00"),
                "Bonus deposit",
                Transaction.TransactionType.CREDIT
        );
        Transaction transfer3 = new Transaction(
                new BigDecimal("500.00"),
                "Transfer to ACC001",
                Transaction.TransactionType.DEBIT
        );
        
        // Add transactions to accounts
        account1.addTransaction(deposit1);
        account1.addTransaction(withdrawal1);
        account2.addTransaction(deposit2);
        account2.addTransaction(payment2);
        account3.addTransaction(deposit3);
        account3.addTransaction(transfer3);
        
        // Save accounts (transactions will be saved via cascade)
        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);
        
        System.out.println("Sample data initialized successfully.");
    }
}