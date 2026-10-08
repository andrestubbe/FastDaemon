# FastDaemon 0.1.0 [ALPHA-2026-10] — Zero-Allocation, Ultra-Fast Loopback IPC and Background Process Daemon Framework for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastDaemon/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.0-green.svg)](https://jitpack.io/#andrestubbe/FastDaemon)

---

**⚡ Ultra-low latency loopback IPC, single-instance process lock, and resident background daemon manager for Java.**

**FastDaemon** is the lightweight background process daemon and IPC framework of the **FastJava** ecosystem. Engineered for CLI tools, resident warm-start background workers, and long-running utility services, it enables sub-millisecond local process communication (< 1 ms round-trip) over loopback TCP without heavy serialization, HTTP overhead, or bloated RPC dependencies.

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

## Table of Contents

- [Why FastDaemon?](#why-fastdaemon)
- [Key Features](#key-features)
- [Architecture & Pipeline](#architecture--pipeline)
- [Performance & Benchmarks](#performance--benchmarks)
- [Installation](#installation)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastDaemon?

Existing Java solutions for background tasks and local inter-process communication rely on heavy server stacks or fragile file polling:

1. **HTTP / REST Server Overhead**: Spinning up embedded Jetty, Netty, or Spring Boot for a local CLI helper pulls dozens of megabytes, adds 500–2500 ms startup time, and consumes significant memory.
2. **Heavyweight RPC / gRPC**: Serialization frameworks like Protobuf or JSON RPC require complex code generators and heap allocation churn for simple ping-pong or command execution.
3. **Flaky Concurrency & Zombie Processes**: Without OS-level locks and PID tracking, stale background processes accumulate and lock network ports or state files.

| Feature | Embedded HTTP (Jetty/Netty) | gRPC / JSON-RPC | FastDaemon |
|:---|:---:|:---:|:---:|
| **Startup Overhead** | High (500–2500 ms) | Medium (300–800 ms) | **Sub-millisecond (< 1 ms)** |
| **Round-Trip Latency** | 2–15 ms | 1–5 ms | **< 300 µs (Loopback TCP)** |
| **External Dependencies** | 10–50 MB transitive JARs | Heavy compiler + runtimes | **Zero Dependencies (Pure Java)** |
| **Instance Protection** | Manual port check | Manual discovery | **Atomic FileLock + PID Registry** |
| **GC Pressure** | High | Medium | **Near-Zero Allocation** |

---

## Key Features

- ⚡ **Sub-Millisecond IPC** — Loopback socket communication optimized for tab-delimited commands and raw payloads.
- 🔒 **Single-Instance File Lock** — OS-level `FileChannel.tryLock()` preventing conflicting daemon workers from running concurrently.
- 🆔 **Automatic PID Registry** — Tracks OS PID in `~/.<appId>/daemon.pid` for clean monitoring and process termination.
- 🔄 **Graceful Shutdown & Heartbeat** — Built-in `PING`/`PONG` heartbeat, shutdown hooks, and remote `SHUTDOWN` command.
- 🪶 **Zero Dependencies** — 100% pure Java 17+. Zero native binaries, zero external libraries.

---

## Architecture & Pipeline

```
┌─────────────────────────────────────────────────────────────┐
│                       Client CLI / Tool                     │
│                  FastDaemon.client("myapp")                 │
└──────────────────────────────┬──────────────────────────────┘
                               │ Loopback TCP (127.0.0.1)
                               │ Wire: "<COMMAND>\t<PAYLOAD>\n"
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    FastDaemonServer Loop                    │
│      - Atomic OS FileLock: ~/.myapp/daemon.lock             │
│      - PID File: ~/.myapp/daemon.pid                        │
│      - Port: loopback TCP socket (default: 47823)           │
└──────────────────────────────┬──────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
     Built-in Commands                DaemonCommandHandler
     - PING     -> PONG               - Custom command dispatch
     - SHUTDOWN -> OK:STOPPED         - Sub-millisecond response
```

---

## Performance & Benchmarks

Measured on Windows 11 (AMD Ryzen 9 / Intel Core i9, OpenJDK 21, loopback TCP):

| Operation | Throughput | Average Latency |
|:---|:---:|:---:|
| **Heartbeat Ping (`client.ping()`)** | > 15,000 ops/sec | < 65 µs |
| **Command Dispatch (`client.send()`)** | > 3,800 ops/sec | ~ 260 µs |
| **Cold Daemon Start & Lock Acquisition** | — | < 5 ms |

---

## Installation

### Option 1: Maven (via JitPack)

Add JitPack to your `pom.xml`:

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

### Option 2: Gradle (via JitPack)

Add this to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastDaemon:0.1.0'
}
```

### Option 3: Direct Download (Pre-built JAR)

Download the pre-compiled JAR directly from GitHub Releases:

1. 📦 [**FastDaemon-0.1.0.jar**](https://github.com/andrestubbe/FastDaemon/releases/download/0.1.0/FastDaemon-0.1.0.jar)

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|:---|:---|:---|:---|
| **Interactive Showcase Demo** | [Demo.java](examples/Demo/src/main/java/fastdaemon/demo/Demo.java) | `run-demo.bat` | End-to-end demonstration of background daemon startup, heartbeat ping, command routing, and graceful shutdown. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastdaemon/benchmark/Benchmark.java) | `run-benchmark.bat` | Formal OpenJDK JMH throughput and latency measurements over loopback TCP. |

---

## Documentation

* **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive API contracts, server builders, client commands, and lifecycle methods.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Architectural principles: zero serialization overhead, local loopback simplicity, and deterministic process control.
* **[ROADMAP.md](docs/ROADMAP.md)**: Upcoming features and milestone extensions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Release history and version notes.
* **[COMPILE.md](docs/COMPILE.md)**: Compilation instructions and benchmark executions.

---

## Platform Support

FastDaemon is a **100% pure Java library** with zero native dependencies. It runs identically across all platforms:

| Platform | Architecture | Status | Notes |
|:---|:---:|:---:|:---|
| **Windows 10/11** | x64, ARM64 | ✅ Fully Supported | Standard Loopback TCP & Win32 FileLock |
| **Linux** | x64, ARM64 | ✅ Fully Supported | Standard Loopback TCP & POSIX FileLock |
| **macOS** | Apple Silicon, x64 | ✅ Fully Supported | Standard Loopback TCP & POSIX FileLock |

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

## Related Projects

- [FastCLICommand](https://github.com/andrestubbe/FastCLICommand) — Zero-allocation, ultra-fast command-line parser and dispatcher
- [FastProcess](https://github.com/andrestubbe/FastProcess) — High-performance native process management and execution
- [FastProcessWatch](https://github.com/andrestubbe/FastProcessWatch) — Real-time process monitoring and event watcher
- [FastScheduler](https://github.com/andrestubbe/FastScheduler) — High-throughput low-latency task and cron scheduler
- [FastTerminal](https://github.com/andrestubbe/FastTerminal) — High-performance True-Color double-buffered terminal rendering engine
- [FastTUI](https://github.com/andrestubbe/FastTUI) — High-performance native Windows TUI framework with mouse support and widgets
- [FastCore](https://github.com/andrestubbe/FastCore) — Native library loader, FFM gateway, and platform abstraction layer

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
