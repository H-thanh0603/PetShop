package Context;

import DAO.PaymentTransactionDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shuts down the HikariCP connection pool when the application is undeployed.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Pool is initialized lazily in DBContext static block
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "payment-expiry-maintenance");
            t.setDaemon(true);
            return t;
        });
        PaymentTransactionDAO dao = new PaymentTransactionDAO();
        scheduler.scheduleAtFixedRate(dao::expirePendingTransactions, 1, 5, TimeUnit.MINUTES);
        System.out.println("[AppLifecycle] PetShop application started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        DBContext.shutdown();
        System.out.println("[AppLifecycle] PetShop application stopped. Connection pool closed.");
    }
}
