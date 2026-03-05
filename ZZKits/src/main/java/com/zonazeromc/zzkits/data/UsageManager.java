package com.zonazeromc.zzkits.data;

import com.zonazeromc.zzkits.util.JsonUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class UsageManager {

    private final File usageFile;
    private UsageData data;

    public UsageManager(Path dataDirectory) {
        dataDirectory.toFile().mkdirs();
        this.usageFile = dataDirectory.resolve("usage.json").toFile();
        this.data = new UsageData();
    }

    public void loadOrCreate() {
        if (!usageFile.exists()) {
            this.data = new UsageData();
            save();
            return;
        }
        this.data = JsonUtil.readJson(usageFile, UsageData.class, new UsageData());
        if (this.data == null) this.data = new UsageData();
        if (this.data.kits == null) this.data.kits = new java.util.HashMap<>();
    }

    public synchronized void save() {
        try {
            JsonUtil.writeJson(usageFile, data);
        } catch (Exception ignored) {}
    }

    public synchronized boolean hasEverUsed(String kitName, UUID uuid) {
        Map<String, Long> kitMap = data.kits.get(kitName.toLowerCase());
        return kitMap != null && kitMap.containsKey(uuid.toString());
    }

    public synchronized long getLastUsed(String kitName, UUID uuid) {
        Map<String, Long> kitMap = data.kits.get(kitName.toLowerCase());
        if (kitMap == null) return -1L;
        return kitMap.getOrDefault(uuid.toString(), -1L);
    }

    public synchronized void markUsedNow(String kitName, UUID uuid) {
        String key = kitName.toLowerCase();
        data.kits.computeIfAbsent(key, k -> new java.util.HashMap<>())
            .put(uuid.toString(), System.currentTimeMillis());
    }

    public synchronized void clearKit(String kitName) {
        data.kits.remove(kitName.toLowerCase());
    }

    public File getUsageFile() { return usageFile; }
}
