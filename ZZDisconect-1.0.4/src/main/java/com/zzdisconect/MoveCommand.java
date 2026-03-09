package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MoveCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> hostArg;
    private final RequiredArg<Integer> portArg;

    public MoveCommand(ZZDisconect plugin) {
        super("move", "Mueve a un jugador a un servidor especifico: /move <jugador> <host> <port>", true);
        this.plugin = plugin;
        this.playerArg = withRequiredArg("jugador", "Jugador objetivo", ArgTypes.PLAYER_REF);
        this.hostArg = withRequiredArg("host", "Host destino", ArgTypes.STRING);
        this.portArg = withRequiredArg("port", "Puerto destino", ArgTypes.INTEGER);
        addAliases("zzmove");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        PlayerRef player = playerArg.get(context);
        String host = hostArg.get(context);
        int port = portArg.get(context);
        if (player == null || !player.isValid()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Jugador no encontrado o desconectado."));
            return CompletableFuture.completedFuture(null);
        }

        if (!plugin.isValidTarget(host, port)) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Servidor de destino invalido."));
            return CompletableFuture.completedFuture(null);
        }

        player.referToServer(host, port);
        context.sender().sendMessage(Message.raw("[ZZDisconect] Jugador " + player.getUsername() + " movido a " + host + ":" + port));

        return CompletableFuture.completedFuture(null);
    }
}
