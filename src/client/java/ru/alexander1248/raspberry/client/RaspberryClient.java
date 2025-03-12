package ru.alexander1248.raspberry.client;

import net.fabricmc.api.ClientModInitializer;


import static ru.alexander1248.raspberry.Raspberry.LOGGER;

public class RaspberryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LOGGER.info("Raspberry Client Initialized");

    }
}
