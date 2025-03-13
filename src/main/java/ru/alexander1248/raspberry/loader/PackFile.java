package ru.alexander1248.raspberry.loader;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class PackFile {
    @SerializedName("path")
    public String path;

    @SerializedName("alters")
    public String[] alternativePaths;

    @SerializedName("hashes")
    public Map<String, String> hashes;

    @SerializedName("env")
    public Set<String> environments;

    @SerializedName("download")
    public String downloadUri;

}
