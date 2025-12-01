package networkmonitor.model;

import java.time.LocalDateTime;

/**
 * Represents a blocked domain and its IP address.
 * Maps to the dbo.netmonitor database table.
 */
public class BlacklistEntry {
    private int id;
    private String ipAddress;
    private String websiteName;
    private LocalDateTime createdAt;

    /**
     * Constructor with all fields
     * @param id Database ID
     * @param ipAddress Blocked IP address
     * @param websiteName Corresponding domain name
     * @param createdAt Timestamp of when the entry was created
     */
    public BlacklistEntry(int id, String ipAddress, String websiteName, LocalDateTime createdAt) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.websiteName = websiteName;
        this.createdAt = createdAt;
    }

    /**
     * Constructor without ID and createdAt
     * @param ipAddress
     * @param websiteName
     */
    public BlacklistEntry(String ipAddress, String websiteName) {
        this.ipAddress = ipAddress;
        this.websiteName = websiteName;
    }

    /**
     * Getters
     */
    public int getId() { return id; }
    public String getIpAddress() { return ipAddress; }
    public String getWebsiteName() { return websiteName; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * toString method for easy representation
     * @return String representation of the BlacklistEntry
     */
    @Override
    public String toString() {
        return ipAddress + " (" + websiteName + ")";
    }
}