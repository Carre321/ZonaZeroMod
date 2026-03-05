package com.zonazeromc.zzkits.data;

import com.zonazeromc.zzkits.util.JsonUtil;
import com.zonazeromc.zzkits.util.TextUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga / crea / guarda mensajes.json
 * - Soporta códigos de color con '&' (se convierten a '§' al enviar)
 * - Soporta placeholders {clave} (incluye {prefix})
 */
public class MensajesManager {

    private final File file;
    private MensajesData data;

    public MensajesManager(Path dataDirectory) {
        this.file = dataDirectory.resolve("mensajes.json").toFile();
    }

    public MensajesData get() {
        if (data == null) data = defaults();
        return data;
    }

    public synchronized void loadOrCreate() {
        if (!file.exists()) {
            data = defaults();
            save();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            MensajesData parsed = JsonUtil.gson().fromJson(reader, MensajesData.class);
            if (parsed == null) parsed = defaults();
            data = mergeDefaults(parsed);
        } catch (Exception e) {
            data = defaults();
        }

        // Guardar por si faltaban claves
        save();
    }

    public synchronized void save() {
        try {
            JsonUtil.writeJson(file, get());
        } catch (Exception ignored) {}
    }

    public String prefix() {
        return TextUtil.colorize(get().prefix);
    }

    public String text(String key) {
        String s = get().texts.get(key);
        return s != null ? s : ("&c<mensaje faltante: " + key + ">");
    }

    public String format(String key, Map<String, String> placeholders) {
        Map<String, String> ph = new HashMap<>();
        if (placeholders != null) ph.putAll(placeholders);
        ph.putIfAbsent("prefix", get().prefix);

        String raw = TextUtil.applyPlaceholders(text(key), ph);
        return TextUtil.colorize(raw);
    }

    public void send(CommandContext ctx, String key) {
        ctx.sendMessage(Message.raw(format(key, null)));
    }

    public void send(CommandContext ctx, String key, Map<String, String> placeholders) {
        ctx.sendMessage(Message.raw(format(key, placeholders)));
    }

    public void send(Player player, String key, Map<String, String> placeholders) {
        if (player == null) return;
        player.sendMessage(Message.raw(format(key, placeholders)));
    }

    public void send(Player player, String key) {
        if (player == null) return;
        player.sendMessage(Message.raw(format(key, null)));
    }

    /** Devuelve el texto ya formateado para concatenarlo si hace falta */
    public String fmt(String key, Map<String, String> placeholders) {
        return format(key, placeholders);
    }

    private MensajesData mergeDefaults(MensajesData current) {
        MensajesData def = defaults();

        if (current.prefix == null || current.prefix.isEmpty()) current.prefix = def.prefix;

        if (current.help == null || current.help.isEmpty()) current.help = def.help;

        if (current.texts == null) current.texts = new HashMap<>();
        for (Map.Entry<String, String> e : def.texts.entrySet()) {
            current.texts.putIfAbsent(e.getKey(), e.getValue());
        }

        return current;
    }

    private MensajesData defaults() {
        MensajesData d = new MensajesData();

        // Help (/kit)
        d.help.add("{prefix}&eComandos:");
        d.help.add("{prefix}&7/kit <nombre> &f→ obtiene un kit (permiso: &e kit.<nombre> &f o &e kit.admin&f)");
        d.help.add("{prefix}&7/kit list &f→ lista kits");
        d.help.add("{prefix}&7/kits &f→ abre el menú de kits (GUI)");
        d.help.add("{prefix}&7/kit gui &f→ abre el menú de kits (GUI)");
        d.help.add("{prefix}&7/kit get <kit> <jugador> &f→ da un kit a un jugador (&ekit.admin&f)");
        d.help.add("{prefix}&7/kit create <kit> [cooldownSeg] [oneTime] [jugador] &f→ crea/actualiza kit desde inventario (&ekit.admin&f)");
        d.help.add("{prefix}&7/kit delete <kit> &f→ borra un kit (&ekit.admin&f)");
        d.help.add("{prefix}&7/kit reload &f→ recarga config/kits/mensajes (&ekit.admin&f)");
        d.help.add("{prefix}&7/kitsetstarter [jugador] &f→ guarda el kit starter (&ekit.admin&f)");

        // Textos
        d.texts.put("consola_desactivada", "&cLos comandos desde consola están desactivados en config.json.");
        d.texts.put("gui_solo_jugador", "{prefix}&cEste comando solo puede usarse en el juego (no desde consola).");
        d.texts.put("gui_abierto", "{prefix}&7Abriendo menú de kits...");
        d.texts.put("gui_selecciona_kit", "{prefix}&cSelecciona un kit primero.");
        d.texts.put("gui_sin_kits", "{prefix}&cNo tienes kits disponibles.");
        d.texts.put("no_permiso_admin", "{prefix}&cNo tienes permiso. Requiere &ekit.admin&c.");
        d.texts.put("reload_ok", "{prefix}&aZZKits recargado correctamente.");
        d.texts.put("lista_vacia", "{prefix}&cNo hay kits creados. Usa &e/kit create <nombre>&c o &e/kitsetstarter&c.");
        d.texts.put("lista_kits", "{prefix}&eKits ({count}): &f{kits}");
        d.texts.put("uso_kit_get", "{prefix}&cUso: &e/kit get <kit> <jugador>");
        d.texts.put("kit_no_existe", "{prefix}&cEse kit no existe: &e{kit}");
        d.texts.put("error_objetivo", "{prefix}&cError: no se pudo obtener el jugador objetivo.");
        d.texts.put("error_inventario", "{prefix}&cError: el jugador no tiene inventario.");
        d.texts.put("kit_guardado", "{prefix}&aKit guardado: &e{kit}&a (cooldown={cooldown}s, oneTime={oneTime})");
        d.texts.put("kit_permiso_info", "{prefix}&7Permiso para usarlo: &e kit.{kit}&7 (o &e kit.admin&7)");
        d.texts.put("error_guardando_kit", "{prefix}&cError guardando el kit: &e{error}");
        d.texts.put("kit_eliminado", "{prefix}&aKit eliminado: &e{kit}");
        d.texts.put("kit_eliminar_fail", "{prefix}&cNo se pudo eliminar (¿no existe?) kit: &e{kit}");
        d.texts.put("starter_guardado", "{prefix}&aKit starter guardado como &e{kit}&a.");
        d.texts.put("starter_info", "{prefix}&7Los nuevos jugadores lo recibirán al unirse (si giveStarterOnFirstJoin=true).");
        d.texts.put("error_guardando_starter", "{prefix}&cError guardando el kit starter: &e{error}");

        d.texts.put("target_requerido", "{prefix}&cDebes especificar un jugador (desde consola) o ejecutar el comando como jugador.");
        d.texts.put("target_ref_invalida", "{prefix}&cEl jugador no está en el mundo (ref inválida).");

        d.texts.put("no_puedes_dar_otro", "{prefix}&cNo puedes dar kits a otros jugadores. Requiere &ekit.admin&c.");
        d.texts.put("no_permiso_kit", "{prefix}&cNo tienes permiso para usar ese kit. Requiere &ekit.{kit}&c.");
        d.texts.put("one_time_usado", "{prefix}&cEste kit es de 1 solo uso y ya lo has usado.");
        d.texts.put("cooldown", "{prefix}&cDebes esperar &e{secs}&c segundo(s) para volver a usar ese kit.");
        d.texts.put("en_combate", "{prefix}&cNo puedes usar kits estando en combate. Espera &e{secs}&c segundo(s).");
        d.texts.put("kit_entregado", "{prefix}&aKit &e{kit}&a entregado a &e{player}&a (&e{count}&a ítems).");
        d.texts.put("kit_recibido", "{prefix}&aHas recibido el kit &e{kit}&a.");

        d.texts.put("kit_recibido_admin", "{prefix}&aHas recibido el kit &e{kit}&a de un admin.");
        d.texts.put("starter_recibido_join", "{prefix}&a¡Bienvenido! Has recibido el kit &e{kit}&a con &e{count}&a ítem(s).");

        d.texts.put("inventario_sin_espacio", "{prefix}&cNo tienes espacio suficiente en el inventario para recibir este kit.");
        d.texts.put("objetivo_sin_espacio", "{prefix}&c{player} no tiene espacio suficiente en el inventario. No se entregó el kit.");
        d.texts.put("items_tirados_suelo", "{prefix}&eInventario lleno: &f{dropped}&e item(s) se tiraron al suelo.");
        d.texts.put("items_tirados_suelo_otro", "{prefix}&eA &f{player}&e se le tiraron &f{dropped}&e item(s) al suelo por falta de espacio.");


        return d;
    }
}
