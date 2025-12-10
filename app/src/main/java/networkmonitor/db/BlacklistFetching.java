package networkmonitor.db;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import networkmonitor.model.BlacklistEntry;

/**
 * Service class responsible for fetching and synchronizing blacklist data.
 * It handles manual list loading and incremental downloads from web sources.
 * Now supports state persistence to resume from the last saved offset.
 */
public class BlacklistFetching implements Runnable {
    // Logger for logging information and errors
    private static final Logger LOGGER = Logger.getLogger(BlacklistFetching.class.getName());

    // In-memory cache for blacklist entries
    private static List<BlacklistEntry> blacklistCache;
    
    // Configuration constants
    private static final String SOURCE_URL = "https://raw.githubusercontent.com/StevenBlack/hosts/refs/heads/master/alternates/porn/hosts";
    private static final int BATCH_SIZE = 50;
    private static final int SLEEP_INTERVAL_MS = 100;
    
    // State persistence file
    private static final String STATE_FILE = "blacklist_state.properties";
    private static final String KEY_OFFSET = "last_processed_offset";

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

        // 3. Load saved offset (State Restoration)
        int startOffset = loadSavedOffset();
        LOGGER.log(Level.INFO, "Resuming download from last saved offset: {0}", startOffset);

        // 4. Incremental load starting from saved position
        loadIncrementalWebList(loader, dao, startOffset);
    }

    /**
     * Updates the in-memory cache with the latest data from the database.
     */
    private static void refreshCache(BlacklistDao dao) {
        blacklistCache = dao.loadAllEntries();
        if (blacklistCache != null)
            LOGGER.log(Level.INFO, "Cache updated. Database size: {0}", blacklistCache.size());
    }

    /**
     * Loads a predefined list of domains.
     */
    private static void loadManualList(BlacklistLoader loader, BlacklistDao dao) {
        List<String> myCustomBlocks = Arrays.asList("pornhub.com", "xhamster.com", "xnxx.com");
        List<String> toAdd = new ArrayList<>();

        LOGGER.info("\n--- Checking manual list for duplicates... ---");

        for (String domain : myCustomBlocks)
            if (!isElementInTheList(domain))
                toAdd.add(domain);
            else
                LOGGER.log(Level.INFO, "Skipping: {0} is already in the database.", domain);

        if (!toAdd.isEmpty()) {
            LOGGER.log(Level.INFO, "Adding {0} new manual entries...", toAdd.size());
            loader.populateFromList(toAdd);
            refreshCache(dao);
        } else {
            LOGGER.info("--- No new manual entries to add. ---");
        }
    }

    private static boolean isElementInTheList(String checkLink) {
        if (blacklistCache == null || blacklistCache.isEmpty())
            return false;
        return blacklistCache.stream().anyMatch(entry -> entry.getWebsiteName().equalsIgnoreCase(checkLink));
    }

    /**
     * Continuously downloads new entries from the web source in batches.
     * Saves the current progress (offset) after each batch.
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
            
            // Advance offset
            currentOffset += BATCH_SIZE;
            
            // SAVE STATE: Save the new offset immediately so we don't lose progress
            saveOffset(currentOffset);

            try { 
                Thread.sleep(SLEEP_INTERVAL_MS); 
            } catch (InterruptedException e) {
                LOGGER.warning("Background thread interrupted. Exiting loop.");
                Thread.currentThread().interrupt();
                keepLoading = false;
            }
        }
    }

    /**
     * Loads the last processed line number from the properties file.
     * @return The saved offset, or 0 if no file exists.
     */
    private static int loadSavedOffset() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(STATE_FILE)) {
            props.load(in);
            String offsetStr = props.getProperty(KEY_OFFSET, "0");
            return Integer.parseInt(offsetStr);
        } catch (IOException | NumberFormatException e) {
            LOGGER.info("No saved state found or invalid file. Starting from offset 0.");
            return 0; // Default start
        }
    }

    /**
     * Saves the current line number to the properties file.
     * @param offset The current offset to save.
     */
    private static void saveOffset(int offset) {
        Properties props = new Properties();
        props.setProperty(KEY_OFFSET, String.valueOf(offset));
        try (FileOutputStream out = new FileOutputStream(STATE_FILE)) {
            props.store(out, "Network Monitor Blacklist State");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save processing state!", e);
        }
    }
}