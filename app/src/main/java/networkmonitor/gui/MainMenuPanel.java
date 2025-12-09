package networkmonitor.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import networkmonitor.gui.ApplicationFrame.FlatButton;

/**
 * The main menu panel containing navigation buttons to different modes.
 * Styled with a dark theme and purple accents.
 */
public class MainMenuPanel extends JPanel {
    /**
     * Constructs the main menu panel.
     * @param packetListener Action to perform when Packet Monitoring is clicked.
     * @param blacklistListener Action to perform when Blacklist Monitoring is clicked.
     */
    public MainMenuPanel(ActionListener packetListener, ActionListener blacklistListener) {
        initComponents(packetListener, blacklistListener);
    }

    /**
     * Initializes components and layout.
     */
    private void initComponents(ActionListener packetListener, ActionListener blacklistListener) {
        // Set Panel Background
        setBackground(ApplicationFrame.COLOR_BACKGROUND);
        setLayout(new GridBagLayout());
        
        // Add padding around the entire panel content
        setBorder(new EmptyBorder(50, 50, 50, 50));

        GridBagConstraints layout = new GridBagConstraints();
        layout.insets = new Insets(15, 15, 15, 15);
        layout.fill = GridBagConstraints.HORIZONTAL;
        layout.gridx = 0;


        // --- Title Section ---
        JLabel titleLabel = new JLabel("NETWORK MONITOR");
        titleLabel.setFont(new Font(ApplicationFrame.SANS_SERIF_FONT, Font.BOLD, 32));
        titleLabel.setForeground(ApplicationFrame.COLOR_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        layout.gridy = 0;
        add(titleLabel, layout);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Secure your connection. Monitor traffic.");
        subtitleLabel.setFont(new Font(ApplicationFrame.SANS_SERIF_FONT, Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        layout.gridy = 1;
        layout.insets = new Insets(0, 15, 40, 15);
        add(subtitleLabel, layout);


        // --- Buttons Section ---
        // Reset insets for buttons
        layout.insets = new Insets(15, 15, 15, 15);

        FlatButton packetMonitoringBtn = new FlatButton("Packet Monitoring");
        packetMonitoringBtn.addActionListener(packetListener);
        layout.gridy = 2;
        add(packetMonitoringBtn, layout);

        FlatButton blacklistMonitoringBtn = new FlatButton("Blacklist Monitoring");
        blacklistMonitoringBtn.addActionListener(blacklistListener);
        layout.gridy = 3;
        add(blacklistMonitoringBtn, layout);

        FlatButton exitBtn = new FlatButton("Close Application", Color.DARK_GRAY, Color.GRAY);
        exitBtn.addActionListener(e -> System.exit(0));
        layout.gridy = 4;
        add(exitBtn, layout);
    }
}