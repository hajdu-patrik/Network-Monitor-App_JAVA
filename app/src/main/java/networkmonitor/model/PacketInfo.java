package networkmonitor.model;

/**
 * DTO (Data Transfer Object) class representing a captured network packet.
 * It transforms raw Pcap4J packet data into a format suitable for the GUI table.
 */
public class PacketInfo {
    private final int number;
    private final String timestamp;
    private final String sourceIp;
    private final String destIp;
    private final String protocol;
    private final int length;
    private final String info;

    public PacketInfo(int number, String timestamp, String sourceIp, String destIp, String protocol, int length, String info) {
        this.number = number;
        this.timestamp = timestamp;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.protocol = protocol;
        this.length = length;
        this.info = info;
    }

    // Getters
    public int getNumber() { return number; }
    public String getTimestamp() { return timestamp; }
    public String getSourceIp() { return sourceIp; }
    public String getDestIp() { return destIp; }
    public String getProtocol() { return protocol; }
    public int getLength() { return length; }
    public String getInfo() { return info; }
}