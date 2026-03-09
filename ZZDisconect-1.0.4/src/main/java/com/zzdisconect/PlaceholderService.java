package com.zzdisconect;

import com.hypixel.hytale.server.core.universe.Universe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class PlaceholderService {

    private final AtomicReference<Map<String, String>> cache = new AtomicReference<>(new HashMap<>());

    public void refresh(ZZDisconectConfig cfg, StatusService status) {
        Map<String, String> m = new HashMap<>();

        int players = Universe.get().getPlayers().size();
        int maxPlayers = Math.max(0, cfg.getStatusMaxPlayers());
        int freeSlots = Math.max(0, maxPlayers - players);

        m.put("zz_server_name", cfg.getServerName());
        m.put("zz_server_id", cfg.getServerId());
        m.put("zz_server_state", status.getState().name());
        m.put("zz_online_offline", status.getState() == ServerState.OFFLINE ? cfg.getStatusOfflineText() : cfg.getStatusOnlineText());
        m.put("zz_closing", String.valueOf(status.getState() == ServerState.SHUTTING_DOWN || status.getState() == ServerState.REDIRECTING));
        m.put("zz_maintenance", String.valueOf(cfg.isMaintenanceMode()));
        m.put("zz_players_current", String.valueOf(players));
        m.put("zz_players_max", String.valueOf(maxPlayers));
        m.put("zz_slots_free", String.valueOf(freeSlots));
        m.put("zz_redirect_target", cfg.getRedirectTargetHost() + ":" + cfg.getRedirectTargetPort());
        m.put("zz_shutdown_remaining", String.valueOf(status.getShutdownRemainingSeconds()));
        m.put("zz_shutdown_in_progress", String.valueOf(status.isShutdownInProgress()));
        m.put("zz_ping_estimated", cfg.getPingEstimatedText());
        m.put("zz_mod_version", cfg.getModVersion());
        m.put("zz_config_version", cfg.getConfigVersion());

        cache.set(m);
    }

    public String apply(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String out = input;
        for (Map.Entry<String, String> e : cache.get().entrySet()) {
            out = out.replace("%" + e.getKey() + "%", e.getValue());
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return out;
    }
}
