package networkmonitor;

import javax.swing.SwingUtilities;
import networkmonitor.gui.ApplicationFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationFrame frame = new ApplicationFrame();
            frame.setVisible(true);
        });
    }
}
