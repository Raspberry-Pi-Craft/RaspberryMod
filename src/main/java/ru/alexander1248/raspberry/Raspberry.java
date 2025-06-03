package ru.alexander1248.raspberry;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alexander1248.raspberry.commands.UpdateCommand;
import ru.alexander1248.raspberry.config.RaspberryConfig;
import ru.alexander1248.raspberry.loader.PackIndexUpdater;
import ru.alexander1248.raspberry.loggers.AbstractMessenger;
import ru.alexander1248.raspberry.loggers.LoggerMessenger;

import java.io.IOException;

public class Raspberry implements ModInitializer {
    public static final String MOD_ID = "Raspberry";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RaspberryConfig CONFIG  = RaspberryConfig.createAndLoad();


    @Override
    public void onInitialize() {
        AbstractMessenger messenger = new LoggerMessenger(LOGGER);
        messenger.info("Raspberry initialized as... {}!", FabricLoader.getInstance().getEnvironmentType().name());
        try {
            PackIndexUpdater.init(messenger);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
                CommandRegistrationCallback.EVENT.register(Raspberry::registerCommands);
                if (CONFIG.updateOnLoad()) {
                    PackIndexUpdater.checkFiles(messenger);
                    if (PackIndexUpdater.tryUpdateFiles(messenger, null) && !Raspberry.CONFIG.dontReload())
                        System.exit(0);
                }
            }

        } catch (IOException e) {
            messenger.error("File IO error!", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            messenger.error("Interrupted!", e);
            throw new RuntimeException(e);
        }
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        UpdateCommand.register(dispatcher, registryAccess);
    }

}
