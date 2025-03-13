package ru.alexander1248.raspberry.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import ru.alexander1248.raspberry.Raspberry;

import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

@Modmenu(modId = Raspberry.MOD_ID)
@Config(name = "raspberry-config", wrapperName = "RaspberryConfig")
public class RaspberryConfigModel {
    public String modListUri = "https://raw.githubusercontent.com/Alexander1248/RaspberryMod/refs/heads/main/data/index.json";
    public long connectionTimeout = 10;
    public TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;
    public int connectionRetry = 3;
}
