package ru.alexander1248.raspberry.loader.data;

import com.google.gson.annotations.SerializedName;

public class PackData {

    @SerializedName("force_update")
    public boolean isNeedUpdateImmediately = false;

    @SerializedName("files")
    public PackFile[] files = new PackFile[0];
}
