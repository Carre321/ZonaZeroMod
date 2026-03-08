package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MoveCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public MoveCommand(ZZDisconect plugin) {
        super("move", "Mueve a un jugador a un servidor específico: /move <jugador> <host> <port>", true);
        this.plugin = plugin;
        addAliases("zzmove");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String[] args = context.args();
        if (args.length < 3) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Uso: /move <jugador> <host> <port>"));
            return CompletableFuture.completedFuture(null);
        }

        String playerName = args[0];
        String host = args[1];
        int port;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Puerto inválido."));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef player = Universe.get().getPlayer(playerName);
        if (player == null) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Jugador no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        if (!plugin.isValidTarget(host, port)) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Servidor de destino inválido."));
            return CompletableFuture.completedFuture(null);
        }

        player.referToServer(host, port);
        context.sender().sendMessage(Message.raw("[ZZDisconect] Jugador " + playerName + " movido a " + host + ":" + port));

        return CompletableFuture.completedFuture(null);
    }
}