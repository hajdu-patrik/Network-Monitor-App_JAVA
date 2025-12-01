package networkmonitor.db;

import networkmonitor.model.BlacklistEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Blacklist operations.
 */
public class BlacklistDao {
    // Logger for logging information and errors
    private static final Logger LOGGER = Logger.getLogger(BlacklistDao.class.getName());
    // Database manager instance
    private final DatabaseManager dbManager;

    /**
     * Constructor initializes the DatabaseManager instance.
     */
    public BlacklistDao() {
        this.dbManager = new DatabaseManager();
    }

    /**
     * Checks if the blacklist table contains any data.
     * @return true if database has entries, false otherwise.
     */
    public boolean isDatabasePopulated() {
        String query = "SELECT COUNT(*) FROM dbo.netmonitor";
        try (Connection conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking database count", e);
        }
        return false;
    }

    /**
     * Loads all blacklist entries from the database into memory.
     * @return List of BlacklistEntry objects.
     */
    public List<BlacklistEntry> loadAllEntries() {
        List<BlacklistEntry> entries = new ArrayList<>();
        String query = "SELECT ID, ip_address, website_name, created_at FROM dbo.netmonitor";

        try (Connection conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                BlacklistEntry entry = new BlacklistEntry(
                    rs.getInt("ID"),
                    rs.getString("ip_address"),
                    rs.getString("website_name"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                );
                entries.add(entry);
            }
            LOGGER.log(Level.INFO, "Loaded {0} blacklist entries from database.", entries.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading blacklist entries", e);
        }
        return entries;
    }
}