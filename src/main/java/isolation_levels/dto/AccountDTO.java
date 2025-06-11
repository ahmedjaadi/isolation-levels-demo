package isolation_levels.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Account entity.
 * This class is used to transfer account data to and from the API,
 * without exposing the internal structure of the Account entity.
 *
 * @author JetBrains Junie
 */
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private Long version;
    
    // Default constructor
    public AccountDTO() {
    }
    
    /**
     * Creates a new AccountDTO with the specified details.
     *
     * @param id the account ID
     * @param accountNumber the unique account number
     * @param ownerName the name of the account owner
     * @param balance the balance of the account
     * @param version the version for optimistic locking
     */
    public AccountDTO(Long id, String accountNumber, String ownerName, BigDecimal balance, Long version) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.version = version;
    }
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}