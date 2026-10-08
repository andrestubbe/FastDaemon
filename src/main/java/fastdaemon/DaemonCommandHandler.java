package fastdaemon;

/**
 * Functional callback interface for handling incoming IPC messages in the FastDaemon server.
 */
@FunctionalInterface
public interface DaemonCommandHandler {
    /**
     * Handles an incoming request command.
     *
     * @param command The command name (e.g. "SPEAK", "STATUS")
     * @param payload The raw string payload (arguments or serialized line)
     * @return Response string sent back to the client (e.g. "OK", "PONG", "ERR")
     */
    String handle(String command, String payload);
}
