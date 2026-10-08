package fastdaemon.benchmark;

import fastdaemon.FastDaemon;
import fastdaemon.FastDaemonClient;
import fastdaemon.FastDaemonServer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private static final String APP_ID = "fastdaemon-benchmark";
    private static final int PORT = 49153;

    private FastDaemonClient client;
    private Thread serverThread;
    private FastDaemonServer server;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        server = FastDaemon.server(APP_ID)
            .port(PORT)
            .onCommand((cmd, payload) -> "OK")
            .build();

        serverThread = new Thread(server::run, "BenchServer");
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(200);

        client = FastDaemon.client(APP_ID).port(PORT).build();
        if (!client.ping(1000)) {
            throw new IllegalStateException("Failed to connect to benchmark daemon server!");
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (client != null) {
            client.shutdown();
        }
        if (server != null) {
            server.shutdown();
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public boolean benchmarkPing() {
        return client.ping(500);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public String benchmarkCommandRoundTrip() {
        return client.send("ECHO", "payload_test_data", 500);
    }
}
