package ru.alexander1248.raspberry.loggers;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CombinedMessenger implements AbstractMessenger {

    private final AbstractMessenger[] messengers;

    public CombinedMessenger(AbstractMessenger... messengers) {
        this.messengers = messengers;
    }
    
    @Override
    public void debug(String var1) {
        for (AbstractMessenger messenger : messengers)
            messenger.debug(var1);
    }

    @Override
    public void debug(String var1, Object... var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.debug(var1, var2);
    }

    @Override
    public void debug(String var1, Throwable var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.debug(var1, var2);
    }

    @Override
    public void info(String var1) {
        for (AbstractMessenger messenger : messengers)
            messenger.info(var1);
    }

    @Override
    public void info(String var1, Object... var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.info(var1, var2);
    }

    @Override
    public void info(String var1, Throwable var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.info(var1, var2);
    }

    @Override
    public void warn(String var1) {
        for (AbstractMessenger messenger : messengers)
            messenger.warn(var1);
    }

    @Override
    public void warn(String var1, Object... var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.warn(var1, var2);
    }

    @Override
    public void warn(String var1, Throwable var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.warn(var1, var2);
    }

    @Override
    public void error(String var1) {
        for (AbstractMessenger messenger : messengers)
            messenger.error(var1);
    }

    @Override
    public void error(String var1, Object... var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.error(var1, var2);
    }

    @Override
    public void error(String var1, Throwable var2) {
        for (AbstractMessenger messenger : messengers)
            messenger.error(var1, var2);
    }
}
