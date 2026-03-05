package com.zonazeromc.zzkits.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Utilidades de JSON (Gson).
 */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private JsonUtil() {}

    public static Gson gson() {
        return GSON;
    }

    public static <T> T readJson(File file, Class<T> clazz, T fallback) {
        if (file == null || !file.exists()) return fallback;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            T parsed = GSON.fromJson(reader, clazz);
            return parsed != null ? parsed : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public static void writeJson(File file, Object obj) throws IOException {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(obj, writer);
        }
    }
}
