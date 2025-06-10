package isolation_levels.repository;

import isolation_levels.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository interface for {@link Account} entities.
 * Provides methods to interact with the accounts table in the database.
 *
 * @author JetBrains Junie
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Finds an account by its account number with a pessimistic read lock.
     * This is useful for demonstrating transaction isolation levels.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithPessimisticReadLock(@Param("accountNumber") String accountNumber);

    /**
     * Finds an account by its account number with a pessimistic write lock.
     * This is useful for demonstrating transaction isolation levels.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithPessimisticWriteLock(@Param("accountNumber") String accountNumber);
}