package isolation_levels.service;

import isolation_levels.model.Account;
import isolation_levels.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link AccountService}.
 * Tests transaction isolation levels and concurrency control.
 *
 * @author JetBrains Junie
 */
@SpringBootTest
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private static final String TEST_ACCOUNT_NUMBER = "TEST001";
    private static final String TEST_OWNER_NAME = "Test User";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    @Transactional
    public void setUp() {
        // Delete the test account if it exists
        accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER)
                .ifPresent(account -> accountRepository.delete(account));

        // Create a fresh test account
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_OWNER_NAME, INITIAL_BALANCE);
    }

    @Test
    @Transactional
    public void testCreateAccount() {
        // Given
        String accountNumber = "TEST002";
        String ownerName = "Another Test User";
        BigDecimal initialBalance = new BigDecimal("2000.00");

        // When
        Account account = accountService.createAccount(accountNumber, ownerName, initialBalance);

        // Then
        assertNotNull(account);
        assertNotNull(account.getId());
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(ownerName, account.getOwnerName());
        assertEquals(initialBalance, account.getBalance());
    }

    @Test
    @Transactional
    public void testGetAccountReadCommitted() {
        // When
        Optional<Account> accountOpt = accountService.getAccountReadCommitted(TEST_ACCOUNT_NUMBER);

        // Then
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_OWNER_NAME, account.getOwnerName());
        assertEquals(INITIAL_BALANCE, account.getBalance());
    }

    @Test
    @Transactional
    public void testUpdateBalanceReadCommitted() {
        // Given
        BigDecimal newBalance = new BigDecimal("1500.00");

        // When
        Optional<Account> accountOpt = accountService.updateBalanceReadCommitted(TEST_ACCOUNT_NUMBER, newBalance);

        // Then
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();
        assertEquals(newBalance, account.getBalance());

        // Verify the balance was updated in the database
        Optional<Account> updatedAccountOpt = accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER);
        assertTrue(updatedAccountOpt.isPresent());
        assertEquals(newBalance, updatedAccountOpt.get().getBalance());
    }

    @Test
    public void testOptimisticLocking() throws InterruptedException {
        // This test simulates concurrent updates to the same account
        // One of the updates should fail with OptimisticLockingFailureException

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> thread1Exception = new AtomicReference<>();
        AtomicReference<Exception> thread2Exception = new AtomicReference<>();

        // Thread 1: Update balance with optimistic lock
        executor.submit(() -> {
            try {
                latch.await(); // Wait for the signal to start
                accountService.updateBalanceWithOptimisticLock(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
            } catch (Exception e) {
                thread1Exception.set(e);
            }
        });

        // Thread 2: Update balance with optimistic lock
        executor.submit(() -> {
            try {
                latch.await(); // Wait for the signal to start
                accountService.updateBalanceWithOptimisticLock(TEST_ACCOUNT_NUMBER, new BigDecimal("200.00"));
            } catch (Exception e) {
                thread2Exception.set(e);
            }
        });

        // Start both threads simultaneously
        latch.countDown();

        // Wait for threads to complete
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        // At least one thread should have thrown an OptimisticLockingFailureException
        boolean optimisticLockingExceptionThrown = 
            (thread1Exception.get() instanceof OptimisticLockingFailureException) ||
            (thread2Exception.get() instanceof OptimisticLockingFailureException);

        assertTrue(optimisticLockingExceptionThrown, 
            "Expected at least one thread to throw OptimisticLockingFailureException");

        // Verify the account balance was updated by one of the threads
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER);
        assertTrue(accountOpt.isPresent());
        
        BigDecimal expectedBalance1 = INITIAL_BALANCE.add(new BigDecimal("100.00"));
        BigDecimal expectedBalance2 = INITIAL_BALANCE.add(new BigDecimal("200.00"));
        BigDecimal actualBalance = accountOpt.get().getBalance();
        
        assertTrue(
            actualBalance.equals(expectedBalance1) || actualBalance.equals(expectedBalance2),
            "Expected balance to be updated by one of the threads"
        );
    }

    @Test
    @Transactional
    public void testPessimisticLocking() {
        // When
        Optional<Account> accountOpt = accountService.updateBalanceWithPessimisticLock(
            TEST_ACCOUNT_NUMBER, new BigDecimal("300.00"));

        // Then
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();
        assertEquals(INITIAL_BALANCE.add(new BigDecimal("300.00")), account.getBalance());

        // Verify the balance was updated in the database
        Optional<Account> updatedAccountOpt = accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER);
        assertTrue(updatedAccountOpt.isPresent());
        assertEquals(INITIAL_BALANCE.add(new BigDecimal("300.00")), updatedAccountOpt.get().getBalance());
    }
}