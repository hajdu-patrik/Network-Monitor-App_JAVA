package networkmonitor.model;

/**
 * DTO (Data Transfer Object) class representing a captured network packet.
 * Uses the Builder Pattern to fix SonarLint S107 (Constructor has >7 parameters).
 */
public class PacketInfo {
    private final int number;
    private final String timestamp;
    private final String sourceIp;
    private final String destIp;
    private final String protocol;
    private final int length;
    private final String info;
    private final boolean isBlocked;

    // Private constructor, only accessible via Builder
    private PacketInfo(Builder builder) {
        this.number = builder.number;
        this.timestamp = builder.timestamp;
        this.sourceIp = builder.sourceIp;
        this.destIp = builder.destIp;
        this.protocol = builder.protocol;
        this.length = builder.length;
        this.info = builder.info;
        this.isBlocked = builder.isBlocked;
    }

    // Getters
    public int getNumber() { return number; }
    public String getTimestamp() { return timestamp; }
    public String getSourceIp() { return sourceIp; }
    public String getDestIp() { return destIp; }
    public String getProtocol() { return protocol; }
    public int getLength() { return length; }
    public String getInfo() { return info; }
    public boolean isBlocked() { return isBlocked; }

    /**
     * Builder class to construct PacketInfo objects cleanly.
     */
    public static class Builder {
        private int number;
        private String timestamp;
        private String sourceIp;
        private String destIp;
        private String protocol;
        private int length;
        private String info;
        private boolean isBlocked;

        // Setter methods for builder pattern
        public Builder number(int number) { this.number = number; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder sourceIp(String sourceIp) { this.sourceIp = sourceIp; return this; }
        public Builder destIp(String destIp) { this.destIp = destIp; return this; }
        public Builder protocol(String protocol) { this.protocol = protocol; return this; }
        public Builder length(int length) { this.length = length; return this; }
        public Builder info(String info) { this.info = info; return this; }
        public Builder isBlocked(boolean isBlocked) { this.isBlocked = isBlocked; return this; }

        // Builds the PacketInfo object
        public PacketInfo build() {
            return new PacketInfo(this);
        }
    }
}