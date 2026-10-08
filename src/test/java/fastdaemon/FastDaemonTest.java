package fastdaemon;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class FastDaemonTest {

    @Test
    public void testServerClientPingAndCustomCommand() throws Exception {
        int testPort = 58921;
        String appId = "fastdaemontest";

        FastDaemonServer server = FastDaemon.server(appId)
                .port(testPort)
                .onCommand((cmd, payload) -> {
                    if ("ECHO".equalsIgnoreCase(cmd)) {
                        return "REPLY:" + payload;
                    }
                    return "UNKNOWN";
                })
                .build();

        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(server::run);

        FastDaemonClient client = FastDaemon.client(appId).port(testPort).build();

        // Wait for server to become responsive
        boolean ready = false;
        for (int i = 0; i < 20; i++) {
            if (client.ping()) {
                ready = true;
                break;
            }
            Thread.sleep(50);
        }
        assertTrue(ready, "Server must be reachable via PING");

        // Send custom command
        String reply = client.send("ECHO", "Hello FastDaemon");
        assertEquals("REPLY:Hello FastDaemon", reply);

        // Shutdown server
        assertTrue(client.shutdown());
        serverFuture.get(2, TimeUnit.SECONDS);
        assertFalse(client.ping());
    }
}
