package com.zzdisconect;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ZZDisconectConfig {

    public static final BuilderCodec<ZZDisconectConfig> CODEC =
            BuilderCodec.builder(ZZDisconectConfig.class, ZZDisconectConfig::new)

                    .append(new KeyedCodec<>("ModName", Codec.STRING), (c, v) -> c.modName = v, c -> c.modName).add()
                    .append(new KeyedCodec<>("ConfigVersion", Codec.STRING), (c, v) -> c.configVersion = v, c -> c.configVersion).add()
                    .append(new KeyedCodec<>("ModVersion", Codec.STRING), (c, v) -> c.modVersion = v, c -> c.modVersion).add()
                    .append(new KeyedCodec<>("Debug", Codec.BOOLEAN), (c, v) -> c.debug = v, c -> c.debug).add()

                    .append(new KeyedCodec<>("ServerName", Codec.STRING), (c, v) -> c.serverName = v, c -> c.serverName).add()
                    .append(new KeyedCodec<>("ServerId", Codec.STRING), (c, v) -> c.serverId = v, c -> c.serverId).add()

                    .append(new KeyedCodec<>("PlayerPrefix", Codec.STRING), (c, v) -> c.playerPrefix = v, c -> c.playerPrefix).add()
                    .append(new KeyedCodec<>("ConsolePrefix", Codec.STRING), (c, v) -> c.consolePrefix = v, c -> c.consolePrefix).add()

                    .append(new KeyedCodec<>("PermissionParar", Codec.STRING), (c, v) -> c.permissionParar = v, c -> c.permissionParar).add()
                    .append(new KeyedCodec<>("PermissionCancelarParada", Codec.STRING), (c, v) -> c.permissionCancelarParada = v, c -> c.permissionCancelarParada).add()
                    .append(new KeyedCodec<>("PermissionAvisos", Codec.STRING), (c, v) -> c.permissionAvisos = v, c -> c.permissionAvisos).add()

                    .append(new KeyedCodec<>("PararEnabled", Codec.BOOLEAN), (c, v) -> c.pararEnabled = v, c -> c.pararEnabled).add()
                    .append(new KeyedCodec<>("PararDurationSeconds", Codec.INTEGER), (c, v) -> c.pararDurationSeconds = v, c -> c.pararDurationSeconds).add()
                    .append(new KeyedCodec<>("PararBroadcastIntervalsSeconds", Codec.STRING), (c, v) -> c.pararBroadcastIntervalsSeconds = v, c -> c.pararBroadcastIntervalsSeconds).add()
                    .append(new KeyedCodec<>("PararAnnounceDelayMs", Codec.INTEGER), (c, v) -> c.pararAnnounceDelayMs = v, c -> c.pararAnnounceDelayMs).add()
                    .append(new KeyedCodec<>("PararCountdownEverySeconds", Codec.INTEGER), (c, v) -> c.pararCountdownEverySeconds = v, c -> c.pararCountdownEverySeconds).add()
                    .append(new KeyedCodec<>("PostTransferDelayMs", Codec.INTEGER), (c, v) -> c.postTransferDelayMs = v, c -> c.postTransferDelayMs).add()
                    .append(new KeyedCodec<>("MarkOfflineAfterRedirect", Codec.BOOLEAN), (c, v) -> c.markOfflineAfterRedirect = v, c -> c.markOfflineAfterRedirect).add()
                    .append(new KeyedCodec<>("StopServerAfterRedirect", Codec.BOOLEAN), (c, v) -> c.stopServerAfterRedirect = v, c -> c.stopServerAfterRedirect).add()

                    .append(new KeyedCodec<>("RedirectTargetHost", Codec.STRING), (c, v) -> c.redirectTargetHost = v, c -> c.redirectTargetHost).add()
                    .append(new KeyedCodec<>("RedirectTargetPort", Codec.INTEGER), (c, v) -> c.redirectTargetPort = v, c -> c.redirectTargetPort).add()
                    .append(new KeyedCodec<>("FallbackHost", Codec.STRING), (c, v) -> c.fallbackHost = v, c -> c.fallbackHost).add()
                    .append(new KeyedCodec<>("FallbackPort", Codec.INTEGER), (c, v) -> c.fallbackPort = v, c -> c.fallbackPort).add()
                    .append(new KeyedCodec<>("AllowedDestinationServers", Codec.STRING), (c, v) -> c.allowedDestinationServers = v, c -> c.allowedDestinationServers).add()
                    .append(new KeyedCodec<>("RedirectMaxRetries", Codec.INTEGER), (c, v) -> c.redirectMaxRetries = v, c -> c.redirectMaxRetries).add()
                    .append(new KeyedCodec<>("RedirectRetryDelayMs", Codec.INTEGER), (c, v) -> c.redirectRetryDelayMs = v, c -> c.redirectRetryDelayMs).add()
                    .append(new KeyedCodec<>("FallbackOnRedirectFailure", Codec.BOOLEAN), (c, v) -> c.fallbackOnRedirectFailure = v, c -> c.fallbackOnRedirectFailure).add()
                    .append(new KeyedCodec<>("TransferBatchSize", Codec.INTEGER), (c, v) -> c.transferBatchSize = v, c -> c.transferBatchSize).add()
                    .append(new KeyedCodec<>("TransferBatchDelayMs", Codec.INTEGER), (c, v) -> c.transferBatchDelayMs = v, c -> c.transferBatchDelayMs).add()

                    .append(new KeyedCodec<>("HaltEnabled", Codec.BOOLEAN), (c, v) -> c.haltEnabled = v, c -> c.haltEnabled).add()
                    .append(new KeyedCodec<>("HaltAnnounceDelayMs", Codec.INTEGER), (c, v) -> c.haltAnnounceDelayMs = v, c -> c.haltAnnounceDelayMs).add()
                    .append(new KeyedCodec<>("HaltEnableWhitelist", Codec.BOOLEAN), (c, v) -> c.haltEnableWhitelist = v, c -> c.haltEnableWhitelist).add()
                    .append(new KeyedCodec<>("OpGroupName", Codec.STRING), (c, v) -> c.opGroupName = v, c -> c.opGroupName).add()

                    .append(new KeyedCodec<>("RedirectDuringHandshake", Codec.BOOLEAN), (c, v) -> c.redirectDuringHandshake = v, c -> c.redirectDuringHandshake).add()
                    .append(new KeyedCodec<>("RedirectHost", Codec.STRING), (c, v) -> c.redirectHost = v, c -> c.redirectHost).add()
                    .append(new KeyedCodec<>("RedirectPort", Codec.INTEGER), (c, v) -> c.redirectPort = v, c -> c.redirectPort).add()
                    .append(new KeyedCodec<>("MaintenanceMode", Codec.BOOLEAN), (c, v) -> c.maintenanceMode = v, c -> c.maintenanceMode).add()

                    .append(new KeyedCodec<>("AvisosEnabled", Codec.BOOLEAN), (c, v) -> c.avisosEnabled = v, c -> c.avisosEnabled).add()
                    .append(new KeyedCodec<>("AvisosPrefix", Codec.STRING), (c, v) -> c.avisosPrefix = v, c -> c.avisosPrefix).add()
                    .append(new KeyedCodec<>("AvisosFormat", Codec.STRING), (c, v) -> c.avisosFormat = v, c -> c.avisosFormat).add()
                    .append(new KeyedCodec<>("AvisosAllowPlaceholders", Codec.BOOLEAN), (c, v) -> c.avisosAllowPlaceholders = v, c -> c.avisosAllowPlaceholders).add()
                    .append(new KeyedCodec<>("AvisosCooldownSeconds", Codec.INTEGER), (c, v) -> c.avisosCooldownSeconds = v, c -> c.avisosCooldownSeconds).add()
                    .append(new KeyedCodec<>("AvisosMaxLength", Codec.INTEGER), (c, v) -> c.avisosMaxLength = v, c -> c.avisosMaxLength).add()

                    .append(new KeyedCodec<>("PlaceholderRefreshIntervalSeconds", Codec.INTEGER), (c, v) -> c.placeholderRefreshIntervalSeconds = v, c -> c.placeholderRefreshIntervalSeconds).add()
                    .append(new KeyedCodec<>("StatusMaxPlayers", Codec.INTEGER), (c, v) -> c.statusMaxPlayers = v, c -> c.statusMaxPlayers).add()
                    .append(new KeyedCodec<>("StatusOnlineText", Codec.STRING), (c, v) -> c.statusOnlineText = v, c -> c.statusOnlineText).add()
                    .append(new KeyedCodec<>("StatusOfflineText", Codec.STRING), (c, v) -> c.statusOfflineText = v, c -> c.statusOfflineText).add()
                    .append(new KeyedCodec<>("PingEstimatedText", Codec.STRING), (c, v) -> c.pingEstimatedText = v, c -> c.pingEstimatedText).add()

                    .append(new KeyedCodec<>("LogAuditEnabled", Codec.BOOLEAN), (c, v) -> c.logAuditEnabled = v, c -> c.logAuditEnabled).add()

                    .append(new KeyedCodec<>("AutoRestartEnabled", Codec.BOOLEAN), (c, v) -> c.autoRestartEnabled = v, c -> c.autoRestartEnabled).add()
                    .append(new KeyedCodec<>("AutoRestartAfterSeconds", Codec.INTEGER), (c, v) -> c.autoRestartAfterSeconds = v, c -> c.autoRestartAfterSeconds).add()
                    .append(new KeyedCodec<>("AutoRestartWarningsSeconds", Codec.STRING), (c, v) -> c.autoRestartWarningsSeconds = v, c -> c.autoRestartWarningsSeconds).add()

                    .append(new KeyedCodec<>("MsgPararAnnounce", Codec.STRING), (c, v) -> c.msgPararAnnounce = v, c -> c.msgPararAnnounce).add()
                    .append(new KeyedCodec<>("MsgPararCountdown", Codec.STRING), (c, v) -> c.msgPararCountdown = v, c -> c.msgPararCountdown).add()
                    .append(new KeyedCodec<>("MsgPararSending", Codec.STRING), (c, v) -> c.msgPararSending = v, c -> c.msgPararSending).add()
                    .append(new KeyedCodec<>("MsgPararStopping", Codec.STRING), (c, v) -> c.msgPararStopping = v, c -> c.msgPararStopping).add()
                    .append(new KeyedCodec<>("MsgPararCancelled", Codec.STRING), (c, v) -> c.msgPararCancelled = v, c -> c.msgPararCancelled).add()
                    .append(new KeyedCodec<>("MsgAutoRestartWarning", Codec.STRING), (c, v) -> c.msgAutoRestartWarning = v, c -> c.msgAutoRestartWarning).add()
                    .append(new KeyedCodec<>("MsgAutoRestartStarting", Codec.STRING), (c, v) -> c.msgAutoRestartStarting = v, c -> c.msgAutoRestartStarting).add()
                    .append(new KeyedCodec<>("MsgHaltAnnounce", Codec.STRING), (c, v) -> c.msgHaltAnnounce = v, c -> c.msgHaltAnnounce).add()
                    .append(new KeyedCodec<>("MsgHaltSending", Codec.STRING), (c, v) -> c.msgHaltSending = v, c -> c.msgHaltSending).add()
                    .append(new KeyedCodec<>("MsgHaltWhitelistEnabled", Codec.STRING), (c, v) -> c.msgHaltWhitelistEnabled = v, c -> c.msgHaltWhitelistEnabled).add()
                    .build();

    private String modName = "ZZDisconect";
    private String configVersion = "1.0.0";
    private String modVersion = "1.0.5";
    private boolean debug = false;

    private String serverName = "ZonaZero";
    private String serverId = "zz-main-01";

    private String playerPrefix = "[ZZDisconect] ";
    private String consolePrefix = "[ZZDisconect] ";

    private String permissionParar = "zzdisconect.command.parar";
    private String permissionCancelarParada = "zzdisconect.command.cancelarparada";
    private String permissionAvisos = "zzdisconect.command.avisos";

    private boolean pararEnabled = true;
    private int pararDurationSeconds = 30;
    private String pararBroadcastIntervalsSeconds = "30,20,10,5,4,3,2,1";
    private int pararAnnounceDelayMs = 5000;
    private int pararCountdownEverySeconds = 0;
    private int postTransferDelayMs = 2000;
    private boolean markOfflineAfterRedirect = true;
    private boolean stopServerAfterRedirect = true;

    private String redirectTargetHost = "127.0.0.1";
    private int redirectTargetPort = 5520;
    private String fallbackHost = "127.0.0.1";
    private int fallbackPort = 5520;
    private String allowedDestinationServers = "127.0.0.1:5520";
    private int redirectMaxRetries = 1;
    private int redirectRetryDelayMs = 250;
    private boolean fallbackOnRedirectFailure = true;
    private int transferBatchSize = 25;
    private int transferBatchDelayMs = 100;

    private boolean haltEnabled = true;
    private int haltAnnounceDelayMs = 3000;
    private boolean haltEnableWhitelist = true;
    private String opGroupName = "op";

    private boolean redirectDuringHandshake = false;
    private String redirectHost = "127.0.0.1";
    private int redirectPort = 5520;
    private boolean maintenanceMode = false;

    private boolean avisosEnabled = true;
    private String avisosPrefix = "[AVISO] ";
    private String avisosFormat = "{message}";
    private boolean avisosAllowPlaceholders = true;
    private int avisosCooldownSeconds = 10;
    private int avisosMaxLength = 256;

    private int placeholderRefreshIntervalSeconds = 1;
    private int statusMaxPlayers = 150;
    private String statusOnlineText = "ONLINE";
    private String statusOfflineText = "OFFLINE";
    private String pingEstimatedText = "N/A";

    private boolean logAuditEnabled = true;

    private boolean autoRestartEnabled = false;
    private int autoRestartAfterSeconds = 0;
    private String autoRestartWarningsSeconds = "600,300,60,30,10";

    private String msgPararAnnounce = "El servidor se cerrara en {delay}s.";
    private String msgPararCountdown = "Cierre en {seconds}s. Destino %zz_redirect_target%.";
    private String msgPararSending = "Redirigiendo a todos los jugadores...";
    private String msgPararStopping = "Cierre del servidor en curso...";
    private String msgPararCancelled = "La parada fue cancelada.";
    private String msgAutoRestartWarning = "Reinicio automatico en {seconds}s.";
    private String msgAutoRestartStarting = "Reinicio automatico iniciando flujo de parada.";
    private String msgHaltAnnounce = "Mantenimiento: jugadores no-op seran redirigidos.";
    private String msgHaltSending = "Redirigiendo jugadores no-op...";
    private String msgHaltWhitelistEnabled = "Whitelist activada.";

    public ZZDisconectConfig() {
    }

    public String getModName() { return modName; }
    public String getConfigVersion() { return configVersion; }
    public String getModVersion() { return modVersion; }
    public boolean isDebug() { return debug; }

    public String getServerName() { return serverName; }
    public String getServerId() { return serverId; }

    public String getPlayerPrefix() { return playerPrefix; }
    public String getConsolePrefix() { return consolePrefix; }

    public String getPermissionParar() { return permissionParar; }
    public String getPermissionCancelarParada() { return permissionCancelarParada; }
    public String getPermissionAvisos() { return permissionAvisos; }

    public boolean isPararEnabled() { return pararEnabled; }
    public int getPararDurationSeconds() { return pararDurationSeconds; }
    public String getPararBroadcastIntervalsSeconds() { return pararBroadcastIntervalsSeconds; }
    public int getPararAnnounceDelayMs() { return pararAnnounceDelayMs; }
    public int getPararCountdownEverySeconds() { return pararCountdownEverySeconds; }
    public int getPostTransferDelayMs() { return postTransferDelayMs; }
    public boolean isMarkOfflineAfterRedirect() { return markOfflineAfterRedirect; }
    public boolean isStopServerAfterRedirect() { return stopServerAfterRedirect; }

    public String getRedirectTargetHost() { return redirectTargetHost; }
    public int getRedirectTargetPort() { return redirectTargetPort; }
    public String getFallbackHost() { return fallbackHost; }
    public int getFallbackPort() { return fallbackPort; }
    public String getAllowedDestinationServers() { return allowedDestinationServers; }
    public int getRedirectMaxRetries() { return redirectMaxRetries; }
    public int getRedirectRetryDelayMs() { return redirectRetryDelayMs; }
    public boolean isFallbackOnRedirectFailure() { return fallbackOnRedirectFailure; }
    public int getTransferBatchSize() { return transferBatchSize; }
    public int getTransferBatchDelayMs() { return transferBatchDelayMs; }

    public boolean isHaltEnabled() { return haltEnabled; }
    public int getHaltAnnounceDelayMs() { return haltAnnounceDelayMs; }
    public boolean isHaltEnableWhitelist() { return haltEnableWhitelist; }
    public String getOpGroupName() { return opGroupName; }

    public boolean isRedirectDuringHandshake() { return redirectDuringHandshake; }
    public String getRedirectHost() { return redirectHost; }
    public int getRedirectPort() { return redirectPort; }
    public boolean isMaintenanceMode() { return maintenanceMode; }

    public boolean isAvisosEnabled() { return avisosEnabled; }
    public String getAvisosPrefix() { return avisosPrefix; }
    public String getAvisosFormat() { return avisosFormat; }
    public boolean isAvisosAllowPlaceholders() { return avisosAllowPlaceholders; }
    public int getAvisosCooldownSeconds() { return avisosCooldownSeconds; }
    public int getAvisosMaxLength() { return avisosMaxLength; }

    public int getPlaceholderRefreshIntervalSeconds() { return placeholderRefreshIntervalSeconds; }
    public int getStatusMaxPlayers() { return statusMaxPlayers; }
    public String getStatusOnlineText() { return statusOnlineText; }
    public String getStatusOfflineText() { return statusOfflineText; }
    public String getPingEstimatedText() { return pingEstimatedText; }

    public boolean isLogAuditEnabled() { return logAuditEnabled; }

    public boolean isAutoRestartEnabled() { return autoRestartEnabled; }
    public int getAutoRestartAfterSeconds() { return autoRestartAfterSeconds; }
    public String getAutoRestartWarningsSeconds() { return autoRestartWarningsSeconds; }

    public String getMsgPararAnnounce() { return msgPararAnnounce; }
    public String getMsgPararCountdown() { return msgPararCountdown; }
    public String getMsgPararSending() { return msgPararSending; }
    public String getMsgPararStopping() { return msgPararStopping; }
    public String getMsgPararCancelled() { return msgPararCancelled; }
    public String getMsgAutoRestartWarning() { return msgAutoRestartWarning; }
    public String getMsgAutoRestartStarting() { return msgAutoRestartStarting; }
    public String getMsgHaltAnnounce() { return msgHaltAnnounce; }
    public String getMsgHaltSending() { return msgHaltSending; }
    public String getMsgHaltWhitelistEnabled() { return msgHaltWhitelistEnabled; }
}
