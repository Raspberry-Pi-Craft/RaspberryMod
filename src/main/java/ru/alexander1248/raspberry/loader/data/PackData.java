package ru.alexander1248.raspberry.loader.data;

import com.google.gson.annotations.SerializedName;

public class PackData {

    @SerializedName("version")
    public String version = "undefined";
    @SerializedName("force_updates")
    public String[] forceUpdates = new String[0];

    @SerializedName("files")
    public ru.alexander1248.raspberry.loader.data.PackFile[] files = new ru.alexander1248.raspberry.loader.data.PackFile[0];
}
