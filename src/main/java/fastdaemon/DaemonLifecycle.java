package fastdaemon;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Robust cross-platform single-instance and PID lifecycle manager for FastDaemon.
 */
public final class DaemonLifecycle {

    private final String appId;
    private final Path runtimeDir;
    private final File pidFile;
    private final File lockFile;
    private FileChannel lockChannel;
    private FileLock fileLock;

    public DaemonLifecycle(String appId) {
        this.appId = appId != null && !appId.isBlank() ? appId.trim().toLowerCase() : "app";
        this.runtimeDir = Paths.get(System.getProperty("user.home"), "." + this.appId);
        this.pidFile = runtimeDir.resolve("daemon.pid").toFile();
        this.lockFile = runtimeDir.resolve("daemon.lock").toFile();
    }

    public boolean acquireInstanceLock() {
        try {
            ensureRuntimeDir();
            RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
            lockChannel = raf.getChannel();
            fileLock = lockChannel.tryLock();
            return fileLock != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void releaseInstanceLock() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
            if (lockChannel != null && lockChannel.isOpen()) {
                lockChannel.close();
            }
            if (lockFile.exists()) {
                lockFile.delete();
            }
        } catch (Exception ignored) {}
    }

    public void writePid(long pid) {
        try {
            ensureRuntimeDir();
            Files.writeString(pidFile.toPath(), String.valueOf(pid));
            pidFile.deleteOnExit();
        } catch (Exception ignored) {}
    }

    public long readPid() {
        if (!pidFile.exists()) return -1;
        try {
            String content = Files.readString(pidFile.toPath()).trim();
            return Long.parseLong(content);
        } catch (Exception e) {
            return -1;
        }
    }

    public void clearPid() {
        try {
            if (pidFile.exists()) {
                pidFile.delete();
            }
        } catch (Exception ignored) {}
    }

    public boolean isProcessAlive(long pid) {
        if (pid <= 0) return false;
        return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
    }

    public boolean killProcess(long pid) {
        if (pid <= 0) return false;
        return ProcessHandle.of(pid).map(ph -> {
            ph.destroy();
            return true;
        }).orElse(false);
    }

    private void ensureRuntimeDir() {
        if (!runtimeDir.toFile().exists()) {
            runtimeDir.toFile().mkdirs();
        }
    }

    public String appId() {
        return appId;
    }

    public Path runtimeDir() {
        return runtimeDir;
    }
}
