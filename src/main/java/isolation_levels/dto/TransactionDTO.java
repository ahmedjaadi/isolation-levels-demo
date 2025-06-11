package isolation_levels.dto;

import isolation_levels.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Transaction entity.
 * This class is used to transfer transaction data to and from the API,
 * without exposing the internal structure of the Transaction entity.
 *
 * @author JetBrains Junie
 */
public class TransactionDTO {
    private Long id;
    private Long accountId;
    private String accountNumber; // Only include account number, not the full account
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
    private Transaction.TransactionType type;
    
    // Default constructor
    public TransactionDTO() {
    }
    
    /**
     * Creates a new TransactionDTO with the specified details.
     *
     * @param id the transaction ID
     * @param accountId the ID of the associated account
     * @param accountNumber the account number of the associated account
     * @param amount the transaction amount
     * @param description the transaction description
     * @param timestamp the transaction timestamp
     * @param type the transaction type (DEBIT or CREDIT)
     */
    public TransactionDTO(Long id, Long accountId, String accountNumber, BigDecimal amount, 
                         String description, LocalDateTime timestamp, Transaction.TransactionType type) {
        this.id = id;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.type = type;
    }
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Transaction.TransactionType getType() {
        return type;
    }
    
    public void setType(Transaction.TransactionType type) {
        this.type = type;
    }
}