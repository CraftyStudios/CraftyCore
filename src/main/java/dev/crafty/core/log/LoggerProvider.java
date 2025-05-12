package dev.crafty.core.log;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

public class LoggerProvider implements Logger {
    private static final Queue<String> buffer = new ArrayDeque<>();

    private final File logFile;

    public LoggerProvider(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public void info(String message, boolean console) {

    }

    @Override
    public void info(String message) {
        info(message, true);
    }

    @Override
    public void warn(String message, boolean console) {

    }

    @Override
    public void warn(String message) {
        warn(message, true);
    }

    @Override
    public void error(String message, boolean console) {

    }

    @Override
    public void error(String message) {
        error(message, true);
    }

    @Override
    public void logAndDigest(Exception e) {

    }

    @Override
    public void logAndDigest(String context, Exception e) {

    }

    @Override
    public void logAndRaise(Exception e) throws Exception {

    }

    @Override
    public void logAndRaise(String context, Exception e) throws Exception {

    }

    @Override
    public File getCurrentLogFile() {
        return this.logFile;
    }
}
