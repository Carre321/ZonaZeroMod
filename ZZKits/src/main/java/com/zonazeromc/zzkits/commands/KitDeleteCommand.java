package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;

public class KitDeleteCommand extends ZZCommandBase {

    private final RequiredArg<String> kitArg;

    public KitDeleteCommand() {
        super("delete", "Borra un kit");
        this.kitArg = withRequiredArg("kit", "Nombre del kit", ArgTypes.STRING);
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

        String kitName = ZZKits.instance().getKitManager().sanitize(kitArg.get(ctx));

        boolean ok = ZZKits.instance().getKitManager().deleteKit(kitName);
        ZZKits.instance().getUsageManager().clearKit(kitName);
        ZZKits.instance().getUsageManager().save();

        var mensajes = ZZKits.instance().getMensajes();
        java.util.Map<String, String> ph = java.util.Map.of("kit", kitName);
        if (ok) mensajes.send(ctx, "kit_eliminado", ph);
        else mensajes.send(ctx, "kit_eliminar_fail", ph);
    }
}
