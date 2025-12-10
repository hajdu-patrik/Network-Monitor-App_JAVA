package networkmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity class representing a blacklist entry in the database.
 * Maps to the "netmonitor" table by use of JPA annotations.
 */
@Entity
@Table(name = "netmonitor")
public class BlacklistEntry {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private int id;

    @Column(name = "ip_address", nullable = false, unique = true) // Column properties
    private String ipAddress;

    @Column(name = "website_name")
    private String websiteName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    public BlacklistEntry() {}

    /**
     * Constructor to create a BlacklistEntry
     * @param ipAddress
     * @param websiteName
     */
    public BlacklistEntry(String ipAddress, String websiteName) {
        this.ipAddress = ipAddress;
        this.websiteName = websiteName;
        this.createdAt = LocalDateTime.now(); // We set the date on the Java side
    }

    // Getters
    public String getIpAddress() { return ipAddress; }
    public String getWebsiteName() { return websiteName; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * String representation of the BlacklistEntry
     * @return Formatted string with IP address and website name
     */
    @Override
    public String toString() {
        return ipAddress + " (" + websiteName + ")";
    }
}