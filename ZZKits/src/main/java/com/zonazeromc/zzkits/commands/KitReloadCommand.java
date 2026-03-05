package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public class KitReloadCommand extends ZZCommandBase {

    public KitReloadCommand() {
        super("reload", "Recarga ZZKits (config/kits/usage)");
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer() && !ZZKits.instance().getConfigManager().get().allowConsoleCommands) {
            ZZKits.instance().getMensajes().send(ctx, "consola_desactivada");
            return;
        }

        if (!ctx.sender().hasPermission("kit.admin")) {
            ZZKits.instance().getMensajes().send(ctx, "no_permiso_admin");
            return;
        }

        ZZKits.instance().reloadAll();

        ZZKits.instance().getMensajes().send(ctx, "reload_ok");
    }
}
