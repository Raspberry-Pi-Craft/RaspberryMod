package ru.alexander1248.raspberry.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

import java.util.concurrent.TimeUnit;

@Modmenu(modId = "raspberry")
@Config(name = "raspberry-updater", wrapperName = "RaspberryConfig")
public class RaspberryConfigModel {
    public String modListUri = "https://raw.githubusercontent.com/Raspberry-Pi-Craft/RaspberryMod/refs/heads/main/data/index_v2.json";
    public long connectionTimeout = 10;
    public TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;
    public int connectionRetry = 3;
    public boolean autoReload = true;
    public boolean updateOnLoad = false;
}
