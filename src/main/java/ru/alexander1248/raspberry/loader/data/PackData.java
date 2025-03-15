package ru.alexander1248.raspberry.loader.data;

import com.google.gson.annotations.SerializedName;

public class PackData {

    @SerializedName("force_update")
    public boolean isNeedUpdateImmediately = false;

    @SerializedName("files")
    public ru.alexander1248.raspberry.loader.data.PackFile[] files = new ru.alexander1248.raspberry.loader.data.PackFile[0];
}
