package networkmonitor;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.logging.Logger;

import networkmonitor.db.BlacklistDao;
import networkmonitor.db.BlacklistLoader;
import networkmonitor.gui.ApplicationFrame;
import networkmonitor.model.BlacklistEntry;

// Main application entry point
public class Main {
    // Logger for logging information
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    // In-memory cache for blacklist entries
    protected static List<BlacklistEntry> blacklistCache;

    public static void main(String[] args) {
        // Check and load database in a separate thread to avoid blocking the GUI
        new Thread(() -> {
            BlacklistDao dao = new BlacklistDao();
        while(true) {
            // Check if the database is already populated
            List<BlacklistEntry> existingEntries = dao.loadAllEntries();
            int currentCount = existingEntries.size();
            
            LOGGER.info("Current database size: " + currentCount);
            
            // Always try to download 10 NEW entries (Incremental Update)
            LOGGER.info("Downloading NEXT 10 entries...");
            BlacklistLoader loader = new BlacklistLoader();
        
            // Limit = 10, Offset = how many we already have
            loader.populateDatabase(10, currentCount); 

            // Update the in-memory cache with the new data
            blacklistCache = dao.loadAllEntries();
            LOGGER.info("Blacklist cache updated. Total items: " + blacklistCache.size());
        }
        }).start();

        // Start the GUI
        SwingUtilities.invokeLater(() -> {
            ApplicationFrame frame = new ApplicationFrame();
            frame.setVisible(true);
        });
    }
}