package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class HaltCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public HaltCommand(ZZDisconect plugin) {
        super("halt", "Mantenimiento: manda a no-ops al lobby y activa whitelist (no apaga el servidor)", true);
        this.plugin = plugin;
        addAliases("zzhalt");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!plugin.getMainConfig().get().isHaltEnabled()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] /halt está deshabilitado en config."));
            return CompletableFuture.completedFuture(null);
        }

        plugin.runHaltSequence("command:/halt");
        return CompletableFuture.completedFuture(null);
    }
}
