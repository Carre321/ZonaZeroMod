package com.zonazeromc.zzkits.util;

import java.util.Map;

/**
 * Utilidades de texto:
 * - Color codes con '&' (estilo Minecraft) -> '§'
 * - Reemplazo de placeholders {clave}
 */
public final class TextUtil {

    private TextUtil() {}

    public static String colorize(String input) {
        if (input == null) return "";
        String s = input;

        // Soporta también '§' directo (no lo toca)
        // Convierte &a, &b, &1, &l, &r, etc. en §a...
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                char n = Character.toLowerCase(s.charAt(i + 1));
                if (isColorCodeChar(n)) {
                    out.append('§').append(n);
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static boolean isColorCodeChar(char c) {
        return (c >= '0' && c <= '9')
            || (c >= 'a' && c <= 'f')
            || (c >= 'k' && c <= 'o')
            || c == 'r';
    }

    public static String applyPlaceholders(String template, Map<String, String> placeholders) {
        if (template == null) return "";
        String out = template;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                String key = e.getKey();
                String val = e.getValue() == null ? "" : e.getValue();
                out = out.replace("{" + key + "}", val);
            }
        }
        return out;
    }
}
