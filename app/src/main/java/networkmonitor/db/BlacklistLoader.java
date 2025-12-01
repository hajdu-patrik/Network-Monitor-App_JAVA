package networkmonitor.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loader class to populate the blacklist database from an external source.
 * Refactored to comply with SonarQube quality standards.
 */
public class BlacklistLoader {
    // Logger for logging information and errors
    private static final Logger LOGGER = Logger.getLogger(BlacklistLoader.class.getName());
    // URL of the source hosts file containing blocked domains
    private static final String SOURCE_URL = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/gambling-porn/hosts";
    // SQL insert statement to add entries to the netmonitor table
    private static final String INSERT_SQL = "INSERT INTO dbo.netmonitor (ip_address, website_name) VALUES (?, ?)";
    // Constant to avoid repeated extraction that needs to be edited in multiple places
    private static final String NULL_IP = "0.0.0.0";

    /**
     * Populates the database with blocked IP addresses and their corresponding domain names.
     * @param limit The maximum number of entries to add to the database
     * @param offset How many valid entries to skip from the beginning of the file.
     */
    public void populateDatabase(int limit, int offset) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int addedCount = 0;
        int skippedCount = 0;
        Set<String> processedIps = new HashSet<>();

        try {
            LOGGER.log(Level.INFO, "Download list from here: {0}", SOURCE_URL);
            URL url = URI.create(SOURCE_URL).toURL();
            
            DatabaseManager dbManager = new DatabaseManager();
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(INSERT_SQL);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null && addedCount < limit) {
                    if (isValidLine(line)) {
                        if (skippedCount < offset) {
                            skippedCount++;
                            continue;
                        }

                        if (processLine(line, pstmt, processedIps)) {
                            addedCount++;
                        }
                    }
                }
            }

            if (addedCount > 0) {
                LOGGER.info("Saving new entries to database...");
                pstmt.executeBatch();
                conn.commit();
                LOGGER.log(Level.INFO, "SUCCESS! Skipped first {0}, uploaded next {1} entries.", new Object[]{offset, addedCount});
            } else {
                LOGGER.info("No new unique entries found to add.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error populating database", e);
            rollbackQuietly(conn);
        } finally {
            closeQuietly(pstmt);
            closeQuietly(conn);
        }
    }

    /**
     * Checks if a line looks like a valid blocklist entry (starts with 0.0.0.0).
     */
    private boolean isValidLine(String line) {
        return line != null && line.startsWith(NULL_IP) && !line.equals("0.0.0.0 0.0.0.0");
    }


    /**
     * Processes a single line from the hosts file.
     * @return true if a new IP was successfully added to the batch, false otherwise.
     */
    private boolean processLine(String line, PreparedStatement pstmt, Set<String> processedIps) throws SQLException {
        // The lines in the hosts file look like this: "0.0.0.0 random.com"
        if (!line.startsWith(NULL_IP))
            return false;

        String[] parts = line.split("\\s+");
        if (parts.length < 2)
            return false;

        String domain = parts[1];
        if (NULL_IP.equals(domain))
            return false;

        try {
            // Domain -> IP resolution (This is the most important step!)
            InetAddress address = InetAddress.getByName(domain);
            String realIp = address.getHostAddress();

            if (!processedIps.contains(realIp)) {
                pstmt.setString(1, realIp);
                pstmt.setString(2, domain);
                pstmt.addBatch();
                
                processedIps.add(realIp);
                
                LOGGER.log(Level.INFO, "Blocked: {0} [{1}]", new Object[]{domain, realIp});
                return true;
            }
        } catch (Exception e) {
            // If a domain has already disappeared or cannot be resolved, we simply skip it.
        }
        return false;
    }

    /**
     * Helper method to rollback transaction quietly.
     */
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // Ignore rollback errors
            }
        }
    }

    /**
     * Helper method to close resources quietly.
     */
    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Ignore close errors
            }
        }
    }
}