package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class PararCommand extends AbstractAsyncCommand {

    private final ZZDisconect plugin;

    public PararCommand(ZZDisconect plugin) {
        super("parar", "Anuncia cierre con mensaje+tiempo, redirige y luego ejecuta /stop", false);
        this.plugin = plugin;
        setAllowsExtraArguments(true);
        addAliases("zzparar");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!plugin.getMainConfig().get().isPararEnabled()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] /parar esta deshabilitado en config."));
            return CompletableFuture.completedFuture(null);
        }

        String input = context.getInputString();
        if (input == null || input.isBlank()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Uso: /zzparar <mensaje> <tiempoSegundos>"));
            return CompletableFuture.completedFuture(null);
        }

        String parsed = input.trim();
        if (parsed.startsWith("/")) {
            parsed = parsed.substring(1).trim();
        }
        int firstSpace = parsed.indexOf(' ');
        if (firstSpace >= 0) {
            parsed = parsed.substring(firstSpace + 1).trim();
        }

        if (parsed.isBlank()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Uso: /zzparar <mensaje> <tiempoSegundos>"));
            return CompletableFuture.completedFuture(null);
        }

        String[] parts = parsed.split("\\s+");
        if (parts.length < 2) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Uso: /zzparar <mensaje> <tiempoSegundos>"));
            return CompletableFuture.completedFuture(null);
        }

        String rawSeconds = parts[parts.length - 1].trim();
        int durationSeconds;
        try {
            durationSeconds = Integer.parseInt(rawSeconds);
        } catch (NumberFormatException e) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] El tiempo debe ser un numero entero en segundos."));
            return CompletableFuture.completedFuture(null);
        }

        if (durationSeconds <= 0) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] El tiempo debe ser mayor que 0."));
            return CompletableFuture.completedFuture(null);
        }

        int lastSpace = parsed.lastIndexOf(' ');
        String customMessage = lastSpace > 0 ? parsed.substring(0, lastSpace).trim() : "";
        if (customMessage.isBlank()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Debes indicar un mensaje de cierre."));
            return CompletableFuture.completedFuture(null);
        }

        plugin.runPararSequence("command:/parar sender=" + context.sender(), customMessage, durationSeconds);
        return CompletableFuture.completedFuture(null);
    }
}
