package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import java.util.Map;
import com.zonazeromc.zzkits.util.TextUtil;


public class KitCommand extends ZZCommandBase {

    public KitCommand() {
        super("kit", "ZZKits - comandos de kits");

        // Subcomandos
        addSubCommand(new KitReloadCommand());
        addSubCommand(new KitCreateCommand());
        addSubCommand(new KitDeleteCommand());
        addSubCommand(new KitGetCommand());
        addSubCommand(new KitListCommand());
        addSubCommand(new KitGuiCommand());

        // Variante de uso: /kit <nombre> [player]
        addUsageVariant(new KitUseVariant());
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        // La ayuda se define en mensajes.json (lista 'help')
        var plugin = ZZKits.instance();
        var mensajes = plugin.getMensajes();
        var cfg = plugin.getConfigManager().get();

        Map<String, String> ph = Map.of(
            "prefix", mensajes.get().prefix,
            "starterKit", (cfg.starterKit == null ? "default" : cfg.starterKit)
        );

        for (String line : mensajes.get().help) {
            String rendered = TextUtil.colorize(TextUtil.applyPlaceholders(line, ph));
            ctx.sendMessage(Message.raw(rendered));
        }
    }

}
