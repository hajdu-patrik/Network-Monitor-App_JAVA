package networkmonitor.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import networkmonitor.gui.ApplicationFrame.FlatButton;
import networkmonitor.service.CaptureService;
import networkmonitor.model.PacketInfo;

/**
 * Panel for Packet Monitoring mode.
 * Displays captured network packets in a scrollable table with control options.
 */
public class PacketMonitorPanel extends JPanel {
    private JTable packetTable;
    private DefaultTableModel tableModel;
    private boolean isCapturing = false;
    private JLabel statusLabel;
    private transient CaptureService captureService;
    
    /**
     * Constructs the Packet Monitor Panel.
     * @param backAction Action to perform when the "Back to Menu" button is clicked.
     */
    public PacketMonitorPanel(ActionListener backAction) {
        setLayout(new BorderLayout());
        setBackground(ApplicationFrame.COLOR_BACKGROUND);

        // --- TOP: Main Toolbar Container ---
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(ApplicationFrame.COLOR_BACKGROUND);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        // 1. LEFT SIDE: Capture Controls
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        leftControls.setBackground(ApplicationFrame.COLOR_BACKGROUND);

        // --- START BUTTON (Green) ---
        FlatButton startBtn = new FlatButton("Start Capture", 
            new Color(0, 153, 76),
            new Color(0, 204, 102),
            30, 35
        );
        startBtn.addActionListener(e -> startCapture());

        // --- RESET BUTTON (Yellow) ---
        FlatButton resetBtn = new FlatButton("Reset", 
            new Color(255, 140, 0),
            new Color(255, 165, 0),
            30, 35
        );
        resetBtn.addActionListener(e -> clearData());

        // --- STOP BUTTON (Red) ---
        FlatButton stopBtn = new FlatButton("Stop Capture", 
            new Color(204, 0, 0),
            new Color(255, 51, 51),
            30, 35
        );
        stopBtn.addActionListener(e -> stopCapture());

        statusLabel = new JLabel("Status: None");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Adding buttons in requested order: Start -> Reset -> Stop
        leftControls.add(startBtn);
        leftControls.add(resetBtn); // Beillesztve középre
        leftControls.add(stopBtn);
        leftControls.add(Box.createHorizontalStrut(10));
        leftControls.add(statusLabel);

        // 2. RIGHT SIDE: Navigation
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightControls.setBackground(ApplicationFrame.COLOR_BACKGROUND);

        // Default Purple Button for Back
        FlatButton backBtn = new FlatButton("Back to Menu", 30, 35);
        backBtn.addActionListener(e -> {
            resetFullState();
            backAction.actionPerformed(e);
        });

        rightControls.add(backBtn);

        toolbar.add(leftControls, BorderLayout.WEST);
        toolbar.add(rightControls, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // --- CENTER: Packet Table ---
        initTable();
        JScrollPane scrollPane = new JScrollPane(packetTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ApplicationFrame.COLOR_BACKGROUND);
        
        add(scrollPane, BorderLayout.CENTER);

        // Initialize Capture Service
        this.captureService = new CaptureService(this::addPacketToTable);
    }

    /**
     * Initializes the packet table with model, renderers, and styles.
     */
    private void initTable() {
        String[] columnNames = {"No.", "Time", "Source IP", "Destination IP", "Protocol", "Length", "Info"};
        
        // 1. Model Definition with Types (For Correct Sorting)
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5) {
                    return Integer.class; // No. and Length are numbers
                }
                return String.class; // Others are text
            }
        };

        packetTable = new JTable(tableModel);
        
        // 2. Enable Sorting
        packetTable.setAutoCreateRowSorter(true);

        // 3. Visual Styling (Dark Theme Base)
        packetTable.setBackground(new Color(60, 63, 65));
        packetTable.setForeground(Color.WHITE);
        packetTable.setGridColor(new Color(100, 100, 100));
        packetTable.setRowHeight(25);
        packetTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        packetTable.setFillsViewportHeight(true);

        JTableHeader header = packetTable.getTableHeader();
        header.setBackground(new Color(45, 45, 48));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));

        // 4. Custom Renderer for Color Coding (TCP/UDP)
        packetTable.setDefaultRenderer(Object.class, new PacketTableCellRenderer());
        packetTable.setDefaultRenderer(Integer.class, new PacketTableCellRenderer()); // Apply to Numbers too

        // 5. Column Width Optimization
        TableColumnModel columnModel = packetTable.getColumnModel();

        // No. (ID) - Short
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(0).setMaxWidth(80);

        // Time - Medium
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(1).setMaxWidth(150);

        // IPs - Medium
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(120);

        // Protocol - Short
        columnModel.getColumn(4).setPreferredWidth(60);
        columnModel.getColumn(4).setMaxWidth(80);

        // Length - Short
        columnModel.getColumn(5).setPreferredWidth(60);
        columnModel.getColumn(5).setMaxWidth(80);

        // Info - Long (Takes remaining space)
        columnModel.getColumn(6).setPreferredWidth(300);
    }

    /**
     * Starts the packet capture process.
     */
    private void startCapture() {
        if (!isCapturing) {
            isCapturing = true;
            statusLabel.setText("Status: Capturing...");
            statusLabel.setForeground(Color.GREEN);
            
            captureService.startCapturing();
        }
    }

    /**
     * Stops the packet capture process.
     */
    private void stopCapture() {
        if (isCapturing) {
            isCapturing = false;
            statusLabel.setText("Status: Stopped");
            statusLabel.setForeground(Color.RED);

            captureService.stopCapturing();
        }
    }

    /**
     * Clears the table and resets the packet counter WITHOUT stopping the capture.
     * This is used by the "Reset" button.
     */
    private void clearData() {
        if (tableModel != null)
            tableModel.setRowCount(0);

        if (captureService != null)
            captureService.resetPacketCount();
    }

    /**
     * Resets the entire panel state (stops capture, clears data).
     * This is used when navigating back to the menu.
     */
    private void resetFullState() {
        if (isCapturing)
            stopCapture();
        
        statusLabel.setText("Status: None");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        
        clearData(); // Reuse clear logic
    }

    /**
     * Adds a captured packet to the table.
     * @param packet The PacketInfo object containing packet details.
     */
    public void addPacketToTable(PacketInfo packet) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[]{
                packet.getNumber(),
                packet.getTimestamp(),
                packet.getSourceIp(),
                packet.getDestIp(),
                packet.getProtocol(),
                packet.getLength(),
                packet.getInfo()
            });
            // Auto-scroll logic: only if user hasn't scrolled up
            // (Simpler version: always scroll to bottom)
            packetTable.scrollRectToVisible(packetTable.getCellRect(packetTable.getRowCount() - 1, 0, true));
        });
    }

    /**
     * Custom Cell Renderer to handle row coloring based on Protocol.
     */
    private static class PacketTableCellRenderer extends DefaultTableCellRenderer {
        // Colors for protocols (Pale/Pastel versions for readability)
        private static final Color COLOR_TCP_BG = new Color(225, 240, 255); // Pale Blue
        private static final Color COLOR_TCP_FG = Color.BLACK;              // Text needs to be dark on pale bg
        
        private static final Color COLOR_UDP_BG = new Color(255, 255, 225); // Pale Yellow
        private static final Color COLOR_UDP_FG = Color.BLACK;

        private static final Color COLOR_DEFAULT_BG = new Color(60, 63, 65); // Dark Theme Default
        private static final Color COLOR_DEFAULT_FG = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 1. Center align specific columns
            if (column == 0 || column == 4 || column == 5) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            // 2. Handle Selection (Don't override selection color)
            if (isSelected) {
                // Keep default selection colors (usually blueish)
                return c;
            }

            // 3. Get Protocol value to determine color
            // IMPORTANT: We must convert view row index to model row index because of sorting!
            int modelRow = table.convertRowIndexToModel(row);
            String protocol = (String) table.getModel().getValueAt(modelRow, 4); // Column 4 is Protocol

            if ("TCP".equalsIgnoreCase(protocol)) {
                c.setBackground(COLOR_TCP_BG);
                c.setForeground(COLOR_TCP_FG);
            } else if ("UDP".equalsIgnoreCase(protocol)) {
                c.setBackground(COLOR_UDP_BG);
                c.setForeground(COLOR_UDP_FG);
            } else {
                c.setBackground(COLOR_DEFAULT_BG);
                c.setForeground(COLOR_DEFAULT_FG);
            }

            return c;
        }
    }
}