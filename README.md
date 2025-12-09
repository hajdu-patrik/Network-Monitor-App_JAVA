![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java_Swing-E76F00?style=flat&logo=java&logoColor=white)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?style=flat&logo=gradle&logoColor=white)
![Pcap4J](https://img.shields.io/badge/Network-Pcap4J-blue?style=flat&logo=network&logoColor=white)
![Npcap](https://img.shields.io/badge/Driver-Npcap_WinPcap-blue?style=flat&logo=windows&logoColor=white)
![H2 Database](https://img.shields.io/badge/Database-H2_(Embedded)-yellow?style=flat&logo=database&logoColor=white)
![Hibernate](https://img.shields.io/badge/ORM-Hibernate-59666C?style=flat&logo=hibernate&logoColor=white)
![Architecture](https://img.shields.io/badge/Pattern-Layered_%2F_Service-purple?style=flat)
![Status](https://img.shields.io/badge/Status-Educational-lightgrey?style=flat)

# Network Monitor & IPS (Intrusion Prevention System)

## 📋 Project Overview

**Network Monitor Pro** is a sophisticated network analysis and security tool developed in **Java**. It goes beyond simple packet sniffing by integrating a proactive **Intrusion Prevention System (IPS)**.

The application captures real-time network traffic using the **Pcap4J** library (wrapping Npcap/libpcap) and analyzes packets against a dynamically updated blacklist. When a connection to a malicious IP is detected, the system flags it visually.

The project features a modern **Java Swing GUI**, robust multi-threaded architecture, and persistent data storage using an embedded **H2 Database**.

---

## 🚀 Key Features

### 🛡️ Active Security (IPS)
- **Real-time Threat Detection:** Automatically checks every outgoing packet's destination IP against a local blacklist database.
- **Visual Alerting:** Malicious traffic is highlighted in **RED** in the monitoring dashboard.

### 📡 Network Monitoring
- **Deep Packet Inspection:** Captures and parses TCP, UDP, and IPv4 headers.
- **Live Traffic Table:** Displays detailed information: Source/Dest IP, Protocol, Length, Ports, and Timestamps.
- **Smart Interface Selection:** Automatically detects and prefers physical network adapters (Wi-Fi 7, Ethernet) over virtual ones (Hyper-V, WAN Miniport).
- **Color Coded Traffic:**
    - **Blue:** TCP Traffic
    - **Yellow:** UDP Traffic
    - **Red:** Blocked/Malicious Traffic

### 💾 Data & Persistence
- **H2 Database Engine:** Embedded SQL database stores over **12,000+** blacklisted IP addresses.
- **Automatic Updates:** A background service fetches and updates the blacklist from reputable online sources (StevenBlack/hosts) on every startup.
- **Optimized Performance:** Uses in-memory caching for blacklist lookups to ensure zero latency during packet processing.

### 🖥️ UI/UX
- **Modern Swing Interface:** Dark theme with custom-styled "Flat" buttons and responsive layouts.
- **Control Panel:** Start, Stop, and Reset/Clear logs dynamically without restarting the application.
- **Background Service:** The protection engine runs as a daemon thread, protecting the system even when the GUI is navigated away from the monitor view.

---

## 🛠️ Technology Stack
* **Language:** Java (JDK 21)
* **Build System:** Gradle (Kotlin DSL)
* **GUI Framework:** Java Swing (Custom Look & Feel)
* **Network Lib:** Pcap4J (Packet Capture & Injection)
* **Database:** H2 Database (File-based storage)
* **ORM:** Hibernate (JPA) for entity management

---

## 📂 Project Structure

The project follows a clean **Layered Architecture** to separate concerns:
```
NetworkMonitor/
├── app/src/main/java/networkmonitor/
│ ├── Main.java
│ ├── db/
│ │ ├── BlacklistDao.java
│ │ ├── BlacklistFetching.java
│ │ ├── BlacklistLoader.java
│ │ └── DatabaseManager.java
│ ├── gui/
│ │ ├── ApplicationFrame.java
│ │ ├── MainMenuPanel.java
│ │ └── PacketMonitorPanel.java
│ ├── model/
│ │ ├── BlacklistEntry.java
| │ └── PacketInfo.java
│ └── service/
│ └── aptureService.java
└── build.gradle.kts
```

