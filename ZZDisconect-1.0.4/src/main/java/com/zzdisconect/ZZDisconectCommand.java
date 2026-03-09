package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

public final class ZZDisconectCommand extends CommandBase {

    public ZZDisconectCommand(ZZDisconect plugin) {
        super("zzdisconect", "Comandos administrativos de ZZDisconect");
        addAliases("zzd");
        addSubCommand(new ZZDisconectReloadCommand(plugin));
    }

    @Override
    protected void executeSync(CommandContext context) {
        context.sendMessage(Message.raw("[ZZDisconect] Uso: /zzdisconect reload"));
    }
}
