package ru.alexander1248.raspberry;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alexander1248.raspberry.config.RaspberryConfig;

public class Raspberry implements ModInitializer {
    public static final String MOD_ID = "Raspberry";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RaspberryConfig config  = RaspberryConfig.createAndLoad();

    @Override
    public void onInitialize() {
        LOGGER.info("Raspberry Initialized");
    }


}
