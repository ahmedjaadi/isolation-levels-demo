package isolation_levels.controller;

import isolation_levels.dto.TransactionDTO;
import isolation_levels.mapper.EntityDTOMapper;
import isolation_levels.model.Transaction;
import isolation_levels.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing transactions.
 * This controller provides endpoints for demonstrating transaction isolation levels.
 *
 * @author JetBrains Junie
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new transaction for an account.
     *
     * @param requestBody the request body containing transaction details
     * @return the created transaction DTO if successful, or 404 if the account was not found
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody Map<String, String> requestBody) {
        String accountNumber = requestBody.get("accountNumber");
        BigDecimal amount = new BigDecimal(requestBody.get("amount"));
        String description = requestBody.get("description");
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(requestBody.get("type"));

        Optional<Transaction> transactionOpt = transactionService.createTransaction(accountNumber, amount, description, type);
        return transactionOpt.map(transaction -> ResponseEntity.ok(EntityDTOMapper.toTransactionDTO(transaction)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all transactions for an account using the specified isolation level.
     *
     * @param accountNumber the account number
     * @param isolationLevel the isolation level to use (READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE)
     * @return a list of transaction DTOs for the account
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "READ_COMMITTED") String isolationLevel) {

        List<Transaction> transactions;

        switch (isolationLevel.toUpperCase()) {
            case "READ_UNCOMMITTED":
                transactions = transactionService.getTransactionsReadUncommitted(accountNumber);
                break;
            case "READ_COMMITTED":
                transactions = transactionService.getTransactionsReadCommitted(accountNumber);
                break;
            case "REPEATABLE_READ":
                transactions = transactionService.getTransactionsRepeatableRead(accountNumber);
                break;
            case "SERIALIZABLE":
                transactions = transactionService.getTransactionsSerializable(accountNumber);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(EntityDTOMapper.toTransactionDTOs(transactions));
    }

    /**
     * Retrieves all transactions for an account, ordered by timestamp (newest first).
     *
     * @param accountNumber the account number
     * @return a list of transaction DTOs for the account, ordered by timestamp
     */
    @GetMapping("/account/{accountNumber}/recent")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactionsByAccount(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumberOrderByTimestampDesc(accountNumber);
        return ResponseEntity.ok(EntityDTOMapper.toTransactionDTOs(transactions));
    }

    /**
     * Transfers money between two accounts.
     *
     * @param requestBody the request body containing transfer details
     * @return 200 OK if the transfer was successful, 400 Bad Request otherwise
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody Map<String, String> requestBody) {
        String fromAccountNumber = requestBody.get("fromAccountNumber");
        String toAccountNumber = requestBody.get("toAccountNumber");
        BigDecimal amount = new BigDecimal(requestBody.get("amount"));

        boolean success = transactionService.transferMoney(fromAccountNumber, toAccountNumber, amount);

        if (success) {
            return ResponseEntity.ok("Transfer successful");
        } else {
            return ResponseEntity.badRequest().body("Transfer failed");
        }
    }
}
