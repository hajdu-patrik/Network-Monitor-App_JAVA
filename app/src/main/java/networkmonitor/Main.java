package networkmonitor;

import javax.swing.SwingUtilities;

import networkmonitor.db.BlacklistFetching;
import networkmonitor.gui.ApplicationFrame;

// Main entry point for the Network Monitor application.
public class Main {
    public static void main(String[] args) {
        /**
         * Start the GUI on the Event Dispatch Thread to ensure thread safety
         */
        SwingUtilities.invokeLater(() -> {
            ApplicationFrame frame = new ApplicationFrame();
            frame.setVisible(true);
        });

        /**
         * Start background data synchronization tasks in a separate thread
         * to prevents the GUI from freezing during network/database operations.
         */ 
        Thread backgroundThread = new Thread(new BlacklistFetching());
        backgroundThread.setDaemon(true); 
        backgroundThread.start();
    }
}