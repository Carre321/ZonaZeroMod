package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class PararCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public PararCommand(ZZDisconect plugin) {
        super("parar", "Anuncia, espera, manda a todos al lobby y luego ejecuta /stop", true);
        this.plugin = plugin;
        addAliases("zzparar");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!plugin.getMainConfig().get().isPararEnabled()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] /parar está deshabilitado en config."));
            return CompletableFuture.completedFuture(null);
        }

        plugin.runPararSequence("command:/parar");
        return CompletableFuture.completedFuture(null);
    }
}
