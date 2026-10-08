# The Philosophy of FastDaemon

> [!IMPORTANT]
> **"Sub-Millisecond IPC. Deterministic Process Control. Zero Bloat."**

FastDaemon is designed on the principle that local tooling, developer utilities, and background helper processes should **start instantaneously, communicate without serialization overhead, and never leave zombie processes behind**.

---

## Core Tenets

### 1. Reject Heavy HTTP Stacks for Local Process Communication
When a CLI tool needs to talk to a background worker on `localhost`, spinning up embedded HTTP servers (Jetty, Tomcat, Netty) brings:
- 10–50 MB of third-party JARs.
- 500–2,500 ms cold startup delays.
- Heavy JSON or REST parsing overhead.

FastDaemon uses direct **loopback TCP with a lightweight delimiter protocol (`CMD\tPAYLOAD\n`)**, delivering round-trip latency under 300 µs with zero external libraries.

### 2. Deterministic Single-Instance Coordination
Background daemons frequently collide when multiple terminal sessions launch at once. FastDaemon utilizes OS-level kernel locks (`FileChannel.tryLock()`) combined with atomic PID files:
- Multiple daemon instances cannot run simultaneously.
- Stale lock files are cleanly reclaimed by the OS kernel upon process exit.
- Client tools can immediately detect if a worker is alive with a fast non-blocking heartbeat probe.

### 3. Graceful Lifecycle & Clean Exit
A well-behaved background process must cleanly clean up after itself. FastDaemon provides:
- Automatic JVM shutdown hook registration.
- Built-in remote `SHUTDOWN` command.
- Deterministic PID cleanup.

### 4. Zero Dependencies & Instant Startup
FastDaemon contains **zero external dependencies** and is built on pure Java APIs. It runs on JDK 17+ with instant cold-start performance.

---

**🤖 FastDaemon — Powering background process execution in the FastJava Ecosystem.**
