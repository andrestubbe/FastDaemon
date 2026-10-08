# FastDaemon API Reference Manual

`FastDaemon` provides lightweight, zero-dependency background daemon process management and ultra-fast loopback inter-process communication for Java applications.

---

## 1. Class: `fastdaemon.FastDaemon`

Entry point and factory for building daemon servers and clients.

### Method Index

| Method Signature | Return Type | Description |
|:---|:---|:---|
| `server(String appId)` | `ServerBuilder` | Creates a new server builder configured for the given application identifier. |
| `client(String appId)` | `ClientBuilder` | Creates a new client builder configured for the given application identifier. |

### Inner Class: `FastDaemon.ServerBuilder`

| Method | Return Type | Description |
|:---|:---|:---|
| `port(int port)` | `ServerBuilder` | Sets the loopback TCP port (default: `47823`). |
| `onCommand(DaemonCommandHandler handler)` | `ServerBuilder` | Registers the custom command dispatcher callback. |
| `build()` | `FastDaemonServer` | Builds the server instance. |
| `start()` | `void` | Builds and starts the server loop in the calling thread (blocking). |

### Inner Class: `FastDaemon.ClientBuilder`

| Method | Return Type | Description |
|:---|:---|:---|
| `port(int port)` | `ClientBuilder` | Sets the target loopback TCP port (default: `47823`). |
| `build()` | `FastDaemonClient` | Constructs the configured client instance. |

---

## 2. Class: `fastdaemon.FastDaemonClient`

Ultra-low latency client communicating with the resident daemon over loopback TCP.

### Method Index

| Method Signature | Return Type | Description |
|:---|:---|:---|
| `ping()` | `boolean` | Checks daemon availability with default 300 ms timeout. |
| `ping(int timeoutMs)` | `boolean` | Checks daemon availability with specified timeout in milliseconds. |
| `send(String command, String payload)` | `String` | Sends a command with payload (default 3,000 ms timeout). Returns response string. |
| `send(String command, String payload, int timeoutMs)` | `String` | Sends a command with payload and custom timeout. |
| `shutdown()` | `boolean` | Sends a graceful shutdown command to the background daemon. |
| `isRunning()` | `boolean` | Convenience alias for `ping()`. |
| `readPid()` | `long` | Reads recorded PID from `~/.<appId>/daemon.pid` (or `-1` if absent). |
| `appId()` | `String` | Returns the configured application identifier. |
| `port()` | `int` | Returns the configured loopback TCP port. |

---

## 3. Class: `fastdaemon.FastDaemonServer`

High-performance daemon listener handling lock acquisition, PID registration, and socket events.

### Method Index

| Method Signature | Return Type | Description |
|:---|:---|:---|
| `run()` | `void` | Acquires OS instance lock, writes PID, registers shutdown hooks, and enters socket accept loop (blocking). |
| `shutdown()` | `void` | Stops the server loop, releases instance lock, and deletes PID file. |
| `isRunning()` | `boolean` | Returns whether the server loop is currently active. |
| `port()` | `int` | Returns the bound loopback port. |

---

## 4. Interface: `fastdaemon.DaemonCommandHandler`

Functional interface for dispatching custom application commands.

```java
@FunctionalInterface
public interface DaemonCommandHandler {
    String handle(String command, String payload);
}
```

---

## 5. Class: `fastdaemon.DaemonLifecycle`

Low-level process lifecycle and single-instance coordination.

### Method Index

| Method Signature | Return Type | Description |
|:---|:---|:---|
| `acquireInstanceLock()` | `boolean` | Acquires exclusive OS `FileLock` on `~/.<appId>/daemon.lock`. |
| `releaseInstanceLock()` | `void` | Releases the held file lock. |
| `writePid(long pid)` | `void` | Writes process PID to disk. |
| `readPid()` | `long` | Reads recorded PID from disk (`-1` if missing). |
| `clearPid()` | `void` | Deletes the PID file. |

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
