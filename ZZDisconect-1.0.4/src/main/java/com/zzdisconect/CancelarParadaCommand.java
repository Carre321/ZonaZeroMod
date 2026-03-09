package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class CancelarParadaCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public CancelarParadaCommand(ZZDisconect plugin) {
        super("cancelarparada", "Cancela el cierre en curso", true);
        this.plugin = plugin;
        addAliases("zzcancelarparada");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!plugin.cancelPararSequence("command:/cancelarparada")) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] No hay una parada en curso."));
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage(Message.raw("[ZZDisconect] Parada cancelada."));
        return CompletableFuture.completedFuture(null);
    }
}
