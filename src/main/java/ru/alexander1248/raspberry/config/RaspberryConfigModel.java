package ru.alexander1248.raspberry.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import ru.alexander1248.raspberry.Raspberry;

@Modmenu(modId = Raspberry.MOD_ID)
@Config(name = "raspberry-config", wrapperName = "RaspberryConfig")
public class RaspberryConfigModel {
    public String clientModListUri = "";
}
