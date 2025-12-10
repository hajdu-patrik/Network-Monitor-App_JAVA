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
    // UI Components
    private JTable packetTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    
    // Transient because CaptureService is not Serializable
    private transient CaptureService captureService;
    
    /**
     * Constructs the Packet Monitor Panel.
     * @param backAction Action to perform when the "Back to Menu" button is clicked.
     * @param sharedService The global CaptureService instance from ApplicationFrame.
     */
    public PacketMonitorPanel(ActionListener backAction, CaptureService sharedService) {
        this.captureService = sharedService; // Dependency Injection

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
        startBtn.addActionListener(e -> resumeGuiUpdates());

        // --- RESET BUTTON (Yellow) ---
        FlatButton resetBtn = new FlatButton("Clear Log", 
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
        stopBtn.addActionListener(e -> stopProtection());

        statusLabel = new JLabel("Status: None");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Adding buttons in requested order: Start -> Reset -> Stop
        leftControls.add(startBtn);
        leftControls.add(resetBtn); 
        leftControls.add(stopBtn);
        leftControls.add(Box.createHorizontalStrut(10));
        leftControls.add(statusLabel);

        // 2. RIGHT SIDE: Navigation
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightControls.setBackground(ApplicationFrame.COLOR_BACKGROUND);

        // Default Purple Button for Back
        FlatButton backBtn = new FlatButton("Back to Menu", 30, 35);
        backBtn.addActionListener(e -> {
            pauseGuiUpdates(); // Pause updates to save resources
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
    }

    /**
     * Initializes the packet table with model, renderers, and styles.
     */
    private void initTable() {
        String[] columnNames = {"No.", "Time", "Source IP", "Destination IP", "Protocol", "Length", "Info", "Blocked"};
        
        // 1. Model Definition with Types (For Correct Sorting)
        tableModel = new DefaultTableModel(columnNames, 0) {
            /**
             * Overrides to make cells non-editable and define column classes.
             * @param row Row index
             * @param column Column index
             * @return false to make all cells non-editable
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            /**
             * Defines the class type for each column to ensure proper sorting.
             * @param columnIndex Index of the column
             * @return Class type of the column
             */
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5)
                    return Integer.class;

                if (columnIndex == 7)
                    return Boolean.class;

                return String.class;
            }
        };

        packetTable = new JTable(tableModel);
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
        packetTable.setDefaultRenderer(Integer.class, new PacketTableCellRenderer());

        // 5. Column Width Optimization
        TableColumnModel columnModel = packetTable.getColumnModel();
        
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(0).setMaxWidth(80);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(1).setMaxWidth(150);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(70);
        columnModel.getColumn(4).setMaxWidth(90);
        columnModel.getColumn(5).setPreferredWidth(60);
        columnModel.getColumn(5).setMaxWidth(80);
        columnModel.getColumn(6).setPreferredWidth(300);

        columnModel.getColumn(7).setMinWidth(0);
        columnModel.getColumn(7).setMaxWidth(0);
        columnModel.getColumn(7).setWidth(0);
    }

    /**
     * Resumes updating the GUI with packet data.
     */
    private void resumeGuiUpdates() {
        if (captureService != null) {
            captureService.setPacketListener(this::addPacketToTable);
            captureService.startCapturing();
            statusLabel.setText("Status: Capturing");
            statusLabel.setForeground(Color.GREEN);
        }
    }

    /**
     * Pauses GUI updates but keeps the capture service running in background.
     */
    private void pauseGuiUpdates() {
        if (captureService != null) {
            captureService.setPacketListener(null);
        }
    }

    /**
     * Completely stops the packet capture process.
     */
    private void stopProtection() {
        if (captureService != null) {
            captureService.stopCapturing();
            statusLabel.setText("Status: None");
            statusLabel.setForeground(Color.GRAY);
        }
    }

    /**
     * Clears the table and resets the packet counter.
     */
    private void clearData() {
        if (tableModel != null)
            tableModel.setRowCount(0);

        if (captureService != null)
            captureService.resetPacketCount();
    }

    /**
     * Adds a captured packet to the table in a thread-safe manner.
     * @param packet The PacketInfo object containing packet details.
     */
    public void addPacketToTable(PacketInfo packet) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[]{
                packet.getNumber(), packet.getTimestamp(), packet.getSourceIp(), packet.getDestIp(),
                packet.getProtocol(), packet.getLength(), packet.getInfo(), packet.isBlocked()
            });
            packetTable.scrollRectToVisible(packetTable.getCellRect(packetTable.getRowCount() - 1, 0, true));
        });
    }

    /**
     * Custom Cell Renderer to handle row coloring based on Protocol.
     */
    private static class PacketTableCellRenderer extends DefaultTableCellRenderer {
        private static final Color COLOR_TCP_BG = new Color(225, 240, 255);
        private static final Color COLOR_TCP_FG = Color.BLACK;
        private static final Color COLOR_UDP_BG = new Color(255, 255, 225);
        private static final Color COLOR_UDP_FG = Color.BLACK;
        private static final Color COLOR_DEFAULT_BG = new Color(60, 63, 65);
        private static final Color COLOR_DEFAULT_FG = Color.WHITE;
        private static final Color COLOR_BLOCKED_BG = new Color(255, 102, 102);
        private static final Color COLOR_BLOCKED_FG = Color.WHITE;

        /**
         * Overrides the default rendering to apply custom colors based on protocol type.
         * @param table The JTable.
         * @param value The cell value.
         * @param isSelected Whether the cell is selected.
         * @param hasFocus Whether the cell has focus.
         * @param row The row index.
         * @param column The column index.
         * @return The component used for rendering the cell.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (column == 0 || column == 4 || column == 5)
                setHorizontalAlignment(SwingConstants.CENTER);
            else
                setHorizontalAlignment(SwingConstants.LEFT);

            if (isSelected)
                return c;

            int modelRow = table.convertRowIndexToModel(row);
            
            Boolean isBlocked = (Boolean) table.getModel().getValueAt(modelRow, 7);
            String protocol = (String) table.getModel().getValueAt(modelRow, 4);

            if (Boolean.TRUE.equals(isBlocked)) {
                c.setBackground(COLOR_BLOCKED_BG);
                c.setForeground(COLOR_BLOCKED_FG);
            } else if ("TCP".equalsIgnoreCase(protocol)) {
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