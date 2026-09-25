# Network Monitor & IPS (Intrusion Prevention System)

![Java](https://img.shields.io/badge/Java-25%2B-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java_Swing-E76F00?style=flat&logo=java&logoColor=white)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?style=flat&logo=gradle&logoColor=white)
![Pcap4J](https://img.shields.io/badge/Network-Pcap4J-blue?style=flat&logo=network&logoColor=white)
![Npcap](https://img.shields.io/badge/Driver-Npcap_WinPcap-blue?style=flat&logo=windows&logoColor=white)
![MSSQL](https://img.shields.io/badge/Database-Microsoft_SQL_Server-CC2927?style=flat&logo=microsoft-sql-server&logoColor=white)
![Hibernate](https://img.shields.io/badge/ORM-Hibernate-59666C?style=flat&logo=hibernate&logoColor=white)
![Architecture](https://img.shields.io/badge/Pattern-Layered_%2F_Service-purple?style=flat)
![Status](https://img.shields.io/badge/Status-Educational-lightgrey?style=flat)
![License](https://img.shields.io/badge/License-All_Rights_Reserved-red?style=flat)

---

## 📋 Project Overview

**Network Monitor Pro** is a sophisticated network analysis and security tool developed in **Java**. It combines deep packet inspection with a proactive **Intrusion Prevention System (IPS)** logic to identify potential threats.

The application captures real-time network traffic using the **Pcap4J** library (wrapping Npcap/libpcap) and analyzes packets against a dynamically updated blacklist. When a connection to a malicious IP is detected, the system immediately flags it visually on the dashboard to alert the user.

The project features a modern **Java Swing GUI**, robust multi-threaded architecture, and enterprise-grade persistent data storage using **Microsoft SQL Server 2022** via Hibernate ORM.

---

## ✨ Key Features

### 🛡️ Active Security (IPS)
- **Real-time Threat Detection:** Automatically checks every outgoing packet's destination IP against a local blacklist database.
- **Visual Alerting:** Malicious traffic is instantly highlighted in **RED** in the monitoring dashboard for immediate visibility.

### 📡 Network Monitoring
- **Deep Packet Inspection:** Captures and parses TCP, UDP, and IPv4 headers.
- **Live Traffic Table:** Displays detailed information: Source/Dest IP, Protocol, Length, Ports, and Timestamps.
- **Smart Interface Selection:** Automatically detects and prefers physical network adapters (Wi-Fi 7, Ethernet) over virtual ones (Hyper-V, WAN Miniport).
- **Color Coded Traffic:**
    - **Blue:** TCP Traffic
    - **Yellow:** UDP Traffic
    - **Red:** Blocked/Malicious Traffic

### 💾 Data & Persistence
- **Microsoft SQL Server:** Enterprise-level database storing over **12,000+** blacklisted IP addresses.
- **Automatic Updates:** A background service (`BlacklistFetching`) fetches and updates the blacklist from reputable online sources (StevenBlack/hosts) on every startup.
- **State Persistence:** The application remembers the download progress of the blacklist file, ensuring efficient bandwidth usage and resuming updates seamlessly.
- **Optimized Performance:** Uses in-memory caching for blacklist lookups to ensure zero latency during packet processing.

### 🖥️ UI/UX
- **Modern Swing Interface:** Dark theme with custom-styled "Flat" buttons and responsive layouts.
- **Control Panel:** Start, Stop, and Reset/Clear logs dynamically without restarting the application.
- **Background Service:** The protection engine runs as a daemon thread, protecting the system even when the GUI is navigated away from the monitor view.

---

## 🛠️ Technology Stack
* **Language:** Java (JDK 25)
* **Build System:** Gradle (Kotlin DSL)
* **GUI Framework:** Java Swing (Custom Look & Feel)
* **Network Lib:** Pcap4J (Packet Capture)
* **Database:** Microsoft SQL Server 2022
* **ORM:** Hibernate (JPA) for entity management

---

## 📂 Project Structure
The project follows a clean **Layered Architecture** to separate concerns:
```
NetworkMonitor/
├── networkmonitor/
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
│ │ └── PacketInfo.java
│ └── service/
│ └── CaptureService.java
|
├── resources/
│ ├── META-INF/
│ │ └── persistence.xml
│ ├── icon.png
│ └── simplelogger.properties
|
├── blacklist_state.properties
└── build.gradle.kts
```

---

## ⚙️ Setup & Usage
### Prerequisites
1. **Java JDK 25** (or newer) installed and configured.
2. **Npcap (Windows)** or **libpcap (Linux)** installed.
   - **CRITICAL FOR WINDOWS USERS**: When installing Npcap, you **MUST** check the box: **"Install Npcap in WinPcap API-compatible Mode"** Without this option, the application will fail to load the native packet capture libraries.

### 1. Database Setup
Before running the application, ensure your local MSSQL instance is ready:
* **Host:** `localhost:1433`
* **Database Name:** `netmonitor`
* *Note: Ensure TCP/IP is enabled in SQL Server Configuration Manager.*

The app no longer ships a hardcoded login. Create your own SQL Server login (with a password of
your choice), grant it access to the `netmonitor` database, and provide the credentials either as
environment variables:
* `NETMON_DB_USER`, `NETMON_DB_PASSWORD` (optionally `NETMON_DB_URL` to override the connection string)

or via a local, git-ignored properties file. Copy `app/db.local.properties.example` to
`app/db.local.properties` and fill in your login:
```Properties
db.user=your_sql_login
db.password=change_me
```
Environment variables take precedence over `app/db.local.properties` when both are set.

### 2. Clone the Repository
```Bash
git clone https://github.com/hajdu-patrik/Network-Monitor-App_JAVA.git
cd Network-Monitor-App_JAVA
```

### 3. Build the Project
The project uses the Gradle Wrapper, so no manual Gradle installation is required.
```Bash
./gradlew build
```

### 4. Run the Application
**Windows (PowerShell/CMD):**
```Bash
./gradlew run
```

*Note: Depending on your system security settings, you might need to run the terminal as Administrator to grant access to the network card.*

---

## 📦 Releases

| Version | Description | Download |
| :--- | :--- | :--- |
| **v1.1.0** | **Network Monitor v1.1.0** - Latest stable release. Features real-time packet inspection, IPS threat detection, and persistent blacklist management via MSSQL. | [Source code (zip)](https://github.com/hajdu-patrik/Network-Monitor-App_JAVA/archive/refs/tags/v1.1.0.zip) |

---

## 📄 License

Copyright (c) Hajdú Patrik Zsolt. All rights reserved.

Published for demonstration and portfolio purposes only. Using any part of this code as a solution for an academic assignment is strictly prohibited. See [LICENSE.md](LICENSE.md) for the full terms.
