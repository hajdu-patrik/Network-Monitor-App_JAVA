package networkmonitor.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.util.logging.Logger;

/**
 * The main application window for the Network Monitor.
 * Manages navigation between the Menu, Monitor, and IDS views using CardLayout.
 */
public class ApplicationFrame extends JFrame {
    // Logger
    private static final Logger LOGGER = Logger.getLogger(ApplicationFrame.class.getName());

    // Layout Components
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // View Identifiers
    private static final String VIEW_MENU = "Menu";
    private static final String VIEW_PACKET_MONITOR = "Packet monitoring";
    private static final String VIEW_BLACKLIST_MONITOR = "Blacklist monitoring";

    // Theme Fonts
    public static final String SANS_SERIF_FONT = "SansSerif";

    // Theme Colors
    public static final Color COLOR_BACKGROUND = new Color(44, 44, 44); // Dark Gray
    public static final Color COLOR_TEXT = Color.WHITE;
    public static final Color COLOR_PRIMARY = new Color(98, 0, 234);    // Deep Purple
    public static final Color COLOR_HOVER = new Color(124, 77, 255);    // Light Purple

    /**
     * Constructs the main window, initializes frame properties, and sets up views.
     */
    public ApplicationFrame() {
        initMainFrame();
        initViews();
        showView(VIEW_MENU);

        toFront();
        requestFocus();
    }


    /**
     * Initializes the main JFrame properties.
     */
    private void initMainFrame() {
        setTitle("Network Monitor Pro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);

        loadApplicationIcon();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(COLOR_BACKGROUND); // Set global background
        add(cardPanel);
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


    /**
     * Initializes and adds all views (Menu, Monitor, IDS) to the CardLayout.
     */
    private void initViews() {
        // Create the Menu Panel with actions to switch views
        MainMenuPanel mainMenu = new MainMenuPanel(
            e -> showView(VIEW_PACKET_MONITOR),
            e -> showView(VIEW_BLACKLIST_MONITOR)
        );

        // Add views to the card layout
        cardPanel.add(mainMenu, VIEW_MENU);

        PacketMonitorPanel packetMonitor = new PacketMonitorPanel(e -> showView(VIEW_MENU));
        BlacklistMonitorPanel blacklistMonitorPanel = new BlacklistMonitorPanel();

        cardPanel.add(packetMonitor, VIEW_PACKET_MONITOR);
        cardPanel.add(blacklistMonitorPanel, VIEW_BLACKLIST_MONITOR);
    }

    /**
     * Switches the active view in the CardLayout container.
     * @param viewName The name of the view to switch to.
     */
    private void showView(String viewName) {
        cardLayout.show(cardPanel, viewName);
    }


/**
     * A custom styled button class for the dark theme.
     * Supports custom colors and padding adjustments.
     */
    public static class FlatButton extends JButton {
        private final int widthPadding;
        private final int heightPadding;

        // Default constructor: default colors, default padding
        public FlatButton(String text) {
            this(text, COLOR_PRIMARY, COLOR_HOVER, 40, 45);
        }

        // Constructor with custom colors, default padding
        public FlatButton(String text, Color primaryColor, Color hoverColor) {
            this(text, primaryColor, hoverColor, 40, 45);
        }

        // Constructor with default colors, custom default padding
        public FlatButton(String text, int widthPadding, int heightPadding) {
            this(text, COLOR_PRIMARY, COLOR_HOVER, widthPadding, heightPadding);
        }

        // Full constructor with custom colors and custom padding
        public FlatButton(String text, Color primaryColor, Color hoverColor, int widthPadding, int heightPadding) {
            super(text);
            this.widthPadding = widthPadding;
            this.heightPadding = heightPadding;

            setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 16));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(primaryColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Add hover effect with custom colors
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverColor);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(primaryColor);
                }
            });
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();

            return new Dimension(d.width + widthPadding, heightPadding); 
        }
    }
}