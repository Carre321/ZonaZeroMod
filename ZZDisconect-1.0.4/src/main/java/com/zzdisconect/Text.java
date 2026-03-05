package com.zzdisconect;

import java.util.HashMap;
import java.util.Map;

public final class Text {
    private Text() {}

    public static String fmt(String template, Map<String, String> vars) {
        String out = template;
        if (vars == null) return out;

        for (Map.Entry<String, String> e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return out;
    }

    public static Map<String, String> vars(Object... kv) {
        Map<String, String> map = new HashMap<>();
        if (kv == null) return map;
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), String.valueOf(kv[i + 1]));
        }
        return map;
    }
}
