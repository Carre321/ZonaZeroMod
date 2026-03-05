package com.zonazeromc.zzkits.data;

import java.util.HashMap;
import java.util.Map;

/**
 * usage.json
 * Guarda el último uso (epoch millis) por kit y por jugador.
 */
public class UsageData {
    public int version = 1;

    /** kitName -> (uuidString -> lastUsedEpochMillis) */
    public Map<String, Map<String, Long>> kits = new HashMap<>();
}
