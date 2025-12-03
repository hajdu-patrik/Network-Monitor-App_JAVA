package networkmonitor.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import networkmonitor.model.BlacklistEntry;

/**
 * Service class responsible for fetching and synchronizing blacklist data.
 * It handles manual list loading and incremental downloads from web sources.
 */
public class BlacklistFetching implements Runnable {
    // Logger for logging information and errors
    private static final Logger LOGGER = Logger.getLogger(BlacklistFetching.class.getName());

    // In-memory cache for blacklist entries
    private static List<BlacklistEntry> blacklistCache;
    
    // Configuration constants
    private static final String SOURCE_URL = "https://raw.githubusercontent.com/StevenBlack/hosts/refs/heads/master/alternates/porn/hosts";
    private static final int BATCH_SIZE = 10;
    private static final int SLEEP_INTERVAL_MS = 100;


    /**
     * Returns the current in-memory blacklist cache.
     * @return List of BlacklistEntry objects.
     */
    public static synchronized List<BlacklistEntry> getBlacklistCache() {
        return blacklistCache;
    }

    /**
     * The entry point for the background thread.
     * Executes the data synchronization logic sequentially.
     */
    @Override
    public void run() {
        BlacklistLoader loader = new BlacklistLoader();
        BlacklistDao dao = new BlacklistDao();

        // 1. Initial Load
        refreshCache(dao);

        // 2. Load manual list
        loadManualList(loader, dao);

        // 3. Incremental load
        loadIncrementalWebList(loader, dao, 1110);
    }

    /**
     * Updates the in-memory cache with the latest data from the database.
     * @param dao The Data Access Object used to query the database.
     */
    private static void refreshCache(BlacklistDao dao) {
        blacklistCache = dao.loadAllEntries();
        
        if (blacklistCache != null)
            LOGGER.log(Level.INFO, "Cache updated. Database size: {0}", blacklistCache.size());
    }

    /**
     * Loads a predefined list of domains, checking against the cache to avoid duplicates.
     * @param loader The loader service to process DNS and DB insertion.
     * @param dao The DAO to refresh the cache after insertion.
     */
    private static void loadManualList(BlacklistLoader loader, BlacklistDao dao) {
        List<String> myCustomBlocks = Arrays.asList("pornhub.com", "xhamster.com", "xnxx.com");
        List<String> toAdd = new ArrayList<>();

        LOGGER.info("\n--- Checking manual list for duplicates... ---");

        for (String domain : myCustomBlocks)
            if (isElementInTheList(domain))
                LOGGER.log(Level.INFO, "Skipping: {0} is already in the database.", domain);
            else
                toAdd.add(domain);

        if (!toAdd.isEmpty()) {
            LOGGER.log(Level.INFO, "Adding {0} new manual entries...", toAdd.size());
            loader.populateFromList(toAdd);
            refreshCache(dao);
        } else {
            LOGGER.info("--- No new manual entries to add. ---");
        }
    }

    /**
     * Checks if a specific domain string exists in the current blacklist cache.
     * @param checkLink The domain name to check.
     * @return true if the domain is found in the cache, false otherwise.
     */
    private static boolean isElementInTheList(String checkLink) {
        if (blacklistCache == null || blacklistCache.isEmpty())
            return false;
        
        return blacklistCache.stream().anyMatch(entry -> entry.getWebsiteName().equalsIgnoreCase(checkLink));
    }

    /**
     * Continuously downloads new entries from the web source in batches.
     * Allows skipping already processed lines using an offset.
     * @param loader The loader service.
     * @param dao The DAO for cache updates.
     * @param startOffset The initial line offset to start reading from the file.
     */
    private static void loadIncrementalWebList(BlacklistLoader loader, BlacklistDao dao, int startOffset) {
        boolean keepLoading = true;
        int currentOffset = startOffset;

        LOGGER.log(Level.INFO, "\n --- Starting web download from file line offset: {0} ---", currentOffset);

        while (keepLoading) {
            LOGGER.info("Fetching next batch...");
            int added = loader.populateFromUrl(SOURCE_URL, BATCH_SIZE, currentOffset, blacklistCache);

            if (added > 0)
                refreshCache(dao);
            else
                LOGGER.info("Batch result: 0 new entries added (might be duplicates or EOF).");

            currentOffset += BATCH_SIZE;
            try { 
                Thread.sleep(SLEEP_INTERVAL_MS); 
            } catch (InterruptedException e) {
                LOGGER.warning("Background thread interrupted. Exiting loop.");
                Thread.currentThread().interrupt();
                keepLoading = false;
            }
        }
    }
}