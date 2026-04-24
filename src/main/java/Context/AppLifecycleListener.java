package Context;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Shuts down the HikariCP connection pool when the application is undeployed.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Pool is initialized lazily in DBContext static block
        System.out.println("[AppLifecycle] PetShop application started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBContext.shutdown();
        System.out.println("[AppLifecycle] PetShop application stopped. Connection pool closed.");
    }
}
