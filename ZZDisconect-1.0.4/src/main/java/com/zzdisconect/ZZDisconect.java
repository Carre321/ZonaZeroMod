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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ZZDisconect extends JavaPlugin {

    private final Config<ZZDisconectConfig> config;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ZZDisconect-Scheduler");
                t.setDaemon(true);
                return t;
            });

    private final AtomicBoolean haltRunning = new AtomicBoolean(false);

    private StatusService statusService;
    private PlaceholderService placeholderService;
    private BroadcastService broadcastService;
    private AuditService auditService;
    private CooldownService cooldownService;
    private PermissionService permissionService;

    private ScheduledFuture<?> placeholderRefreshTask;
    private ScheduledFuture<?> pararCountdownTask;
    private ScheduledFuture<?> pararFinishTask;
    private ScheduledFuture<?> persistentAvisoTask;
    private volatile String persistentAvisoMessage;
    private final List<ScheduledFuture<?>> autoRestartTasks = new CopyOnWriteArrayList<>();

    public ZZDisconect(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("config", ZZDisconectConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();

        this.statusService = new StatusService();
        this.placeholderService = new PlaceholderService();
        this.broadcastService = new BroadcastService(this);
        this.auditService = new AuditService(this);
        this.cooldownService = new CooldownService();
        this.permissionService = new PermissionService(this);

        getCommandRegistry().registerCommand(new PararCommand(this));
        getCommandRegistry().registerCommand(new CancelarParadaCommand(this));
        getCommandRegistry().registerCommand(new HaltCommand(this));
        getCommandRegistry().registerCommand(new MoveCommand(this));
        getCommandRegistry().registerCommand(new LobbyCommand(this));
        getCommandRegistry().registerCommand(new AvisosCommand(this));
        getCommandRegistry().registerCommand(new ZZDisconectCommand(this));

        this.getEventRegistry().registerGlobal(PlayerSetupConnectEvent.class, this::onPlayerSetupConnect);

        statusService.setState(getMainConfig().get().isMaintenanceMode() ? ServerState.MAINTENANCE : ServerState.ONLINE);
        placeholderService.refresh(getMainConfig().get(), statusService);

        getLogger().at(Level.INFO).log("ZZDisconect setup OK. /zzparar <mensaje> <tiempo>, /cancelarparada --confirm, /aviso \"mensaje\" [segundos|static], /zzdisconect reload");
    }

    @Override
    protected void start() {
        ZZDisconectConfig cfg = config.get();
        restartScheduledTasks(cfg);
    }

    @Override
    protected void shutdown() {
        if (placeholderRefreshTask != null) {
            placeholderRefreshTask.cancel(false);
        }
        cancelPararTasks();
        cancelPersistentAvisoTask();
        cancelAutoRestartTasks();
        scheduler.shutdownNow();
    }

    private void onPlayerSetupConnect(PlayerSetupConnectEvent event) {
        ZZDisconectConfig cfg = config.get();

        if (!cfg.isRedirectDuringHandshake() || !cfg.isMaintenanceMode()) {
            return;
        }

        if (!isValidTarget(cfg.getRedirectHost(), cfg.getRedirectPort())) {
            getLogger().at(Level.WARNING).log("Handshake redirect skipped due to invalid RedirectHost/RedirectPort configuration.");
            return;
        }

        event.referToServer(cfg.getRedirectHost(), cfg.getRedirectPort());
        event.setReason("Servidor en mantenimiento. Redirigiendo...");
        event.setCancelled(true);
    }

    public Config<ZZDisconectConfig> getMainConfig() {
        return config;
    }

    public synchronized boolean reloadRuntime(String trigger) {
        try {
            config.load().join();
            ZZDisconectConfig cfg = config.get();

            if (!statusService.isShutdownInProgress()) {
                statusService.setState(cfg.isMaintenanceMode() ? ServerState.MAINTENANCE : ServerState.ONLINE);
            }

            placeholderService.refresh(cfg, statusService);
            restartScheduledTasks(cfg);

            auditService.log("plugin.reload", trigger, "ok");
            getLogger().at(Level.INFO).log("ZZDisconect configuration reloaded. trigger=%s", trigger);
            return true;
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("reloadRuntime failed. trigger=%s", trigger);
            return false;
        }
    }

    public PlaceholderService getPlaceholderService() {
        return placeholderService;
    }

    public BroadcastService getBroadcastService() {
        return broadcastService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public CooldownService getCooldownService() {
        return cooldownService;
    }

    public void sendAvisoCenter(String formattedMessage, int durationSeconds, String trigger) {
        int safeSeconds = Math.max(1, durationSeconds);
        broadcastService.globalCenterRaw(formattedMessage, (float) safeSeconds);
        auditService.log("aviso.send", trigger, "mode=timed duration=" + safeSeconds);
    }

    public synchronized void setPersistentAviso(String formattedMessage, String trigger) {
        String msg = formattedMessage == null ? "" : formattedMessage.trim();
        if (msg.isBlank()) {
            return;
        }

        persistentAvisoMessage = msg;
        cancelPersistentAvisoTask();

        final float segmentDurationSeconds = 6.0f;
        broadcastService.globalCenterRaw(msg, segmentDurationSeconds, 0.1f, 0.1f);
        persistentAvisoTask = scheduler.scheduleAtFixedRate(() -> {
            String sticky = persistentAvisoMessage;
            if (sticky == null || sticky.isBlank()) {
                return;
            }
            broadcastService.globalCenterRaw(sticky, segmentDurationSeconds, 0.05f, 0.05f);
        }, 3, 3, TimeUnit.SECONDS);

        auditService.log("aviso.send", trigger, "mode=static");
    }

    public synchronized void clearPersistentAviso(String trigger) {
        persistentAvisoMessage = null;
        cancelPersistentAvisoTask();
        broadcastService.hideCenter(0.1f);
        auditService.log("aviso.clear", trigger, "");
    }

    public boolean isPararRunning() {
        return statusService.isShutdownInProgress();
    }

    public void runPararSequence(String trigger) {
        runPararSequence(trigger, null, null);
    }

    public void runPararSequence(String trigger, String customMessage, Integer customDurationSeconds) {
        ZZDisconectConfig cfg = config.get();

        if (haltRunning.get()) {
            getLogger().at(Level.INFO).log("Cannot start parar while halt sequence is running. Trigger=%s", trigger);
            return;
        }

        if (!statusService.getState().equals(ServerState.ONLINE) && !statusService.getState().equals(ServerState.MAINTENANCE)) {
            getLogger().at(Level.INFO).log("Cannot start parar from state=%s. Trigger=%s", statusService.getState(), trigger);
            return;
        }

        if (statusService.isShutdownInProgress()) {
            getLogger().at(Level.INFO).log("Parar sequence already running. Ignoring trigger=%s", trigger);
            return;
        }

        if (!isValidTarget(cfg.getRedirectTargetHost(), cfg.getRedirectTargetPort())) {
            getLogger().at(Level.WARNING).log("Invalid redirect target host/port. trigger=%s", trigger);
            return;
        }

        statusService.setShutdownInProgress(true);
        statusService.setState(ServerState.SHUTTING_DOWN);
        int duration = Math.max(1, customDurationSeconds != null ? customDurationSeconds : cfg.getPararDurationSeconds());
        statusService.setShutdownEndEpochMs(System.currentTimeMillis() + (duration * 1000L));

        auditService.log("parar.start", trigger, "duration=" + duration + " target=" + cfg.getRedirectTargetHost() + ":" + cfg.getRedirectTargetPort());

        String announceTemplate = customMessage != null && !customMessage.isBlank()
                ? customMessage.trim()
                : Text.fmt(cfg.getMsgPararAnnounce(), Text.vars("delay", duration));
        broadcastService.globalCenterRaw(buildClosingBroadcastMessage(announceTemplate, duration));

        pararCountdownTask = scheduler.scheduleAtFixedRate(() -> {
            int remaining = statusService.getShutdownRemainingSeconds();
            if (remaining <= 0 || !statusService.isShutdownInProgress()) {
                return;
            }
            broadcastService.globalCenterRaw(buildClosingBroadcastMessage(announceTemplate, remaining));
        }, 1, 1, TimeUnit.SECONDS);

        pararFinishTask = scheduler.schedule(() -> finishPararSequence(trigger), duration, TimeUnit.SECONDS);
    }

    public boolean cancelPararSequence(String trigger) {
        if (!statusService.isShutdownInProgress()) {
            return false;
        }

        if (statusService.getState() == ServerState.REDIRECTING) {
            return false;
        }

        cancelPararTasks();
        statusService.setShutdownInProgress(false);
        statusService.clearShutdownTimer();
        statusService.setState(config.get().isMaintenanceMode() ? ServerState.MAINTENANCE : ServerState.ONLINE);

        broadcastService.globalCenterRaw(config.get().getMsgPararCancelled());
        auditService.log("parar.cancel", trigger, "");
        return true;
    }

    private void finishPararSequence(String trigger) {
        ZZDisconectConfig cfg = config.get();

        if (!statusService.isShutdownInProgress()) {
            return;
        }

        statusService.setState(ServerState.REDIRECTING);

        broadcastService.globalCenterRaw(cfg.getMsgPararSending());

        int total = 0;
        int moved = 0;
        int failed = 0;

        List<PlayerRef> players = Universe.get().getPlayers();
        int batchSize = Math.max(1, cfg.getTransferBatchSize());
        int batchDelayMs = Math.max(0, cfg.getTransferBatchDelayMs());

        for (int i = 0; i < players.size(); i += batchSize) {
            int end = Math.min(i + batchSize, players.size());
            for (int j = i; j < end; j++) {
                PlayerRef playerRef = players.get(j);
                if (playerRef == null || !playerRef.isValid()) {
                    continue;
                }
                total++;
                if (redirectPlayerWithFallback(playerRef)) {
                    moved++;
                } else {
                    failed++;
                }
            }

            if (batchDelayMs > 0 && end < players.size()) {
                sleep(batchDelayMs);
            }
        }

        auditService.log("parar.redirect", trigger, "moved=" + moved + " failed=" + failed + " total=" + total);

        sleep(Math.max(0, cfg.getPostTransferDelayMs()));

        broadcastService.globalCenterRaw(cfg.getMsgPararStopping());

        if (cfg.isMarkOfflineAfterRedirect()) {
            statusService.setState(ServerState.OFFLINE);
        } else {
            statusService.setState(ServerState.SHUTTING_DOWN);
        }

        statusService.setShutdownInProgress(false);
        statusService.clearShutdownTimer();
        cancelPararTasks();

        if (cfg.isStopServerAfterRedirect()) {
            CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "stop");
        }
    }

    public void runHaltSequence(String trigger) {
        if (statusService.isShutdownInProgress()) {
            getLogger().at(Level.INFO).log("Cannot start halt while parar sequence is running. Trigger=%s", trigger);
            return;
        }

        if (!haltRunning.compareAndSet(false, true)) {
            getLogger().at(Level.INFO).log("Halt sequence already running. Ignoring trigger=%s", trigger);
            return;
        }

        try {
            ZZDisconectConfig cfg = config.get();

            statusService.setState(ServerState.MAINTENANCE);
            broadcastService.global(cfg.getMsgHaltAnnounce());
            sleep(Math.max(0, cfg.getHaltAnnounceDelayMs()));

            broadcastService.global(cfg.getMsgHaltSending());

            int moved = 0;
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                if (playerRef == null || !playerRef.isValid()) {
                    continue;
                }

                if (permissionService.isOp(playerRef)) {
                    continue;
                }

                if (redirectPlayerWithFallback(playerRef)) {
                    moved++;
                }
            }

            auditService.log("halt.redirect", trigger, "moved=" + moved);

            if (cfg.isHaltEnableWhitelist()) {
                CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "whitelist enable");
                broadcastService.global(cfg.getMsgHaltWhitelistEnabled());
            }
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("runHaltSequence failed");
        } finally {
            haltRunning.set(false);
        }
    }

    private boolean redirectPlayerWithFallback(PlayerRef playerRef) {
        ZZDisconectConfig cfg = config.get();

        int retries = Math.max(0, cfg.getRedirectMaxRetries());
        for (int i = 0; i <= retries; i++) {
            try {
                playerRef.sendMessage(Message.raw(cfg.getPlayerPrefix() + "-> " + cfg.getRedirectTargetHost() + ":" + cfg.getRedirectTargetPort()));
                playerRef.referToServer(cfg.getRedirectTargetHost(), cfg.getRedirectTargetPort());
                return true;
            } catch (Throwable t) {
                if (i < retries) {
                    sleep(Math.max(0, cfg.getRedirectRetryDelayMs()));
                }
            }
        }

        if (cfg.isFallbackOnRedirectFailure() && isValidTarget(cfg.getFallbackHost(), cfg.getFallbackPort())) {
            try {
                // Arquitectura Probable: deteccion de salud de destino/proxy no documentada aun.
                playerRef.sendMessage(Message.raw(cfg.getPlayerPrefix() + "Fallback -> " + cfg.getFallbackHost() + ":" + cfg.getFallbackPort()));
                playerRef.referToServer(cfg.getFallbackHost(), cfg.getFallbackPort());
                return true;
            } catch (Throwable t) {
                getLogger().at(Level.WARNING).withCause(t)
                        .log("Fallback redirect failed for player %s", playerRef.getUuid());
            }
        }

        getLogger().at(Level.WARNING).log("Redirect failed for player %s", playerRef.getUuid());
        return false;
    }

    private void scheduleAutoRestart(int afterSeconds, String warningsCsv) {
        ZZDisconectConfig cfg = config.get();

        List<Integer> warnings = parseCsvSeconds(warningsCsv);
        warnings.sort((a, b) -> Integer.compare(b, a));

        for (int w : warnings) {
            if (w <= 0) {
                continue;
            }
            int delay = afterSeconds - w;
            if (delay < 0) {
                continue;
            }

            ScheduledFuture<Void> f = scheduler.schedule(() -> {
                broadcastService.global(Text.fmt(cfg.getMsgAutoRestartWarning(), Text.vars("seconds", w)));
                return null;
            }, delay, TimeUnit.SECONDS);

            autoRestartTasks.add(f);
            getTaskRegistry().registerTask(f);
        }

        ScheduledFuture<Void> stopTask = scheduler.schedule(() -> {
            broadcastService.global(cfg.getMsgAutoRestartStarting());
            runPararSequence("autoRestart");
            return null;
        }, afterSeconds, TimeUnit.SECONDS);

        autoRestartTasks.add(stopTask);
        getTaskRegistry().registerTask(stopTask);

        getLogger().at(Level.INFO).log("AutoRestart scheduled in %ds with warnings: %s", afterSeconds, warningsCsv);
    }

    private void restartScheduledTasks(ZZDisconectConfig cfg) {
        if (scheduler.isShutdown()) {
            return;
        }

        if (placeholderRefreshTask != null) {
            placeholderRefreshTask.cancel(false);
        }

        int refresh = Math.max(1, cfg.getPlaceholderRefreshIntervalSeconds());
        placeholderRefreshTask = scheduler.scheduleAtFixedRate(
                () -> placeholderService.refresh(config.get(), statusService),
                1,
                refresh,
                TimeUnit.SECONDS
        );

        cancelAutoRestartTasks();
        if (cfg.isAutoRestartEnabled() && cfg.getAutoRestartAfterSeconds() > 0) {
            scheduleAutoRestart(cfg.getAutoRestartAfterSeconds(), cfg.getAutoRestartWarningsSeconds());
        }
    }

    private void cancelAutoRestartTasks() {
        for (ScheduledFuture<?> task : autoRestartTasks) {
            if (task != null) {
                task.cancel(false);
            }
        }
        autoRestartTasks.clear();
    }

    private void cancelPersistentAvisoTask() {
        if (persistentAvisoTask != null) {
            persistentAvisoTask.cancel(false);
            persistentAvisoTask = null;
        }
    }

    private void cancelPararTasks() {
        if (pararCountdownTask != null) {
            pararCountdownTask.cancel(false);
            pararCountdownTask = null;
        }
        if (pararFinishTask != null) {
            pararFinishTask.cancel(false);
            pararFinishTask = null;
        }
    }

    private String buildClosingBroadcastMessage(String baseMessage, int remainingSeconds) {
        String safeBase = (baseMessage == null || baseMessage.isBlank()) ? "El servidor se esta cerrando." : baseMessage.trim();
        return "El servidor se esta cerrando. " + safeBase + " Tiempo restante: " + remainingSeconds + "s.";
    }

    private List<Integer> parseCsvSeconds(String csv) {
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) {
            return new ArrayList<>();
        }

        for (String part : csv.split(",")) {
            try {
                int v = Integer.parseInt(part.trim());
                if (v > 0) {
                    values.add(v);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<>(values);
    }

    public boolean isValidTarget(String host, int port) {
        return host != null && !host.isBlank() && port > 0 && port <= 65535;
    }

    private void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
