package ru.alexander1248.raspberry.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import ru.alexander1248.raspberry.Raspberry;

@Modmenu(modId = Raspberry.MOD_ID)
@Config(name = "raspberry-config", wrapperName = "RaspberryConfig")
public class RaspberryConfigModel {
    public String modListUri = "https://github.com/Alexander1248/RaspberryMod/raw/refs/heads/main/data/index.json";
    public float connectionTimeout = 1;
    public int connectionRetry = 3;
}
