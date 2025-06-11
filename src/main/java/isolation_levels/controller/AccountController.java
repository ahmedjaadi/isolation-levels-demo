package isolation_levels.controller;

import isolation_levels.dto.AccountDTO;
import isolation_levels.mapper.EntityDTOMapper;
import isolation_levels.model.Account;
import isolation_levels.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing accounts.
 * This controller provides endpoints for demonstrating transaction isolation levels.
 *
 * @author JetBrains Junie
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Creates a new account.
     *
     * @param requestBody the request body containing account details
     * @return the created account DTO
     */
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody Map<String, String> requestBody) {
        String accountNumber = requestBody.get("accountNumber");
        String ownerName = requestBody.get("ownerName");
        BigDecimal initialBalance = new BigDecimal(requestBody.get("initialBalance"));

        Account account = accountService.createAccount(accountNumber, ownerName, initialBalance);
        return ResponseEntity.ok(EntityDTOMapper.toAccountDTO(account));
    }

    /**
     * Retrieves all accounts.
     *
     * @return a list of all account DTOs
     */
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(EntityDTOMapper.toAccountDTOs(accounts));
    }

    /**
     * Retrieves an account by its account number using the specified isolation level.
     *
     * @param accountNumber the account number to search for
     * @param isolationLevel the isolation level to use (READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE)
     * @return the account DTO if found, or 404 if not found
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "READ_COMMITTED") String isolationLevel) {

        Optional<Account> accountOpt;

        switch (isolationLevel.toUpperCase()) {
            case "READ_UNCOMMITTED":
                accountOpt = accountService.getAccountReadUncommitted(accountNumber);
                break;
            case "READ_COMMITTED":
                accountOpt = accountService.getAccountReadCommitted(accountNumber);
                break;
            case "REPEATABLE_READ":
                accountOpt = accountService.getAccountRepeatableRead(accountNumber);
                break;
            case "SERIALIZABLE":
                accountOpt = accountService.getAccountSerializable(accountNumber);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return accountOpt.map(account -> ResponseEntity.ok(EntityDTOMapper.toAccountDTO(account)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an account's balance using the specified isolation level.
     *
     * @param accountNumber the account number to update
     * @param requestBody the request body containing the new balance
     * @param isolationLevel the isolation level to use (READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE)
     * @return the updated account DTO if found, or 404 if not found
     */
    @PutMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountDTO> updateBalance(
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> requestBody,
            @RequestParam(defaultValue = "READ_COMMITTED") String isolationLevel) {

        BigDecimal newBalance = new BigDecimal(requestBody.get("balance"));
        Optional<Account> accountOpt;

        switch (isolationLevel.toUpperCase()) {
            case "READ_UNCOMMITTED":
                accountOpt = accountService.updateBalanceReadUncommitted(accountNumber, newBalance);
                break;
            case "READ_COMMITTED":
                accountOpt = accountService.updateBalanceReadCommitted(accountNumber, newBalance);
                break;
            case "REPEATABLE_READ":
                accountOpt = accountService.updateBalanceRepeatableRead(accountNumber, newBalance);
                break;
            case "SERIALIZABLE":
                accountOpt = accountService.updateBalanceSerializable(accountNumber, newBalance);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return accountOpt.map(account -> ResponseEntity.ok(EntityDTOMapper.toAccountDTO(account)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an account's balance using optimistic locking.
     *
     * @param accountNumber the account number to update
     * @param requestBody the request body containing the amount to add
     * @return the updated account DTO if found, or 404 if not found
     */
    @PutMapping("/{accountNumber}/balance/optimistic")
    public ResponseEntity<AccountDTO> updateBalanceWithOptimisticLock(
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> requestBody) {

        BigDecimal amount = new BigDecimal(requestBody.get("amount"));
        Optional<Account> accountOpt = accountService.updateBalanceWithOptimisticLock(accountNumber, amount);

        return accountOpt.map(account -> ResponseEntity.ok(EntityDTOMapper.toAccountDTO(account)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an account's balance using pessimistic locking.
     *
     * @param accountNumber the account number to update
     * @param requestBody the request body containing the amount to add
     * @return the updated account DTO if found, or 404 if not found
     */
    @PutMapping("/{accountNumber}/balance/pessimistic")
    public ResponseEntity<AccountDTO> updateBalanceWithPessimisticLock(
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> requestBody) {

        BigDecimal amount = new BigDecimal(requestBody.get("amount"));
        Optional<Account> accountOpt = accountService.updateBalanceWithPessimisticLock(accountNumber, amount);

        return accountOpt.map(account -> ResponseEntity.ok(EntityDTOMapper.toAccountDTO(account)))
                .orElse(ResponseEntity.notFound().build());
    }
}
