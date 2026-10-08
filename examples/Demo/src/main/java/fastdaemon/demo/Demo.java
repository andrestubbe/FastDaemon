package fastdaemon.demo;

import fastdaemon.FastDaemon;
import fastdaemon.FastDaemonClient;
import fastdaemon.FastDaemonServer;

/**
 * Interactive showcase demo for FastDaemon loopback IPC and lifecycle management.
 */
public class Demo {

    private static final String APP_ID = "fastdaemon-demo";
    private static final int PORT = 49152;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   🤖 FastDaemon Interactive Showcase Demo");
        System.out.println("==================================================");

        // 1. Check if daemon is already running
        FastDaemonClient client = FastDaemon.client(APP_ID).port(PORT).build();
        System.out.println("\n[1] Probing existing daemon on port " + PORT + "...");
        boolean active = client.ping();
        System.out.println("    Active status: " + (active ? "RUNNING" : "NOT RUNNING"));

        // 2. Start daemon background server thread
        System.out.println("\n[2] Starting FastDaemon server in background thread...");
        Thread daemonThread = new Thread(() -> {
            FastDaemon.server(APP_ID)
                .port(PORT)
                .onCommand((cmd, payload) -> {
                    switch (cmd.toUpperCase()) {
                        case "ECHO":
                            return "PONG: " + payload;
                        case "UPPER":
                            return payload.toUpperCase();
                        case "COMPUTE":
                            int count = Integer.parseInt(payload);
                            return "RESULT:" + (count * 42);
                        default:
                            return "ERR:Unknown command " + cmd;
                    }
                })
                .start();
        }, "FastDaemon-Worker");
        daemonThread.setDaemon(true);
        daemonThread.start();

        // 3. Wait for daemon to become ready
        Thread.sleep(150);
        long startProbe = System.nanoTime();
        boolean ready = client.ping(1000);
        long probeNs = System.nanoTime() - startProbe;
        System.out.printf("    Daemon Ping OK: %b (RTT: %.3f ms)%n", ready, probeNs / 1_000_000.0);

        if (!ready) {
            System.err.println("Failed to start daemon server!");
            return;
        }

        // 4. Send structured IPC commands
        System.out.println("\n[3] Sending IPC Commands over Loopback TCP:");
        String r1 = client.send("ECHO", "FastJava High-Performance IPC");
        System.out.println("    -> ECHO: " + r1);

        String r2 = client.send("UPPER", "sub-millisecond round-trip");
        System.out.println("    -> UPPER: " + r2);

        String r3 = client.send("COMPUTE", "100");
        System.out.println("    -> COMPUTE: " + r3);

        // 5. Measure IPC Round-Trip Latency
        System.out.println("\n[4] Benchmarking Loopback IPC Throughput (10,000 round-trips)...");
        int iterations = 10_000;
        long startIpc = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            client.send("ECHO", "ping");
        }
        long durationNs = System.nanoTime() - startIpc;
        double opsPerSec = (iterations * 1_000_000_000.0) / durationNs;
        double avgLatencyUs = (durationNs / 1000.0) / iterations;

        System.out.printf("    Dispatched %,d commands in %.2f ms%n", iterations, durationNs / 1_000_000.0);
        System.out.printf("    Throughput: %,.0f ops/sec%n", opsPerSec);
        System.out.printf("    Average Latency: %.2f µs per round-trip%n", avgLatencyUs);

        // 6. Graceful shutdown
        System.out.println("\n[5] Graceful Shutdown Request:");
        boolean stopped = client.shutdown();
        System.out.println("    Shutdown command acknowledged: " + stopped);
        Thread.sleep(100);
        System.out.println("    Active after shutdown: " + client.ping(200));

        System.out.println("\n✅ FastDaemon demo finished successfully!");
    }
}
