package fastdaemon;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Ultra-low latency client for interacting with a FastDaemon instance over loopback TCP.
 */
public final class FastDaemonClient {

    private final String appId;
    private final int port;
    private final DaemonLifecycle lifecycle;

    public FastDaemonClient(String appId, int port) {
        this.appId = appId;
        this.port = port;
        this.lifecycle = new DaemonLifecycle(appId);
    }

    public boolean ping() {
        return ping(300);
    }

    public boolean ping(int timeoutMs) {
        try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
            s.setSoTimeout(timeoutMs);
            OutputStream out = s.getOutputStream();
            out.write((FastDaemonProtocol.CMD_PING + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = s.getInputStream();
            byte[] buf = new byte[64];
            int read = in.read(buf);
            if (read > 0) {
                String resp = new String(buf, 0, read, StandardCharsets.UTF_8).trim();
                return FastDaemonProtocol.RESP_PONG.equalsIgnoreCase(resp);
            }
        } catch (Exception ignored) {}
        return false;
    }

    public boolean shutdown() {
        try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
            s.setSoTimeout(1000);
            OutputStream out = s.getOutputStream();
            out.write((FastDaemonProtocol.CMD_SHUTDOWN + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();

            lifecycle.clearPid();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    public String send(String command, String payload) {
        return send(command, payload, 3000);
    }

    public String send(String command, String payload, int timeoutMs) {
        try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
            s.setSoTimeout(timeoutMs);
            OutputStream out = s.getOutputStream();

            String p = (payload != null) ? payload : "";
            String wire = command + FastDaemonProtocol.DELIMITER + p + "\n";
            out.write(wire.getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = s.getInputStream();
            byte[] buf = new byte[4096];
            int read = in.read(buf);
            if (read > 0) {
                return new String(buf, 0, read, StandardCharsets.UTF_8).trim();
            }
            return "";
        } catch (Exception e) {
            return null;
        }
    }

    public long readPid() {
        return lifecycle.readPid();
    }

    public boolean isRunning() {
        return ping();
    }

    public String appId() {
        return appId;
    }

    public int port() {
        return port;
    }
}
