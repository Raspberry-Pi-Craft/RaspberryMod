package ru.alexander1248.raspberry.commands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import ru.alexander1248.raspberry.Raspberry;
import ru.alexander1248.raspberry.loader.PackIndexUpdater;
import ru.alexander1248.raspberry.loggers.AbstractMessenger;
import ru.alexander1248.raspberry.loggers.CommandMessenger;

import java.io.IOException;

public class UpdateCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandBuildContext)
    {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("raspberry");
        builder.requires(c -> c.hasPermissionLevel(2))
                .then(
                        CommandManager.literal("update")
                        .executes(context -> update(context.getSource()))
                )
                .executes(context -> help(context.getSource()));

        dispatcher.register(builder);
    }

    private static int help(ServerCommandSource source) {
        AbstractMessenger messager = new CommandMessenger(source);
        messager.info("======================================================");
        messager.info(" ____    __    ___  ____  ____  ____  ____  ____  _  _");
        messager.info("(  _ \\  /__\\  / __)(  _ \\(  _ \\( ___)(  _ \\(  _ \\( \\/ )");
        messager.info(" )   / /(__)\\ \\__ \\ )___/ ) _ < )__)  )   / )   / \\  /");
        messager.info("(_)\\_)(__)(__)(___/(__)  (____/(____)(_)\\_)(_)\\_) (__)");
        messager.info("======================================================");
        messager.info("Raspberry server control system commands:");
        messager.info("update - updates server modes");
        return 1;
    }

    private static int update(ServerCommandSource source) {
        AbstractMessenger messager = new CommandMessenger(source);
        try {
            PackIndexUpdater.checkFiles(messager);
            if (PackIndexUpdater.tryUpdateFiles(messager, null)) {
                if (Raspberry.CONFIG.dontReload())
                    messager.info("Files updated! Reload server manually...");
                else
                    System.exit(0);
            }
            else
                messager.info("All files already been updated!");
        } catch (IOException e) {
            messager.error("File IO error!", e);
        } catch (InterruptedException e) {
            messager.error("Interrupted!", e);
        }
        return 1;
    }
}
