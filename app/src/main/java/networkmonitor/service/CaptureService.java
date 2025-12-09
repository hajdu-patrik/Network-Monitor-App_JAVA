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
import networkmonitor.db.BlacklistFetching;
import networkmonitor.model.BlacklistEntry;

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
    // Nem final, mert cserélhető (GUI csatlakozik/lecsatlakozik)
    private Consumer<PacketInfo> packetListener;

    /**
     * Default constructor for background service.
     * Listener is initially null.
     */
    public CaptureService() {
        // No InjectionService needed anymore!
    }

    /**
     * Constructor for direct GUI usage (legacy).
     */
    public CaptureService(Consumer<PacketInfo> packetListener) {
        this.packetListener = packetListener;
    }

    /**
     * Sets the listener for GUI updates.
     */
    public void setPacketListener(Consumer<PacketInfo> packetListener) {
        this.packetListener = packetListener;
    }

    /**
     * Resets the internal packet count to zero.
     */
    public void resetPacketCount() {
        this.packetCount = 0;
    }

    /**
     * Starts the packet capturing process on a selected NIF.
     */
    public void startCapturing() {
        if (keepRunning) return; 

        keepRunning = true;
        new Thread(this::captureLoop).start();
    }

    /**
     * Stops the packet capturing process.
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
     * Main capture loop that continuously captures packets from the selected NIF.
     */
    private void captureLoop() {
        try {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            if (allDevs == null || allDevs.isEmpty()) {
                LOGGER.severe("No NIFs found.");
                return;
            }
            
            PcapNetworkInterface nif = allDevs.stream()
                .filter(d -> !d.isLoopBack())
                .filter(d -> d.getDescription() != null)
                .filter(d -> {
                    String desc = d.getDescription().toLowerCase();
                    return !desc.contains("wan") && !desc.contains("hyper-v") && !desc.contains("virtual") && !desc.contains("loopback");
                })
                .filter(d -> {
                    String desc = d.getDescription().toLowerCase();
                    return desc.contains("intel") || desc.contains("killer") || desc.contains("realtek") || desc.contains("wi-fi") || desc.contains("ethernet");
                })
                .findFirst()
                .orElse(allDevs.get(0));

            LOGGER.log(Level.INFO, "Capturing on device: {0} | {1}", new Object[]{nif.getName(), nif.getDescription()});

            int snapshotLength = 65536; 
            int readTimeout = 10; 
            handle = nif.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);

            while (keepRunning && handle.isOpen())
                captureNextPacket();

        } catch (PcapNativeException e) {
            LOGGER.log(Level.SEVERE, "PcapNativeException in capture loop", e);
        } finally {
            if (handle != null && handle.isOpen())
                handle.close();
        }
    }

    /**
     * Captures the next packet and processes it.
     */
    private void captureNextPacket() {
        try {
            Packet packet = handle.getNextPacketEx();
            if (packet != null)
                processPacket(packet);
        } catch (TimeoutException e) {
            // Expected
        } catch (PcapNativeException | NotOpenException | java.io.EOFException e) {
            LOGGER.log(Level.WARNING, "Error capturing packet: {0}", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in packet capture loop", e);
        }
    }

    /**
     * Processes a captured packet.
     */
    @SuppressWarnings("null")
    private void processPacket(Packet packet) {
        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
        if (ipV4Packet == null)
            return;

        packetCount++;
        IpV4Packet.IpV4Header ipHeader = ipV4Packet.getHeader();
        String srcIp = ipHeader.getSrcAddr().getHostAddress();
        String dstIp = ipHeader.getDstAddr().getHostAddress();
        
        // Protocol detection
        String protocol = "Other";
        String info = "Raw IP Data";
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
        boolean isBlocked = false;
        List<BlacklistEntry> blacklist = BlacklistFetching.getBlacklistCache();
        if (blacklist != null)
            isBlocked = blacklist.stream().anyMatch(entry -> entry.getIpAddress().equals(dstIp));
        
        if (packetListener != null) {
            PacketInfo packetInfo = new PacketInfo.Builder()
            .number(packetCount)
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")))
            .sourceIp(srcIp)
            .destIp(dstIp)
            .protocol(protocol)
            .length(packet.length())
            .info(info)
            .isBlocked(isBlocked)
            .build();

        packetListener.accept(packetInfo);
    }
}
}