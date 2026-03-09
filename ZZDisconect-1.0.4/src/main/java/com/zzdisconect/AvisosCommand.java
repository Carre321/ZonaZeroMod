package com.zzdisconect;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvisosCommand extends AbstractAsyncCommand {

    private static final Pattern AVISO_PATTERN = Pattern.compile("^\"([^\"]+)\"(?:\\s+(\\S+))?$");
    private static final int DEFAULT_AVISO_DURATION_SECONDS = 4;

    private final ZZDisconect plugin;

    public AvisosCommand(ZZDisconect plugin) {
        super("aviso", "Envia aviso centrado: /aviso \"mensaje\" [segundos|static|off]", false);
        this.plugin = plugin;
        setAllowsExtraArguments(true);
        addAliases("avisos", "zzaviso", "zzavisos");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        ZZDisconectConfig cfg = plugin.getMainConfig().get();
        if (!cfg.isAvisosEnabled()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] /aviso esta deshabilitado en config."));
            return CompletableFuture.completedFuture(null);
        }

        String argsInput = extractArgs(context.getInputString());
        if (argsInput.isBlank()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Uso: /aviso \"mensaje\" [segundos|static|off]"));
            return CompletableFuture.completedFuture(null);
        }

        if ("off".equalsIgnoreCase(argsInput) || "clear".equalsIgnoreCase(argsInput)) {
            plugin.clearPersistentAviso("command:/aviso off sender=" + context.sender());
            context.sender().sendMessage(Message.raw("[ZZDisconect] Aviso estatico desactivado."));
            return CompletableFuture.completedFuture(null);
        }

        Matcher matcher = AVISO_PATTERN.matcher(argsInput);
        if (!matcher.matches()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Formato invalido. Usa: /aviso \"mensaje\" [segundos|static]"));
            return CompletableFuture.completedFuture(null);
        }

        String rawText = matcher.group(1) == null ? "" : matcher.group(1).trim();
        if (rawText.isBlank()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Debes indicar un mensaje entre comillas."));
            return CompletableFuture.completedFuture(null);
        }
        if (rawText.length() > cfg.getAvisosMaxLength()) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] El mensaje supera el maximo permitido."));
            return CompletableFuture.completedFuture(null);
        }

        String actor = context.sender().toString();
        if (!plugin.getCooldownService().allow("avisos:" + actor, cfg.getAvisosCooldownSeconds())) {
            context.sender().sendMessage(Message.raw("[ZZDisconect] Espera antes de enviar otro aviso."));
            return CompletableFuture.completedFuture(null);
        }

        String modeArg = matcher.group(2) == null ? "" : matcher.group(2).trim();
        boolean staticMode = isStaticKeyword(modeArg);
        int durationSeconds = DEFAULT_AVISO_DURATION_SECONDS;
        if (!modeArg.isEmpty() && !staticMode) {
            try {
                durationSeconds = Integer.parseInt(modeArg);
            } catch (NumberFormatException e) {
                context.sender().sendMessage(Message.raw("[ZZDisconect] Duracion invalida. Usa segundos (ej: 8) o 'static'."));
                return CompletableFuture.completedFuture(null);
            }
            if (durationSeconds <= 0) {
                context.sender().sendMessage(Message.raw("[ZZDisconect] La duracion debe ser mayor que 0 segundos."));
                return CompletableFuture.completedFuture(null);
            }
        }

        String withPrefix = cfg.getAvisosPrefix() + rawText;
        String formatted = cfg.getAvisosFormat().replace("{message}", withPrefix);
        if (cfg.isAvisosAllowPlaceholders()) {
            formatted = plugin.getPlaceholderService().apply(formatted);
        }

        String trigger = "command:/aviso sender=" + actor;
        if (staticMode) {
            plugin.setPersistentAviso(formatted, trigger);
            context.sender().sendMessage(Message.raw("[ZZDisconect] Aviso estatico activado hasta reinicio o /aviso off."));
        } else {
            plugin.sendAvisoCenter(formatted, durationSeconds, trigger);
            context.sender().sendMessage(Message.raw("[ZZDisconect] Aviso enviado en pantalla por " + durationSeconds + "s."));
        }

        return CompletableFuture.completedFuture(null);
    }

    private boolean isStaticKeyword(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toLowerCase();
        return "static".equals(v) || "estatico".equals(v) || "permanente".equals(v) || "sticky".equals(v);
    }

    private String extractArgs(String input) {
        if (input == null) {
            return "";
        }
        String parsed = input.trim();
        if (parsed.startsWith("/")) {
            parsed = parsed.substring(1).trim();
        }
        int firstSpace = parsed.indexOf(' ');
        if (firstSpace < 0) {
            return "";
        }
        return parsed.substring(firstSpace + 1).trim();
    }
}
