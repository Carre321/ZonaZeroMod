package com.zonazeromc.zzkits.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definición de un kit guardado en JSON.
 *
 * Secciones soportadas:
 * - storage
 * - hotbar
 * - armor
 * - utility
 * - tools
 * - backpack
 */
public class KitDefinition {
    public int version = 1;

    public String name = "default";

    /** segundos de cooldown (0 = sin cooldown) */
    public int cooldownSeconds = 0;

    /** si es true, el jugador solo lo puede usar una vez */
    public boolean oneTime = false;

    /** items por sección */
    public Map<String, List<KitSlotItem>> sections = new HashMap<>();
}
