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
        super("lobbyinfo", "Muestra información del lobby: jugadores conectados y estado del servidor", false);
        this.plugin = plugin;
        addAliases("zzlobby");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        List<PlayerRef> players = Universe.get().getPlayers();
        StringBuilder sb = new StringBuilder("[ZZDisconect] Jugadores conectados: ");
        for (PlayerRef p : players) {
            if (p != null && p.isValid()) {
                sb.append(p.getName()).append(", ");
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 2); // remove last comma

        sb.append("\nEstado del servidor: Online (").append(players.size()).append(" jugadores)");

        context.sender().sendMessage(Message.raw(sb.toString()));

        // TODO: Implementar hologramas y PNG en futuras versiones

        return CompletableFuture.completedFuture(null);
    }
}