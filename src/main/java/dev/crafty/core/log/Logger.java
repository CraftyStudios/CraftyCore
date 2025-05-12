package dev.crafty.core.log;

import java.io.File;

public interface Logger {
    /**
     * Logs informational messages
     *
     * @param console should log to console
     * @param message the informational message to be logged
     */
    void info(String message, boolean console);

    /**
     * Logs informational messages to the log file and console
     *
     * @param message the informational message to be logged
     */
    void info(String message);

    /**
     * Logs a warning message
     *
     * @param console should log to console
     * @param message the warning message to be logged
     */
    void warn(String message, boolean console);

    /**
     * Logs a warning message to the console and log file
     *
     * @param message the warning message to be logged
     */
    void warn(String message);

    /**
     * Logs an error message
     *
     * @param console should log to console
     * @param message the error message to be logged
     */
    void error(String message, boolean console);

    /**
     * Logs an error message to the console and log file
     *
     * @param message the error message to be logged
     */
    void error(String message);

    /**
     * Logs an exception to the console and log file.
     *
     * @param e the exception to be logged
     */
    void logAndDigest(Exception e);

    /**
     * Logs an exception to the console and log file.
     *
     * @param context the context of the exception
     * @param e the exception to be logged
     */
    void logAndDigest(String context, Exception e);

    /**
     * Logs an exception to the console and log file and then throws it.
     *
     * @param e the exception to be logged and thrown
     * @throws Exception the exception to be thrown
     */
    void logAndRaise(Exception e) throws Exception;

    /**
     * Logs an exception to the console and log file and then throws it.
     *
     * @param context the context of the exception
     * @param e the exception to be logged and thrown
     * @throws Exception the exception to be thrown
     */
    void logAndRaise(String context, Exception e) throws Exception;

    /**
     * Gets the current log file
     *
     * @return the current log file
     */
    File getCurrentLogFile();
}
