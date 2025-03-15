package ru.alexander1248.raspberry.loggers;

public class CombinedMessenger implements ru.alexander1248.raspberry.loggers.AbstractMessenger {

    private final ru.alexander1248.raspberry.loggers.AbstractMessenger[] messengers;

    public CombinedMessenger(ru.alexander1248.raspberry.loggers.AbstractMessenger... messengers) {
        this.messengers = messengers;
    }
    
    @Override
    public void debug(String var1) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.debug(var1);
    }

    @Override
    public void debug(String var1, Object... var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.debug(var1, var2);
    }

    @Override
    public void debug(String var1, Throwable var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.debug(var1, var2);
    }

    @Override
    public void info(String var1) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.info(var1);
    }

    @Override
    public void info(String var1, Object... var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.info(var1, var2);
    }

    @Override
    public void info(String var1, Throwable var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.info(var1, var2);
    }

    @Override
    public void warn(String var1) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.warn(var1);
    }

    @Override
    public void warn(String var1, Object... var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.warn(var1, var2);
    }

    @Override
    public void warn(String var1, Throwable var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.warn(var1, var2);
    }

    @Override
    public void error(String var1) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.error(var1);
    }

    @Override
    public void error(String var1, Object... var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.error(var1, var2);
    }

    @Override
    public void error(String var1, Throwable var2) {
        for (ru.alexander1248.raspberry.loggers.AbstractMessenger messenger : messengers)
            messenger.error(var1, var2);
    }
}
