package ru.alexander1248.raspberry.loader;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PackFile {
    @SerializedName("path")
    public String path;

    @SerializedName("alters")
    public String[] alternativePaths;

    @SerializedName("hashes")
    public Map<String, String> hashes;

    @SerializedName("env")
    public Map<String, Boolean> environments;

    @SerializedName("download")
    public String downloadUri;

}
