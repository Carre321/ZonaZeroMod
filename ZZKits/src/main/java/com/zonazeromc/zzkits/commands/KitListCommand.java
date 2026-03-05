package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.List;

public class KitListCommand extends ZZCommandBase {

    public KitListCommand() {
        super("list", "Lista todos los kits disponibles");
        addAliases("ls");
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer() && !ZZKits.instance().getConfigManager().get().allowConsoleCommands) {
            ZZKits.instance().getMensajes().send(ctx, "consola_desactivada");
            return;
        }

                var mensajes = ZZKits.instance().getMensajes();
        List<String> kits = ZZKits.instance().getKitManager().listKitNames();
        if (kits.isEmpty()) {
            mensajes.send(ctx, "lista_vacia");
            return;
        }
        String kitsStr = String.join("&7, &f", kits);
        java.util.Map<String, String> ph = java.util.Map.of(
            "count", String.valueOf(kits.size()),
            "kits", kitsStr
        );
        mensajes.send(ctx, "lista_kits", ph);
    }
}
