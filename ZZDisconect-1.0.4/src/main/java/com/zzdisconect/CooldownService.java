package com.zzdisconect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {

    private final Map<String, Long> lastByKey = new ConcurrentHashMap<>();

    public boolean allow(String key, int cooldownSeconds) {
        long now = System.currentTimeMillis();
        long cooldownMs = Math.max(0, cooldownSeconds) * 1000L;
        Long prev = lastByKey.get(key);
        if (prev != null && (now - prev) < cooldownMs) {
            return false;
        }
        lastByKey.put(key, now);
        return true;
    }
}
