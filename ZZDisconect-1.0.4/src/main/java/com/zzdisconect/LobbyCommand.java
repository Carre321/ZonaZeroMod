package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LobbyCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public LobbyCommand(ZZDisconect plugin) {
        super("lobbyinfo", "Muestra informacion del lobby: jugadores conectados y estado del servidor", false);
        this.plugin = plugin;
        addAliases("zzlobby");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        ZZDisconectConfig cfg = plugin.getMainConfig().get();
        List<PlayerRef> players = Universe.get().getPlayers();
        StringBuilder sb = new StringBuilder("[ZZDisconect] Jugadores conectados: ");

        int validCount = 0;
        for (PlayerRef p : players) {
            if (p != null && p.isValid()) {
                if (validCount > 0) {
                    sb.append(", ");
                }
                sb.append(p.getUsername());
                validCount++;
            }
        }

        if (validCount == 0) {
            sb.append("ninguno");
        }

        sb.append("\nEstado del servidor: Online (").append(validCount).append(" jugadores)");
        sb.append("\nServerName: ").append(cfg.getServerName());
        sb.append("\nServerId: ").append(cfg.getServerId());
        sb.append("\nState: ").append(plugin.getPlaceholderService().apply("%zz_server_state%"));
        sb.append("\nShutdownInProgress: ").append(plugin.getPlaceholderService().apply("%zz_shutdown_in_progress%"));
        sb.append("\nShutdownRemaining: ").append(plugin.getPlaceholderService().apply("%zz_shutdown_remaining%"));
        sb.append("\nRedirectTarget: ").append(plugin.getPlaceholderService().apply("%zz_redirect_target%"));
        context.sender().sendMessage(Message.raw(sb.toString()));

        return CompletableFuture.completedFuture(null);
    }
}
