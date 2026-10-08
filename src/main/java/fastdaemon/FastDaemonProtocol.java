package fastdaemon;

/**
 * Common wire-protocol constants for FastDaemon communication.
 */
public final class FastDaemonProtocol {

    public static final String CMD_PING = "PING";
    public static final String RESP_PONG = "PONG";
    public static final String CMD_SHUTDOWN = "SHUTDOWN";
    public static final String RESP_OK_STOPPED = "OK STOPPED";
    public static final String RESP_OK = "OK";
    public static final String RESP_ERR = "ERR";

    public static final char DELIMITER = '\t';
    public static final char NEWLINE = '\n';

    private FastDaemonProtocol() {}
}
