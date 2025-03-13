package ru.alexander1248.raspberry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alexander1248.raspberry.config.RaspberryConfig;
import ru.alexander1248.raspberry.loader.PackIndexUpdater;

import java.io.IOException;

public class Raspberry implements ModInitializer {
    public static final String MOD_ID = "Raspberry";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RaspberryConfig CONFIG  = RaspberryConfig.createAndLoad();

    @Override
    public void onInitialize() {
        LOGGER.info("Raspberry initialized as {}!", FabricLoader.getInstance().getEnvironmentType().name());
        try {
            PackIndexUpdater updater = new PackIndexUpdater(CONFIG.modListUri());
            updater.tryUpdateFiles();
        } catch (IOException e) {
            LOGGER.error("File IO error!", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted!", e);
            throw new RuntimeException(e);
        }
    }
}
