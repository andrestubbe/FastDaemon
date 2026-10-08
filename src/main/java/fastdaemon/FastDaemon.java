package fastdaemon;

/**
 * Unified entry point and fluent builder API for FastDaemon.
 */
public final class FastDaemon {

    private FastDaemon() {}

    public static ServerBuilder server(String appId) {
        return new ServerBuilder(appId);
    }

    public static ClientBuilder client(String appId) {
        return new ClientBuilder(appId);
    }

    public static final class ServerBuilder {
        private final String appId;
        private int port = 47823;
        private DaemonCommandHandler handler;

        public ServerBuilder(String appId) {
            this.appId = appId;
        }

        public ServerBuilder port(int port) {
            this.port = port;
            return this;
        }

        public ServerBuilder onCommand(DaemonCommandHandler handler) {
            this.handler = handler;
            return this;
        }

        public FastDaemonServer build() {
            return new FastDaemonServer(appId, port, handler);
        }

        public void start() {
            build().run();
        }
    }

    public static final class ClientBuilder {
        private final String appId;
        private int port = 47823;

        public ClientBuilder(String appId) {
            this.appId = appId;
        }

        public ClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        public FastDaemonClient build() {
            return new FastDaemonClient(appId, port);
        }
    }
}
