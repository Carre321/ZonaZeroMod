package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public final class ZZDisconectReloadCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public ZZDisconectReloadCommand(ZZDisconect plugin) {
        super("reload", "Recarga la configuracion de ZZDisconect sin reiniciar el servidor");
        this.plugin = plugin;
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String trigger = "command:/zzdisconect reload sender=" + context.sender();
        boolean ok = plugin.reloadRuntime(trigger);

        if (ok) {
            context.sendMessage(Message.raw("[ZZDisconect] Configuracion recargada correctamente."));
        } else {
            context.sendMessage(Message.raw("[ZZDisconect] Error al recargar configuracion. Revisa la consola."));
        }

        return CompletableFuture.completedFuture(null);
    }
}
