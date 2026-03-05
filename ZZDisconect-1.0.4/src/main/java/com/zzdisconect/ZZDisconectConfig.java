package com.zzdisconect;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Main plugin config.
 *
 * NOTE: Keys MUST be capitalized, or config loading will error.
 */
public class ZZDisconectConfig {

    public static final BuilderCodec<ZZDisconectConfig> CODEC =
            BuilderCodec.builder(ZZDisconectConfig.class, ZZDisconectConfig::new)

                    // Fallback target
                    .append(new KeyedCodec<>("FallbackHost", Codec.STRING),
                            (cfg, v) -> cfg.fallbackHost = v,
                            (cfg) -> cfg.fallbackHost).add()

                    .append(new KeyedCodec<>("FallbackPort", Codec.INTEGER),
                            (cfg, v) -> cfg.fallbackPort = v,
                            (cfg) -> cfg.fallbackPort).add()

                    // /parar behavior
                    .append(new KeyedCodec<>("PararEnabled", Codec.BOOLEAN),
                            (cfg, v) -> cfg.pararEnabled = v,
                            (cfg) -> cfg.pararEnabled).add()

                    .append(new KeyedCodec<>("PararAnnounceDelayMs", Codec.INTEGER),
                            (cfg, v) -> cfg.pararAnnounceDelayMs = v,
                            (cfg) -> cfg.pararAnnounceDelayMs).add()

                    .append(new KeyedCodec<>("PararCountdownEverySeconds", Codec.INTEGER),
                            (cfg, v) -> cfg.pararCountdownEverySeconds = v,
                            (cfg) -> cfg.pararCountdownEverySeconds).add()

                    .append(new KeyedCodec<>("PostTransferDelayMs", Codec.INTEGER),
                            (cfg, v) -> cfg.postTransferDelayMs = v,
                            (cfg) -> cfg.postTransferDelayMs).add()

                    // /halt behavior
                    .append(new KeyedCodec<>("HaltEnabled", Codec.BOOLEAN),
                            (cfg, v) -> cfg.haltEnabled = v,
                            (cfg) -> cfg.haltEnabled).add()

                    .append(new KeyedCodec<>("HaltAnnounceDelayMs", Codec.INTEGER),
                            (cfg, v) -> cfg.haltAnnounceDelayMs = v,
                            (cfg) -> cfg.haltAnnounceDelayMs).add()

                    .append(new KeyedCodec<>("HaltEnableWhitelist", Codec.BOOLEAN),
                            (cfg, v) -> cfg.haltEnableWhitelist = v,
                            (cfg) -> cfg.haltEnableWhitelist).add()

                    .append(new KeyedCodec<>("OpGroupName", Codec.STRING),
                            (cfg, v) -> cfg.opGroupName = v,
                            (cfg) -> cfg.opGroupName).add()

                    // Join-time redirect (handshake)
                    .append(new KeyedCodec<>("RedirectDuringHandshake", Codec.BOOLEAN),
                            (cfg, v) -> cfg.redirectDuringHandshake = v,
                            (cfg) -> cfg.redirectDuringHandshake).add()

                    .append(new KeyedCodec<>("RedirectHost", Codec.STRING),
                            (cfg, v) -> cfg.redirectHost = v,
                            (cfg) -> cfg.redirectHost).add()

                    .append(new KeyedCodec<>("RedirectPort", Codec.INTEGER),
                            (cfg, v) -> cfg.redirectPort = v,
                            (cfg) -> cfg.redirectPort).add()

                    .append(new KeyedCodec<>("MaintenanceMode", Codec.BOOLEAN),
                            (cfg, v) -> cfg.maintenanceMode = v,
                            (cfg) -> cfg.maintenanceMode).add()

                    // Auto-restart (one cycle until server stops)
                    .append(new KeyedCodec<>("AutoRestartEnabled", Codec.BOOLEAN),
                            (cfg, v) -> cfg.autoRestartEnabled = v,
                            (cfg) -> cfg.autoRestartEnabled).add()

                    .append(new KeyedCodec<>("AutoRestartAfterSeconds", Codec.INTEGER),
                            (cfg, v) -> cfg.autoRestartAfterSeconds = v,
                            (cfg) -> cfg.autoRestartAfterSeconds).add()

                    .append(new KeyedCodec<>("AutoRestartWarningsSeconds", Codec.STRING),
                            (cfg, v) -> cfg.autoRestartWarningsSeconds = v,
                            (cfg) -> cfg.autoRestartWarningsSeconds).add()

                    .build();

    // Defaults
    private String fallbackHost = "127.0.0.1";
    private int fallbackPort = 5520;

    // /parar
    private boolean pararEnabled = true;
    private int pararAnnounceDelayMs = 5000;
    private int pararCountdownEverySeconds = 0; // 0 = disabled
    private int postTransferDelayMs = 2000;

    // /halt
    private boolean haltEnabled = true;
    private int haltAnnounceDelayMs = 3000;
    private boolean haltEnableWhitelist = true;

    /**
     * Which group is treated as "op".
     * Default matches HytalePermissionsProvider.OP_GROUP ("op").
     */
    private String opGroupName = "op";

    // Handshake redirect (optional)
    private boolean redirectDuringHandshake = false;
    private String redirectHost = "127.0.0.1";
    private int redirectPort = 5520;
    private boolean maintenanceMode = false;

    // Auto-restart
    private boolean autoRestartEnabled = false;

    /**
     * Seconds after boot to trigger the auto-restart flow.
     * Example: 7200 = 2 hours.
     */
    private int autoRestartAfterSeconds = 0;

    /**
     * Comma-separated list of seconds BEFORE restart to broadcast warnings.
     * Example: "600,300,60,30,10"
     */
    private String autoRestartWarningsSeconds = "600,300,60,30,10";

    public ZZDisconectConfig() {}

    public String getFallbackHost() { return fallbackHost; }
    public int getFallbackPort() { return fallbackPort; }

    public boolean isPararEnabled() { return pararEnabled; }
    public int getPararAnnounceDelayMs() { return pararAnnounceDelayMs; }
    public int getPararCountdownEverySeconds() { return pararCountdownEverySeconds; }
    public int getPostTransferDelayMs() { return postTransferDelayMs; }

    public boolean isHaltEnabled() { return haltEnabled; }
    public int getHaltAnnounceDelayMs() { return haltAnnounceDelayMs; }
    public boolean isHaltEnableWhitelist() { return haltEnableWhitelist; }
    public String getOpGroupName() { return opGroupName; }

    public boolean isRedirectDuringHandshake() { return redirectDuringHandshake; }
    public String getRedirectHost() { return redirectHost; }
    public int getRedirectPort() { return redirectPort; }
    public boolean isMaintenanceMode() { return maintenanceMode; }

    public boolean isAutoRestartEnabled() { return autoRestartEnabled; }
    public int getAutoRestartAfterSeconds() { return autoRestartAfterSeconds; }
    public String getAutoRestartWarningsSeconds() { return autoRestartWarningsSeconds; }
}
