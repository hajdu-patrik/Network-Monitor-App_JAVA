package networkmonitor.db;

import java.util.Map;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.logging.Level;

public class DatabaseManager {
    // EntityManagerFactory instance for managing database connections
    private static final EntityManagerFactory emf;

    // Logger for debugging and information
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());


    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Static initializer to set up the EntityManagerFactory.
     * This uses the settings defined in persistence.xml, with the database user,
     * password and (optionally) URL resolved by {@link DatabaseConfig} - environment
     * variables take precedence over the local db.local.properties file.
     */
    static {
        try {
            Map<String, String> overrides = DatabaseConfig.resolveOverrides(System.getenv(), DatabaseConfig.loadLocalProperties());
            emf = Persistence.createEntityManagerFactory("networkMonitorPU", overrides);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Database Connection Failed (MSSQL): {0}", ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Get a new EntityManager instance.
     * @return EntityManager instance
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Close the EntityManagerFactory when the application shuts down.
     */
    public static void close() {
        if (emf != null && emf.isOpen())
            emf.close();
    }
}