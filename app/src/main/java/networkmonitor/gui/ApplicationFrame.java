package networkmonitor.gui;

import java.awt.*;
import javax.swing.*;
import java.util.logging.Logger;

public class ApplicationFrame extends JFrame {
    // Logger
    private static final Logger LOGGER = Logger.getLogger(ApplicationFrame.class.getName());


    /**
     * Constructs the main game window, initializes the frame properties,
     * sets up the views, and displays the main menu.
     */
    public ApplicationFrame() {
        initMainFrame();
    }

    /**
     * Initializes the main JFrame properties such as title, size, location, and layout manager.
     */
    private void initMainFrame() {
        setTitle("Network Monitor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        loadApplicationIcon();

        loadMenu();
    }

    /**
     * Loads the application icon from the resources folder and sets it as the window icon.
     */
    private void loadApplicationIcon() {
        try {
            java.net.URL iconURL = getClass().getResource("/icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
                setTaskbarIcon(icon.getImage());
            }
        } catch (Exception e) {
            LOGGER.warning("Icon load failed");
        }
    }

    /**
     * Sets the application icon in the system taskbar (OS-dependent).
     * @param image The image to use as the icon.
     */
    private void setTaskbarIcon(Image image) {
        if (Taskbar.isTaskbarSupported()) {
            try {
                Taskbar.getTaskbar().setIconImage(image);
            } catch (Exception e) {
                // Ignore platform specific errors
            }
        }
    }

    private void loadMenu() {
        JPanel menuPanel = new JPanel();
        JButton scanMode = new JButton("Scan mode");
    }
}