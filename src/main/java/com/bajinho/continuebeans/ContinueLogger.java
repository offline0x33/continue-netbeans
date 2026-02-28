package com.bajinho.continuebeans;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging for Continue-Beans.
 */
public class ContinueLogger {
    private static final Logger LOGGER = Logger.getLogger("ContinueBeans");

    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static void warn(String message, Throwable t) {
        LOGGER.log(Level.WARNING, message, t);
    }

    public static void error(String message, Throwable t) {
        LOGGER.log(Level.SEVERE, message, t);
    }
}
