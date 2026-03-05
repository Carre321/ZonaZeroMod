package com.zonazeromc.zzkits.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mensajes.json (traducible)
 */
public class MensajesData {
    public int version = 1;

    /** Prefijo para insertar con {prefix} */
    public String prefix = "&7[&bZZKits&7] &r";

    /** Lista de líneas para /kit (ayuda) */
    public List<String> help = new ArrayList<>();

    /** Textos por clave */
    public Map<String, String> texts = new HashMap<>();
}
