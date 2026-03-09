package com.zzdisconect;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * messages.json (created in the plugin data folder).
 *
 * NOTE: Keys MUST be capitalized, or config loading will error.
 */
public class ZZDisconectMessages {

    public static final BuilderCodec<ZZDisconectMessages> CODEC =
            BuilderCodec.builder(ZZDisconectMessages.class, ZZDisconectMessages::new)

                    .append(new KeyedCodec<>("Prefix", Codec.STRING),
                            (m, v) -> m.prefix = v,
                            m -> m.prefix).add()

                    .append(new KeyedCodec<>("PararAnnounce", Codec.STRING),
                            (m, v) -> m.pararAnnounce = v,
                            m -> m.pararAnnounce).add()

                    .append(new KeyedCodec<>("PararCountdown", Codec.STRING),
                            (m, v) -> m.pararCountdown = v,
                            m -> m.pararCountdown).add()

                    .append(new KeyedCodec<>("PararSending", Codec.STRING),
                            (m, v) -> m.pararSending = v,
                            m -> m.pararSending).add()

                    .append(new KeyedCodec<>("PararStopping", Codec.STRING),
                            (m, v) -> m.pararStopping = v,
                            m -> m.pararStopping).add()

                    .append(new KeyedCodec<>("AutoRestartWarning", Codec.STRING),
                            (m, v) -> m.autoRestartWarning = v,
                            m -> m.autoRestartWarning).add()

                    .append(new KeyedCodec<>("AutoRestartStarting", Codec.STRING),
                            (m, v) -> m.autoRestartStarting = v,
                            m -> m.autoRestartStarting).add()

                    .append(new KeyedCodec<>("HaltAnnounce", Codec.STRING),
                            (m, v) -> m.haltAnnounce = v,
                            m -> m.haltAnnounce).add()

                    .append(new KeyedCodec<>("HaltSending", Codec.STRING),
                            (m, v) -> m.haltSending = v,
                            m -> m.haltSending).add()

                    .append(new KeyedCodec<>("HaltWhitelistEnabled", Codec.STRING),
                            (m, v) -> m.haltWhitelistEnabled = v,
                            m -> m.haltWhitelistEnabled).add()

                    .build();

    private String prefix = "[ZZDisconect] ";

    // {delay} = seconds before sending to lobby
    private String pararAnnounce = "El servidor se apagara pronto. En {delay}s seras enviado al lobby.";
    // {seconds} = countdown value
    private String pararCountdown = "Apagando en {seconds}s...";
    private String pararSending = "Enviando a todos al lobby...";
    private String pararStopping = "Apagando servidor...";

    // {seconds} = seconds remaining
    private String autoRestartWarning = "Reinicio automatico en {seconds}s.";
    private String autoRestartStarting = "Reinicio automatico ahora.";

    private String haltAnnounce = "Mantenimiento: se enviara a los jugadores al lobby y se activara la whitelist.";
    private String haltSending = "Enviando al lobby a jugadores (no-op)...";
    private String haltWhitelistEnabled = "Whitelist activada.";

    public ZZDisconectMessages() {
    }

    public String getPrefix() { return prefix; }

    public String getPararAnnounce() { return pararAnnounce; }
    public String getPararCountdown() { return pararCountdown; }
    public String getPararSending() { return pararSending; }
    public String getPararStopping() { return pararStopping; }

    public String getAutoRestartWarning() { return autoRestartWarning; }
    public String getAutoRestartStarting() { return autoRestartStarting; }

    public String getHaltAnnounce() { return haltAnnounce; }
    public String getHaltSending() { return haltSending; }
    public String getHaltWhitelistEnabled() { return haltWhitelistEnabled; }
}
