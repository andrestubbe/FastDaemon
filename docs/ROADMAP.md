# FastDaemon Roadmap

## Milestone Status

### Core Daemon & Loopback IPC (v0.1.0)
**Status:** Released
- [x] Fluent `FastDaemon.server()` and `FastDaemon.client()` builders.
- [x] Sub-millisecond loopback TCP command dispatcher.
- [x] Single-instance OS `FileLock` coordination to avoid multiple conflicting daemon workers.
- [x] Automatic PID tracking in `~/.<appId>/daemon.pid`.
- [x] Built-in `PING` heartbeat and remote `SHUTDOWN` command.
- [x] 100% pure Java 17+ with zero external dependencies.
- [x] OpenJDK JMH microbenchmark suite and interactive showcase demo.

---

## Upcoming Features

### Unix Domain Socket (UDS) Backend
**Status:** In Progress
- [ ] Native Unix Domain Socket (`AF_UNIX`) transport on Linux/macOS and Windows 10+ (`StandardProtocolFamily.UNIX`) for lower kernel latency.

### Named Pipe IPC on Windows
**Status:** Backlog
- [ ] Optional Windows Named Pipe transport (`\\.\pipe\...`) integration with `FastCore`.

### Multi-Worker Concurrency Pool
**Status:** Backlog
- [ ] Configurable worker thread pool for long-running compute jobs in the daemon.
