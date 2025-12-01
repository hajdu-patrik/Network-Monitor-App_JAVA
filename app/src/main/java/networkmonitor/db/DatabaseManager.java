package networkmonitor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Instructions to set up the SQL Server database for this application:
 * STEP 1: Download SQL Server Express Management Studio (SSMS) from Microsoft's official website.
 * STEP 2: Install SSMS by following the installation wizard.
 * STEP 3: Open SSMS and connect to your local SQL Server instance. (localhost\SQLEXPRESS, Windows Authentication, encryption optional)
 * STEP 4: Create a new database/required table/user with CreateDatabase.sql script.
 * STEP 5: Ensure that TCP/IP protocol is enabled for SQL Server in SQL Server Configuration Manager.
 * STEP 6: Make sure your firewall allows connections to SQL Server on port 1433.
 */

// Manages database connections
public class DatabaseManager {
    // The connection string to the SQL Server database
    private static final String CONNECTION_URL = 
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=netmonitor;" +
        "user=java_user;" +
        "password=netMonitor123.@;" +
        "encrypt=false;" + 
        "trustServerCertificate=true;";

    /**
     * Creates and returns a new database connection.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }
}