package ru.alexander1248.raspberry.loggers;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandMessenger implements AbstractMessenger {
    private final ServerCommandSource source;

    public CommandMessenger(ServerCommandSource source) {
        this.source = source;
    }

    private Text formatMessage(String tag, String message) {
        return Text.literal("[%s]%s".formatted(tag, message));
    }
    private String format(String format, Object... data) {
        return String.format(
                format.replace("%", "%%").replace("{}", "%s"),
                data
        );
    }
    
    @Override
    public void debug(String var1) {
        source.sendMessage(formatMessage("DEBUG", var1));
    }

    @Override
    public void debug(String var1, Object... var2) {
        source.sendMessage(formatMessage("DEBUG", format(var1, var2)));
    }

    @Override
    public void debug(String var1, Throwable var2) {
        source.sendMessage(formatMessage("DEBUG", format(var1, var2)));
    }

    @Override
    public void info(String var1) {
        source.sendMessage(formatMessage("INFO", var1));
    }

    @Override
    public void info(String var1, Object... var2) {
        source.sendMessage(formatMessage("INFO", format(var1, var2)));
    }

    @Override
    public void info(String var1, Throwable var2) {
        source.sendMessage(formatMessage("INFO", format(var1, var2)));
    }

    @Override
    public void warn(String var1) {
        source.sendMessage(formatMessage("WARN", var1));
    }

    @Override
    public void warn(String var1, Object... var2) {
        source.sendMessage(formatMessage("WARN", format(var1, var2)));
    }

    @Override
    public void warn(String var1, Throwable var2) {
        source.sendMessage(formatMessage("WARN", format(var1, var2)));
    }

    @Override
    public void error(String var1) {
        source.sendError(formatMessage("ERROR", var1));
    }

    @Override
    public void error(String var1, Object... var2) {
        source.sendError(formatMessage("ERROR", format(var1, var2)));
    }

    @Override
    public void error(String var1, Throwable var2) {
        source.sendError(formatMessage("ERROR", format(var1, var2)));
    }
}
