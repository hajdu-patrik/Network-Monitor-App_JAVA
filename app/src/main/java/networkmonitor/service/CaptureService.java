package networkmonitor.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import networkmonitor.model.PacketInfo;

/**
 * Service responsible for interacting with the Network Interface Card (NIC).
 * Handles packet capturing, parsing, and notifying listeners.
 */
public class CaptureService {
    private static final Logger LOGGER = Logger.getLogger(CaptureService.class.getName());
    private PcapHandle handle;
    private boolean keepRunning = false;
    private int packetCount = 0;
    
    // Callback to send data back to GUI
    private final Consumer<PacketInfo> packetListener;

    /**
     * Constructor for CaptureService.
     * @param packetListener Callback function to handle captured PacketInfo objects.
     */
    public CaptureService(Consumer<PacketInfo> packetListener) {
        this.packetListener = packetListener;
    }

    /**
     * Resets the internal packet count to zero.
     */
    public void resetPacketCount() {
        this.packetCount = 0;
    }

    /**
     * Starts the packet capturing on a separate thread.
     */
    public void startCapturing() {
        if (keepRunning)
            return; // Already running
        keepRunning = true;

        // Run capturing in a background thread to prevent freezing the GUI
        new Thread(this::captureLoop).start();
    }

    /**
     * Stops the capture loop and closes the handle.
     */
    public void stopCapturing() {
        keepRunning = false;
        if (handle != null && handle.isOpen()) {
            try {
                handle.breakLoop();
                handle.close();
            } catch (NotOpenException e) {
                LOGGER.log(Level.WARNING, "Error closing handle", e);
            }
        }
    }

    /**
     * Main capture loop running in a separate thread.
     * Continuously captures packets until stopped.
     */
    private void captureLoop() {
        try {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            if (allDevs == null || allDevs.isEmpty()) {
                LOGGER.severe("No NIFs found.");
                return;
            }
            
            PcapNetworkInterface nif = allDevs.stream()
                // 1. Basic exclusions (Loopback, empty)
                .filter(d -> !d.isLoopBack())
                .filter(d -> d.getDescription() != null)
                // 2. STRICT FILTERING: Exclude anything suspiciously virtual
                .filter(d -> {
                    String desc = d.getDescription().toLowerCase();
                    return !desc.contains("wan")       // Windows WAN Miniport
                        && !desc.contains("hyper-v")   // Microsoft Hyper-V
                        && !desc.contains("virtual")   // VirtualBox / VMware / Microsoft Virtual
                        && !desc.contains("loopback");
                })
                // 3. PRIORITY: Look for physical manufacturers or Wi-Fi
                .filter(d -> {
                    String desc = d.getDescription().toLowerCase();
                    return desc.contains("intel")      // Common laptop Wi-Fi
                        || desc.contains("killer")     // Gamer laptop Wi-Fi (common in Razer)
                        || desc.contains("realtek")    // Ethernet/Wi-Fi
                        || desc.contains("wi-fi")      // General Wi-Fi
                        || desc.contains("ethernet");  // General Ethernet
                })
                .findFirst()
                // 4. FALLBACK: If strict filtering excludes too much, take the first non-WAN, non-Loopback
                .orElse(
                    allDevs.stream()
                        .filter(d -> !d.isLoopBack())
                        .filter(d -> d.getDescription() != null && !d.getDescription().toLowerCase().contains("wan"))
                        .findFirst()
                        .orElse(allDevs.get(0))
                );

            LOGGER.log(Level.INFO, "Capturing on device: {0} | {1}", new Object[]{nif.getName(), nif.getDescription()});

            int snapshotLength = 65536; 
            int readTimeout = 10; 
            handle = nif.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);

            while (keepRunning && handle.isOpen()) {
                captureNextPacket();
            }

        } catch (PcapNativeException e) {
            LOGGER.log(Level.SEVERE, "PcapNativeException in capture loop", e);
        } finally {
            if (handle != null && handle.isOpen()) {
                handle.close();
            }
        }
    }

    /**
     * Helper method to capture a single packet.
     * Handles the specific exceptions related to packet fetching.
     */
    private void captureNextPacket() {
        try {
            Packet packet = handle.getNextPacketEx();
            if (packet != null) {
                processPacket(packet);
            }
        } catch (TimeoutException e) {
            // Timeout is expected in live capture, safe to ignore
        } catch (PcapNativeException | NotOpenException | java.io.EOFException e) {
            // EOF usually means the device was disconnected or reset
            LOGGER.log(Level.WARNING, "Error capturing packet (Connection might be lost): {0}", e.getMessage());
        } catch (Exception e) {
            // Catch-all for unexpected runtime errors to prevent thread death
            LOGGER.log(Level.SEVERE, "Unexpected error in packet capture loop", e);
        }
    }

    /**
     * Parses the raw packet and extracts logic for the GUI.
     * Handles null checks strictly to avoid NPEs on non-IPv4 packets.
     */
    @SuppressWarnings("null")
    private void processPacket(Packet packet) {
        // 1. Try to get IPv4 header
        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
        
        if (ipV4Packet == null)
            return;

        packetCount++;
        IpV4Packet.IpV4Header ipHeader = ipV4Packet.getHeader();

        String srcIp = ipHeader.getSrcAddr().getHostAddress();
        String dstIp = ipHeader.getDstAddr().getHostAddress();
        String protocol = "Other";
        String info = "Raw IP Data";

        // 2. Determine Transport Protocol (TCP/UDP)
        TcpPacket tcp = packet.get(TcpPacket.class);
        if (tcp != null) {
            protocol = "TCP";
            info = "SrcPort: " + tcp.getHeader().getSrcPort() + " -> DstPort: " + tcp.getHeader().getDstPort();
        } else {
            UdpPacket udp = packet.get(UdpPacket.class);
            if (udp != null) {
                protocol = "UDP";
                info = "SrcPort: " + udp.getHeader().getSrcPort() + " -> DstPort: " + udp.getHeader().getDstPort();
            }
        }

        PacketInfo packetInfo = new PacketInfo(
            packetCount,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
            srcIp,
            dstIp,
            protocol,
            packet.length(),
            info
        );

        packetListener.accept(packetInfo);
    }
}