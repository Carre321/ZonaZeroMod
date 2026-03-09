package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

public final class BroadcastService {

    private final ZZDisconect plugin;

    public BroadcastService(ZZDisconect plugin) {
        this.plugin = plugin;
    }

    public void global(String template) {
        global(template, null, null);
    }

    public void global(String template, String key, String value) {
        ZZDisconectConfig cfg = plugin.getMainConfig().get();
        String out = template;
        if (key != null && value != null) {
            out = out.replace("{" + key + "}", value);
        }
        out = cfg.getPlayerPrefix() + plugin.getPlaceholderService().apply(out);
        send(out);
    }

    public void globalRaw(String text) {
        send(plugin.getPlaceholderService().apply(text));
    }

    public void globalCenterRaw(String text) {
        sendCenter(plugin.getPlaceholderService().apply(text), 4.0f, 1.5f, 1.5f);
    }

    public void globalCenterRaw(String text, float durationSeconds) {
        sendCenter(plugin.getPlaceholderService().apply(text), durationSeconds, 0.15f, 0.15f);
    }

    public void globalCenterRaw(String text, float durationSeconds, float fadeInSeconds, float fadeOutSeconds) {
        sendCenter(plugin.getPlaceholderService().apply(text), durationSeconds, fadeInSeconds, fadeOutSeconds);
    }

    public void hideCenter(float fadeOutSeconds) {
        float safeFade = Math.max(0.0f, fadeOutSeconds);
        for (PlayerRef p : Universe.get().getPlayers()) {
            if (p == null || !p.isValid()) {
                continue;
            }
            EventTitleUtil.hideEventTitleFromPlayer(p, safeFade);
        }
    }

    private void send(String out) {
        for (PlayerRef p : Universe.get().getPlayers()) {
            if (p == null || !p.isValid()) {
                continue;
            }
            p.sendMessage(Message.raw(out));
        }
    }

    private void sendCenter(String out, float durationSeconds, float fadeInSeconds, float fadeOutSeconds) {
        Message primary = Message.raw(out);
        Message secondary = Message.empty();
        float safeDuration = Math.max(0.5f, durationSeconds);
        float safeFadeIn = Math.max(0.0f, fadeInSeconds);
        float safeFadeOut = Math.max(0.0f, fadeOutSeconds);
        for (PlayerRef p : Universe.get().getPlayers()) {
            if (p == null || !p.isValid()) {
                continue;
            }
            EventTitleUtil.showEventTitleToPlayer(p, primary, secondary, true, null, safeDuration, safeFadeIn, safeFadeOut);
        }
    }
}
