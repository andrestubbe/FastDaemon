package fastdaemon;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * High-performance, lightweight loopback daemon server for FastJava.
 */
public final class FastDaemonServer {

    private final String appId;
    private final int port;
    private final DaemonCommandHandler handler;
    private final DaemonLifecycle lifecycle;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService workerPool;

    public FastDaemonServer(String appId, int port, DaemonCommandHandler handler) {
        this.appId = appId;
        this.port = port;
        this.handler = handler != null ? handler : (cmd, payload) -> FastDaemonProtocol.RESP_OK;
        this.lifecycle = new DaemonLifecycle(appId);
    }

    /**
     * Starts and runs the daemon loop in the current thread (blocking).
     */
    public void run() {
        if (!lifecycle.acquireInstanceLock()) {
            System.err.println("[FastDaemon] Another instance for '" + appId + "' is already running or holding lock.");
            return;
        }

        lifecycle.writePid(ProcessHandle.current().pid());
        running.set(true);
        workerPool = Executors.newSingleThreadExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(1000);

            while (running.get()) {
                try {
                    Socket socket = server.accept();
                    handleClient(socket);
                } catch (java.net.SocketTimeoutException ignored) {
                } catch (Exception e) {
                    if (!running.get()) break;
                }
            }
        } catch (Exception e) {
            System.err.println("[FastDaemon] Server bind error on port " + port + ": " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            if (workerPool != null) {
                workerPool.shutdown();
            }
            lifecycle.releaseInstanceLock();
            lifecycle.clearPid();
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int read = in.read(buffer);
            if (read <= 0) return;

            String request = new String(buffer, 0, read, StandardCharsets.UTF_8).trim();
            if (request.equalsIgnoreCase(FastDaemonProtocol.CMD_PING)) {
                out.write((FastDaemonProtocol.RESP_PONG + "\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
            } else if (request.equalsIgnoreCase(FastDaemonProtocol.CMD_SHUTDOWN)) {
                out.write((FastDaemonProtocol.RESP_OK_STOPPED + "\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
                running.set(false);
            } else {
                int splitIdx = request.indexOf(FastDaemonProtocol.DELIMITER);
                String cmd = splitIdx >= 0 ? request.substring(0, splitIdx) : request;
                String payload = splitIdx >= 0 ? request.substring(splitIdx + 1) : "";

                String response = handler.handle(cmd, payload);
                if (response != null) {
                    out.write((response + "\n").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            }
        } catch (Exception ignored) {}
    }

    public boolean isRunning() {
        return running.get();
    }

    public int port() {
        return port;
    }
}
