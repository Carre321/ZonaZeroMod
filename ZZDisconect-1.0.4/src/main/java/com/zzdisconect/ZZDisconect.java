package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * ZZDisconect 1.0.4
 *
 * - /parar --confirm:
 *    1) broadcast announcement
 *    2) wait PararAnnounceDelayMs (+ optional countdown)
 *    3) refer ALL players to fallback
 *    4) wait PostTransferDelayMs
 *    5) execute built-in "stop" command (console)
 *
 * - /halt --confirm:
 *    1) broadcast announcement
 *    2) wait HaltAnnounceDelayMs
 *    3) refer NON-OP players to fallback (ops stay)
 *    4) enable whitelist (optional) by executing "whitelist enable" (console)
 *
 * - AutoRestart:
 *    after AutoRestartAfterSeconds, sends warnings at configured offsets then runs the same /parar flow.
 */
public class ZZDisconect extends JavaPlugin {

    private final Config<ZZDisconectConfig> config;
    private final Config<ZZDisconectMessages> messages;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ZZDisconect-Scheduler");
                t.setDaemon(true);
                return t;
            });

    private final AtomicBoolean pararRunning = new AtomicBoolean(false);
    private final AtomicBoolean haltRunning = new AtomicBoolean(false);

    public ZZDisconect(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("ZZDisconect", ZZDisconectConfig.CODEC);

        // This creates "messages.json" in the plugin data directory.
        this.messages = this.withConfig("messages", ZZDisconectMessages.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();
        this.messages.save();

        // Commands
        getCommandRegistry().registerCommand(new PararCommand(this));
        getCommandRegistry().registerCommand(new HaltCommand(this));
        getCommandRegistry().registerCommand(new MoveCommand(this));
        getCommandRegistry().registerCommand(new LobbyCommand(this));

        // Handshake redirect (optional)
        this.getEventRegistry().registerGlobal(PlayerSetupConnectEvent.class, this::onPlayerSetupConnect);

        getLogger().at(Level.INFO).log("ZZDisconect setup OK. Use /parar --confirm or /halt --confirm.");
    }

    @Override
    protected void start() {
        // Auto restart schedule (single cycle until stop happens)
        ZZDisconectConfig cfg = config.get();
        if (cfg.isAutoRestartEnabled() && cfg.getAutoRestartAfterSeconds() > 0) {
            scheduleAutoRestart(cfg.getAutoRestartAfterSeconds(), cfg.getAutoRestartWarningsSeconds());
        }
    }

    @Override
    protected void shutdown() {
        scheduler.shutdownNow();
    }

    private void onPlayerSetupConnect(PlayerSetupConnectEvent event) {
        ZZDisconectConfig cfg = config.get();

        if (!cfg.isRedirectDuringHandshake() || !cfg.isMaintenanceMode()) return;

        if (!isValidTarget(cfg.getRedirectHost(), cfg.getRedirectPort())) {
            getLogger().at(Level.WARNING).log("Handshake redirect skipped due to invalid RedirectHost/RedirectPort configuration.");
            return;
        }

        event.referToServer(cfg.getRedirectHost(), cfg.getRedirectPort());
        event.setReason("Servidor en mantenimiento. Redirigiendo…");
        event.setCancelled(true);
    }

    // ===== Public accessors =====
    public Config<ZZDisconectConfig> getMainConfig() { return config; }

    // ===== /parar flow =====
    public void runPararSequence(String trigger) {
        if (haltRunning.get()) {
            getLogger().at(Level.INFO).log("Cannot start parar while halt sequence is running. Trigger=%s", trigger);
            return;
        }

        if (!pararRunning.compareAndSet(false, true)) {
            getLogger().at(Level.INFO).log("Parar sequence already running. Ignoring trigger=%s", trigger);
            return;
        }

        try {
            ZZDisconectConfig cfg = config.get();
            ZZDisconectMessages msg = messages.get();

            int delayMs = Math.max(0, cfg.getPararAnnounceDelayMs());
            int delaySec = (int) Math.ceil(delayMs / 1000.0);

            broadcast(prefix(msg) + Text.fmt(msg.getPararAnnounce(), Text.vars("delay", delaySec)));

            // Optional countdown broadcasts
            int every = Math.max(0, cfg.getPararCountdownEverySeconds());
            if (delaySec > 0 && every > 0) {
                countdown(delaySec, every, (remaining) ->
                        prefix(msg) + Text.fmt(msg.getPararCountdown(), Text.vars("seconds", remaining)));
            } else if (delayMs > 0) {
                sleep(delayMs);
            }

            broadcast(prefix(msg) + msg.getPararSending());
            int moved = referPlayers(false); // false => move ALL
            getLogger().at(Level.INFO).log("Parar: referred %d players. Trigger=%s", moved, trigger);

            sleep(Math.max(0, cfg.getPostTransferDelayMs()));

            broadcast(prefix(msg) + msg.getPararStopping());

            // Execute built-in /stop
            CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "stop");

        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("runPararSequence failed");
        } finally {
            pararRunning.set(false);
        }
    }

    // ===== /halt flow =====
    public void runHaltSequence(String trigger) {
        if (pararRunning.get()) {
            getLogger().at(Level.INFO).log("Cannot start halt while parar sequence is running. Trigger=%s", trigger);
            return;
        }

        if (!haltRunning.compareAndSet(false, true)) {
            getLogger().at(Level.INFO).log("Halt sequence already running. Ignoring trigger=%s", trigger);
            return;
        }

        try {
            ZZDisconectConfig cfg = config.get();
            ZZDisconectMessages msg = messages.get();

            broadcast(prefix(msg) + msg.getHaltAnnounce());

            sleep(Math.max(0, cfg.getHaltAnnounceDelayMs()));

            broadcast(prefix(msg) + msg.getHaltSending());

            int moved = referPlayers(true); // true => only non-op
            getLogger().at(Level.INFO).log("Halt: referred %d non-op players. Trigger=%s", moved, trigger);

            if (cfg.isHaltEnableWhitelist()) {
                // Built-in whitelist command (exists in server jar): "whitelist enable"
                CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "whitelist enable");
                broadcast(prefix(msg) + msg.getHaltWhitelistEnabled());
            }
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("runHaltSequence failed");
        } finally {
            haltRunning.set(false);
        }
    }

    /**
     * Refer players to the fallback host/port.
     *
     * @param onlyNonOp if true, moves only players NOT in the op group (config OpGroupName)
     */
    private int referPlayers(boolean onlyNonOp) {
        ZZDisconectConfig cfg = config.get();
        if (!isValidTarget(cfg.getFallbackHost(), cfg.getFallbackPort())) {
            getLogger().at(Level.SEVERE).log("Fallback redirection aborted due to invalid FallbackHost/FallbackPort configuration.");
            return 0;
        }

        int moved = 0;

        List<PlayerRef> players = Universe.get().getPlayers();
        for (PlayerRef playerRef : players) {
            if (playerRef == null || !playerRef.isValid()) continue;

            if (onlyNonOp && isOp(playerRef)) {
                continue;
            }

            try {
                playerRef.sendMessage(Message.raw(prefix(messages.get()) + "→ " + cfg.getFallbackHost() + ":" + cfg.getFallbackPort()));
                playerRef.referToServer(cfg.getFallbackHost(), cfg.getFallbackPort());
                moved++;
            } catch (Throwable t) {
                getLogger().at(Level.WARNING).withCause(t)
                        .log("Failed to refer player %s to fallback server", playerRef.getUuid());
            }
        }

        return moved;
    }

    /**
     * Determine OP status via PermissionsModule groups.
     */
    private boolean isOp(PlayerRef playerRef) {
        try {
            String opGroup = config.get().getOpGroupName();
            return PermissionsModule.get().getGroupsForUser(playerRef.getUuid()).contains(opGroup);
        } catch (Throwable t) {
            // If permissions module isn't ready for some reason, default to "not op"
            return false;
        }
    }

    // ===== Auto Restart =====
    private void scheduleAutoRestart(int afterSeconds, String warningsCsv) {
        ZZDisconectMessages msg = messages.get();

        // schedule warning broadcasts
        List<Integer> warnings = parseCsvSeconds(warningsCsv);
        // ensure unique and sorted descending (e.g. 600,300,60...)
        warnings.sort((a, b) -> Integer.compare(b, a));

        for (int w : warnings) {
            if (w <= 0) continue;
            int delay = afterSeconds - w;
            if (delay < 0) continue;

            ScheduledFuture<Void> f = scheduler.schedule(() -> {
                broadcast(prefix(msg) + Text.fmt(msg.getAutoRestartWarning(), Text.vars("seconds", w)));
                return null;
            }, delay, TimeUnit.SECONDS);

            getTaskRegistry().registerTask(f);
        }

        // schedule the actual parar sequence
        ScheduledFuture<Void> stopTask = scheduler.schedule(() -> {
            broadcast(prefix(msg) + msg.getAutoRestartStarting());
            runPararSequence("autoRestart");
            return null;
        }, afterSeconds, TimeUnit.SECONDS);

        getTaskRegistry().registerTask(stopTask);

        getLogger().at(Level.INFO).log("AutoRestart scheduled in %ds with warnings: %s", afterSeconds, warningsCsv);
    }

    private List<Integer> parseCsvSeconds(String csv) {
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return new ArrayList<>();

        for (String part : csv.split(",")) {
            try {
                int v = Integer.parseInt(part.trim());
                if (v > 0) values.add(v);
            } catch (NumberFormatException ignored) {}
        }
        return new ArrayList<>(values);
    }

    private boolean isValidTarget(String host, int port) {
        return host != null && !host.isBlank() && port > 0 && port <= 65535;
    }

    // ===== Messaging helpers =====
    private String prefix(ZZDisconectMessages msg) {
        return msg.getPrefix() == null ? "" : msg.getPrefix();
    }

    private void broadcast(String text) {
        for (PlayerRef p : Universe.get().getPlayers()) {
            if (p == null || !p.isValid()) continue;
            p.sendMessage(Message.raw(text));
        }
    }

    private void countdown(int totalSeconds, int everySeconds, java.util.function.IntFunction<String> messageFn) {
        // Broadcast at totalSeconds, totalSeconds-every, ... > 0
        int remaining = totalSeconds;
        while (remaining > 0) {
            if (remaining == totalSeconds || (everySeconds > 0 && (remaining % everySeconds == 0))) {
                broadcast(messageFn.apply(remaining));
            }
            sleep(1000);
            remaining--;
        }
    }

    private void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
