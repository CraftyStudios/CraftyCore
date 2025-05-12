package dev.crafty.core.log;

public class LoggingUtils {
    public static <T> T tryOrLog(ThrowingSupplier<T> supplier, Logger logger) {
        try {
            return supplier.get();
        } catch (Exception e) {
            logger.logAndDigest(e);
            return null;
        }
    }

    public static void tryOrLog(ThrowingRunnable runnable, Logger logger) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.logAndDigest(e);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}