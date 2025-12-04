package networkmonitor.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import networkmonitor.gui.ApplicationFrame.FlatButton;

/**
 * Panel for Packet Monitoring mode.
 * Displays captured network packets in a scrollable table with control options.
 */
public class PacketMonitorPanel extends JPanel {
    private JTable packetTable;
    private DefaultTableModel tableModel;
    private boolean isCapturing = false;
    private JLabel statusLabel;

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

        // Green Start Button with Hover Effect
        FlatButton startBtn = new FlatButton("Start Capture", 
            new Color(0, 153, 76),  // Normal: Dark Green
            new Color(0, 204, 102),  // Hover: Light Green
            30,
            35
        );
        startBtn.addActionListener(e -> startCapture());

        // Red Stop Button with Hover Effect
        FlatButton stopBtn = new FlatButton("Stop Capture", 
            new Color(204, 0, 0),   // Normal: Dark Red
            new Color(255, 51, 51),  // Hover: Light Red
            30,
            35
        );
        stopBtn.addActionListener(e -> stopCapture());

        statusLabel = new JLabel("Status: None");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        leftControls.add(startBtn);
        leftControls.add(stopBtn);
        leftControls.add(Box.createHorizontalStrut(10));
        leftControls.add(statusLabel);

        // 2. RIGHT SIDE: Navigation
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightControls.setBackground(ApplicationFrame.COLOR_BACKGROUND);

        // Default Purple Button for Back
        FlatButton backBtn = new FlatButton("Back to Menu", 30, 35);
        backBtn.addActionListener(e -> {
            reset();
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

    private void initTable() {
        String[] columnNames = {"No.", "Time", "Source IP", "Destination IP", "Protocol", "Length", "Info"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        packetTable = new JTable(tableModel);
        
        // Dark Theme Styling
        packetTable.setBackground(new Color(60, 63, 65));
        packetTable.setForeground(Color.WHITE);
        packetTable.setGridColor(new Color(100, 100, 100));
        packetTable.setRowHeight(25);
        packetTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        packetTable.setFillsViewportHeight(true);

        JTableHeader header = packetTable.getTableHeader();
        header.setBackground(new Color(45, 45, 48));
        header.setForeground(ApplicationFrame.COLOR_PRIMARY);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ApplicationFrame.COLOR_PRIMARY));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        packetTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        packetTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        packetTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
    }

    
    private void startCapture() {
        if (!isCapturing) {
            isCapturing = true;
            statusLabel.setText("Status: Capturing...");
            statusLabel.setForeground(Color.GREEN);
            
            addPacketToTable(1, "12:00:01", "192.168.1.5", "142.250.180.174", "TCP", "64", "Syn Request");
        }
    }

    private void stopCapture() {
        if (isCapturing) {
            isCapturing = false;
            statusLabel.setText("Status: Stopped");
            statusLabel.setForeground(Color.RED);
        }
    }

    /**
     * Resets the panel state to default.
     * Clears the table and resets capture status.
     */
    private void reset() {
        // Stop capture logic
        isCapturing = false;
        statusLabel.setText("Status: None");
        statusLabel.setForeground(Color.LIGHT_GRAY);

        // Clear all rows from the table
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }
    }

    public void addPacketToTable(int number, String time, String src, String dst, String proto, String len, String info) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[]{number, time, src, dst, proto, len, info});
            packetTable.scrollRectToVisible(packetTable.getCellRect(packetTable.getRowCount() - 1, 0, true));
        });
    }
}