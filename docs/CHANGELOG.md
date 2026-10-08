# Changelog — FastDaemon 📜

All notable changes to **FastDaemon** will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] — 2026-10-08

### Added
- **Core Daemon Framework**:
  - `FastDaemon` fluent builder API for constructing servers and clients.
  - `FastDaemonServer` background listener supporting non-blocking client requests.
  - `FastDaemonClient` ultra-low latency client with `ping()`, `send()`, and `shutdown()`.
  - `DaemonCommandHandler` functional interface for command dispatching.
  - Wire protocol using tab-delimited commands (`<CMD>\t<PAYLOAD>\n`).
- **Process Lifecycle & Instance Coordination**:
  - `DaemonLifecycle` with exclusive OS `FileLock` ensuring single-instance guarantees.
  - Automatic PID file generation (`~/.<appId>/daemon.pid`).
  - JVM shutdown hook integration for automated lock and PID cleanup.
- **Showcase & Benchmarks**:
  - `examples/Demo`: Interactive showcase demonstrating lifecycle, IPC commands, and in-process latency.
  - `examples/Benchmark`: JMH microbenchmark suite measuring loopback throughput and ping times.
  - Windows launcher scripts (`run-demo.bat`, `run-benchmark.bat`).
- **Complete Documentation**:
  - `README.md`, `REFERENCE.md`, `PHILOSOPHY.md`, `ROADMAP.md`, `COMPILE.md`.
