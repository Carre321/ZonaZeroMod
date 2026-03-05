package com.zonazeromc.zzkits;

import com.zonazeromc.zzkits.commands.KitCommand;
import com.zonazeromc.zzkits.commands.KitsCommand;
import com.zonazeromc.zzkits.commands.KitSetStarterCommand;
import com.zonazeromc.zzkits.data.ConfigManager;
import com.zonazeromc.zzkits.data.KitManager;
import com.zonazeromc.zzkits.data.UsageManager;
import com.zonazeromc.zzkits.listeners.PlayerReadyListener;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import com.zonazeromc.zzkits.data.MensajesManager;

/**
 * ZZKits (antes StarterKitPlus)
 * - Multiples kits guardados en JSON
 * - Cooldowns y kits de 1 solo uso
 * - Comandos /kit ... y /kitsetstarter
 */
public final class ZZKits extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static ZZKits instance;

    private ConfigManager configManager;
    private KitManager kitManager;
    private UsageManager usageManager;

    private MensajesManager mensajesManager;

    public ZZKits(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static ZZKits instance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("[ZZKits] Inicializando...");

        reloadAll();

        // Comandos
        getCommandRegistry().registerCommand(new KitCommand());
        // /kits (GUI)
        getCommandRegistry().registerCommand(new KitsCommand());
        KitSetStarterCommand kitSetStarter = new KitSetStarterCommand();
        kitSetStarter.addAliases("setkitstarter"); // compatibilidad con el comando viejo
        getCommandRegistry().registerCommand(kitSetStarter);

        // Eventos
        getEventRegistry().registerGlobal(
            PlayerReadyEvent.class,
            PlayerReadyListener::onPlayerReady
        );

        LOGGER.atInfo().log("[ZZKits] Plugin iniciado correctamente.");
    }

    @Override
    protected void shutdown() {
        try {
            if (usageManager != null) usageManager.save();
        } catch (Exception e) {
            LOGGER.atWarning().log("[ZZKits] Error guardando usage.json: %s", e.getMessage());
        }
        LOGGER.atInfo().log("[ZZKits] Plugin detenido.");
    }

    /**
     * Recarga config + kits + usage desde disco.
     * Se usa en el comando /kit reload
     */
    public synchronized void reloadAll() {
        // Managers
        this.configManager = new ConfigManager(getDataDirectory());
        this.kitManager = new KitManager(getDataDirectory(), configManager);
        this.usageManager = new UsageManager(getDataDirectory());

        // Cargar / migrar
        configManager.loadOrCreate();
        this.mensajesManager = new MensajesManager(getDataDirectory());
        mensajesManager.loadOrCreate();
        kitManager.loadAll();
        usageManager.loadOrCreate();

        // Migración desde StarterKitPlus (.txt -> .json)
        kitManager.migrateLegacyIfNeeded(usageManager);

        // Guardar si se ha creado algo
        usageManager.save();
        configManager.save();
        mensajesManager.save();
    }

    public ConfigManager getConfigManager() { return configManager; }
    public KitManager getKitManager() { return kitManager; }
    public UsageManager getUsageManager() { return usageManager; }
    public MensajesManager getMensajes() { return mensajesManager; }
}