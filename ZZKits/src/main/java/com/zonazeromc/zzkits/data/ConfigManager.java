package com.zonazeromc.zzkits.data;

import com.zonazeromc.zzkits.util.JsonUtil;

import java.io.File;
import java.nio.file.Path;

public class ConfigManager {

    private final File configFile;
    private ConfigData config;

    public ConfigManager(Path dataDirectory) {
        dataDirectory.toFile().mkdirs();
        this.configFile = dataDirectory.resolve("config.json").toFile();
        this.config = new ConfigData();
    }

    public void loadOrCreate() {
        if (!configFile.exists()) {
            this.config = new ConfigData();
            save();
            return;
        }
        this.config = JsonUtil.readJson(configFile, ConfigData.class, new ConfigData());
    }

    public void save() {
        try {
            JsonUtil.writeJson(configFile, config);
        } catch (Exception ignored) {}
    }

    public ConfigData get() {
        return config;
    }

    public File getConfigFile() {
        return configFile;
    }
}
