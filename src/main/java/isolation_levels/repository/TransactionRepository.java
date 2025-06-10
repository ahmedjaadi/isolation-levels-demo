package isolation_levels.repository;

import isolation_levels.model.Account;
import isolation_levels.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Transaction} entities.
 * Provides methods to interact with the transactions table in the database.
 *
 * @author JetBrains Junie
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions for a specific account.
     *
     * @param account the account to find transactions for
     * @return a list of transactions for the account
     */
    List<Transaction> findByAccount(Account account);

    /**
     * Finds all transactions for a specific account, ordered by timestamp (newest first).
     *
     * @param account the account to find transactions for
     * @return a list of transactions for the account, ordered by timestamp
     */
    List<Transaction> findByAccountOrderByTimestampDesc(Account account);

    /**
     * Finds all transactions for a specific account ID.
     *
     * @param accountId the ID of the account to find transactions for
     * @return a list of transactions for the account
     */
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
}