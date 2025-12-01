package networkmonitor;

import javax.swing.SwingUtilities;

import networkmonitor.db.BlacklistLoader;
import networkmonitor.gui.ApplicationFrame;

public class Main {
    public static void main(String[] args) {
        // Load blacklist in a separate thread to avoid blocking the GUI
        new Thread(() -> {
            BlacklistLoader loader = new BlacklistLoader();
            loader.populateDatabase(100); 
        }).start();

        // Launch the GUI
        SwingUtilities.invokeLater(() -> {
            ApplicationFrame frame = new ApplicationFrame();
            frame.setVisible(true);
        });
    }
}
