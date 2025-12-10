package networkmonitor.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import networkmonitor.model.BlacklistEntry;

/**
 * Loader class to populate the blacklist database from an external source or manual list.
 * Refactored to comply with SonarQube quality standards and JPA.
 */
public class BlacklistLoader {
    // Logger for logging information and errors
    private static final Logger LOGGER = Logger.getLogger(BlacklistLoader.class.getName());
    // Constant for lines indicating null IP
    private static final String NULL_IP = "0.0.0.0";

    /**
     * Populates the database with blocked IP addresses from a remote URL.
     * Checks against the existing database entries to avoid duplicates.
     * @param sourceUrl The URL of the hosts file.
     * @param limit Maximum number of new entries to process in this batch.
     * @param offset How many lines to skip from the beginning of the file.
     * @param existingEntries List of entries already in the DB (to prevent duplicates).
     * @return The number of entries successfully added to the database.
     */
    public int populateFromUrl(String sourceUrl, int limit, int offset, List<BlacklistEntry> existingEntries) {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.log(Level.INFO, "Loading from URL: {0} (Offset: {1})", new Object[]{sourceUrl, offset});
        
        Set<String> existingIps = existingEntries.stream()
            .map(BlacklistEntry::getIpAddress)
            .collect(Collectors.toSet());

        List<BlacklistEntry> batchToSave = new ArrayList<>();
        Set<String> currentBatchIps = new HashSet<>();
        int processedCount = 0;

        try {
            URL url = URI.create(sourceUrl).toURL();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                int currentLineIndex = 0;

                while ((line = reader.readLine()) != null) {
                    if (batchToSave.size() >= limit)
                        break;

                    if (isValidLine(line) && currentLineIndex >= offset)
                        processLine(line, batchToSave, currentBatchIps, existingIps);
                    
                    if (isValidLine(line))
                        currentLineIndex++;
                }
            }

            if (!batchToSave.isEmpty()) {
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.log(Level.INFO, "Saving {0} new entries to MSSQL...", batchToSave.size());

                new BlacklistDao().saveAll(batchToSave);
                LOGGER.info("Batch saved successfully.");
                processedCount = batchToSave.size();
            } else {
                LOGGER.info("No new unique entries found in this batch.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error populating database from URL", e);
        }
        return processedCount;
    }

    /**
     * Populate blacklist from a list of domain strings.
     * Resolves IPs and saves unique entries to the database.
     * @param domains List of domain strings
     */
    public void populateFromList(List<String> domains) {
        if (domains == null || domains.isEmpty())
            return;
        
        LOGGER.info("Loading from Manual List...");

        List<BlacklistEntry> batchToSave = new ArrayList<>();
        Set<String> currentBatchIps = new HashSet<>();

        for (String domain : domains) {
            try {
                InetAddress address = InetAddress.getByName(domain);
                String realIp = address.getHostAddress();

                if (currentBatchIps.contains(realIp))
                    continue;

                BlacklistEntry entry = new BlacklistEntry(realIp, domain);
                batchToSave.add(entry);
                currentBatchIps.add(realIp);
                
                LOGGER.log(Level.INFO, "Resolved Manual: {0} -> {1}", new Object[]{domain, realIp});

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not resolve manual domain: {0}", domain);
            }
        }

        if (!batchToSave.isEmpty()) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, "Saving {0} new manual entries to MSSQL...", batchToSave.size());

            new BlacklistDao().saveAll(batchToSave);
            LOGGER.info("Manual batch saved successfully.");
        }
    }

    /**
     * Check if a line from the source is valid for processing.
     * @param line Input line
     * @return true if valid, false otherwise
     */
    private boolean isValidLine(String line) {
        return line != null && line.startsWith(NULL_IP) && !line.equals("0.0.0.0 0.0.0.0");
    }

    /**
     * Process a single line from the source, resolve the domain to IP,
     * and prepare a BlacklistEntry if it's unique.
     * @param line Input line
     * @param batchToSave List to add new entries to
     * @param currentBatchIps Set of IPs already in the current batch
     * @param existingIps Set of IPs already in the database
     */
    private void processLine(String line, List<BlacklistEntry> batchToSave, Set<String> currentBatchIps, Set<String> existingIps) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2)
            return;

        String domain = parts[1];
        if (NULL_IP.equals(domain))
            return;

        try {
            InetAddress address = InetAddress.getByName(domain);
            String realIp = address.getHostAddress();

            if (existingIps.contains(realIp) || currentBatchIps.contains(realIp))
                return;

            BlacklistEntry entry = new BlacklistEntry(realIp, domain);
            batchToSave.add(entry);
            currentBatchIps.add(realIp);
            
            LOGGER.log(Level.INFO, "Resolved: {0} -> {1}", new Object[]{domain, realIp});
        } catch (Exception e) {
            // DNS error, skip
        }
    }
}