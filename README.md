# FastDaemon 0.1.0 [ALPHA-2026-10] — Zero-Allocation, Ultra-Fast Loopback IPC and Background Process Daemon Framework for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastDaemon/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.0-green.svg)](https://jitpack.io/#andrestubbe/FastDaemon)

---

**⚡ Ultra-low latency loopback IPC, single-instance process lock, and resident background daemon manager for Java & GraalVM Native Image.**

**FastDaemon** is the lightweight background process daemon and IPC framework of the **FastJava** ecosystem. Engineered for CLI tools, warm-start native binaries, and long-running services, it enables sub-millisecond local process communication (< 1 ms round-trip) over loopback TCP without heavy serialization or complex RPC dependencies.

---

## Quick Start — Example

### 1. Server Process (Worker Daemon)

```java
import fastdaemon.FastDaemon;

public class MyDaemon {
    public static void main(String[] args) {
        FastDaemon.server("myapp")
            .port(47823)
            .onCommand((cmd, payload) -> {
                if ("ECHO".equalsIgnoreCase(cmd)) {
                    return "PONG: " + payload;
                }
                return "UNKNOWN";
            })
            .start(); // Blocks in daemon event loop
    }
}
```

### 2. Client Process (CLI Invocation)

```java
import fastdaemon.FastDaemon;
import fastdaemon.FastDaemonClient;

public class MyCLI {
    public static void main(String[] args) {
        FastDaemonClient client = FastDaemon.client("myapp").port(47823).build();

        if (client.ping()) {
            String response = client.send("ECHO", "Hello from CLI");
            System.out.println("Daemon responded: " + response);
        } else {
            System.out.println("Daemon is not active.");
        }
    }
}
```

---

## Key Features

- ⚡ **Sub-Millisecond IPC** — Loopback socket communication optimized for raw string and tab-delimited commands.
- 🔒 **Single-Instance File Lock** — Process-level FileLock preventing multiple conflicting daemon workers from running concurrently.
- 🆔 **Automatic PID Registry** — Tracks PID in `~/.<appId>/daemon.pid` for reliable monitoring and shutdown.
- 🪶 **Zero External Dependencies** — 100% pure Java 21+. Fully compatible with GraalVM Native Image (`Serial GC` friendly).
- 🔄 **Graceful Shutdown & Ping** — Built-in `PING`/`PONG` heartbeat and `SHUTDOWN` hook handling.

---

## Installation

### Maven (via JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastDaemon</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## License

MIT License — See [LICENSE](LICENSE) for details.
