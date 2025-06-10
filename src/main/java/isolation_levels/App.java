package isolation_levels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Transaction Isolation Levels demo.
 * This application demonstrates different transaction isolation levels
 * and their effects on concurrent database operations.
 *
 * @author JetBrains Junie
 */
@SpringBootApplication
@EnableTransactionManagement
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
