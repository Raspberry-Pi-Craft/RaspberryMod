package ru.alexander1248.raspberry.loggers;

import org.slf4j.Logger;

public class LoggerMessenger implements AbstractMessenger {
    private final Logger logger;

    public LoggerMessenger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void debug(String var1) {
        logger.debug(var1);
    }

    @Override
    public void debug(String var1, Object... var2) {
        logger.debug(var1, var2);
    }

    @Override
    public void debug(String var1, Throwable var2) {
        logger.debug(var1, var2);
    }

    @Override
    public void info(String var1) {
        logger.info(var1);
    }

    @Override
    public void info(String var1, Object... var2) {
        logger.info(var1, var2);
    }

    @Override
    public void info(String var1, Throwable var2) {
        logger.info(var1, var2);
    }

    @Override
    public void warn(String var1) {
        logger.warn(var1);
    }

    @Override
    public void warn(String var1, Object... var2) {
        logger.warn(var1, var2);
    }

    @Override
    public void warn(String var1, Throwable var2) {
        logger.warn(var1, var2);
    }

    @Override
    public void error(String var1) {
        logger.error(var1);
    }

    @Override
    public void error(String var1, Object... var2) {
        logger.error(var1, var2);
    }

    @Override
    public void error(String var1, Throwable var2) {
        logger.error(var1, var2);
    }
}
