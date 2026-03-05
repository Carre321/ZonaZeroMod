package com.zonazeromc.zzkits.data;

/**
 * Config en /mods/ZZKits/config.json
 */
public class ConfigData {

    /** Para migraciones futuras */
    public int version = 2;

    /** Prefijo para mensajes del plugin */
    public String prefix = "&7[&bZZKits&7] &r";

    /** Kit que se entrega automáticamente al primer join */
    public String starterKit = "default";

    /** Dar el kit starter automáticamente al primer join */
    public boolean giveStarterOnFirstJoin = true;

    /**
     * Si es true: sobrescribe slots (hotbar/armor/etc) al dar un kit.
     * Si es false: solo pone si están vacíos y lo demás lo mete a storage/backpack.
     */
    public boolean overwriteSlotsOnGive = false;

    /** Defaults al crear un kit */
    public int defaultCooldownSeconds = 0;
    public boolean defaultOneTime = false;

    /**
     * Qué hacer si NO hay espacio suficiente al entregar un kit:
     *  - "DROP"   -> lo que no quepa se tira al suelo (si es posible)
     *  - "CANCEL" -> no entrega nada y avisa (estricto: requiere huecos suficientes)
     */
    public String inventoryFullMode = "DROP";

    /** Permitir comandos desde consola */
    public boolean allowConsoleCommands = true;

    // ---------------------------
    // GUI (menú de kits)
    // ---------------------------

    /** Activa el menú de kits (/kits o /kit gui) */
    public boolean enableGui = true;

    /**
     * Acción al hacer click sobre un kit en la GUI:
     *  - "CLAIM"  -> intenta entregar el kit inmediatamente
     *  - "SELECT" -> solo muestra la info (sin entregar) [arquitectura probable: útil si luego añades botón]
     */
    public String guiClickAction = "SELECT";

    /** Si es true: la GUI muestra TODOS los kits. Si es false: solo muestra los kits que el jugador puede usar. */
    public boolean guiShowLockedKits = false;

    // ---------------------------
    // Anti-combat
    // ---------------------------

    /** Si es true: bloquea reclamar kits si el jugador está "en combate" */
    public boolean preventKitsInCombat = true;

    /** Tiempo (en segundos) que dura el "combat tag" desde la última acción de combate */
    public int combatTagSeconds = 15;
}
