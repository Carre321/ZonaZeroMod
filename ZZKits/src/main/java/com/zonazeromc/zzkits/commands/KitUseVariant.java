package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.data.KitDefinition;
import com.zonazeromc.zzkits.data.UsageManager;
import com.zonazeromc.zzkits.listeners.PlayerReadyListener;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Variante de /kit:
 *  /kit <kitName> [player]
 *
 * - Si lo ejecuta un jugador y NO pone [player] => se aplica a sí mismo.
 * - Si lo ejecuta consola => debe poner [player].
 * - Si un jugador pone [player] para dar a otro, requiere kit.admin.
 *
 * Reglas de permisos para usar un kit:
 * - kit.admin   → siempre puede
 * - kit.<kitName>
 */
public class KitUseVariant extends AbstractKitTargetCommand {

    private final RequiredArg<String> kitArg;

    public KitUseVariant() {
        super("Obtiene un kit");
        this.kitArg = withRequiredArg("kit", "Nombre del kit", ArgTypes.STRING);

        // [player] al final
        enableTargetPlayerArg();
    }

    @Override
    protected void executeTarget(
        @Nonnull CommandContext ctx,
        Ref<EntityStore> senderRef,
        @Nonnull Ref<EntityStore> targetRef,
        @Nonnull World world,
        @Nonnull Store<EntityStore> store
    ) {
        String kitName = ZZKits.instance().getKitManager().sanitize(kitArg.get(ctx));

        // Si un jugador intenta aplicar a otro, exigir admin
        if (senderRef != null && !senderRef.equals(targetRef) && !ctx.sender().hasPermission("kit.admin")) {
            ZZKits.instance().getMensajes().send(ctx, "no_puedes_dar_otro");
            return;
        }

        // Permiso para usar este kit
        if (!ctx.sender().hasPermission("kit.admin") && !ctx.sender().hasPermission("kit." + kitName)) {
                        ZZKits.instance().getMensajes().send(ctx, "no_permiso_kit", java.util.Map.of("kit", kitName));
            return;
        }

        KitDefinition kit = ZZKits.instance().getKitManager().getKit(kitName);
        if (kit == null) {
            ZZKits.instance().getMensajes().send(ctx, "kit_no_existe", java.util.Map.of("kit", kitName));
            return;
        }

        Player target = store.getComponent(targetRef, Player.getComponentType());
        if (target == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return;
        }

        @SuppressWarnings("deprecation")
        UUID uuid = target.getUuid();


        // Anti-combat (solo para uso normal, no admin)
        var cfg = ZZKits.instance().getConfigManager().get();
        if (cfg.preventKitsInCombat && !ctx.sender().hasPermission("kit.admin")) {
            long left = com.zonazeromc.zzkits.util.CombatUtil.secondsLeftInCombat(store, targetRef, cfg.combatTagSeconds);
            if (left > 0) {
                ZZKits.instance().getMensajes().send(ctx, "en_combate", java.util.Map.of("secs", String.valueOf(left)));
                return;
            }
        }



        UsageManager usage = ZZKits.instance().getUsageManager();

        // Cooldown / oneTime SOLO para el uso normal (no /kit get)
        if (!ctx.sender().hasPermission("kit.admin")) {
            if (kit.oneTime && usage.hasEverUsed(kitName, uuid)) {
                ZZKits.instance().getMensajes().send(ctx, "one_time_usado");
                return;
            }

            if (kit.cooldownSeconds > 0) {
                long last = usage.getLastUsed(kitName, uuid);
                if (last > 0) {
                    long now = System.currentTimeMillis();
                    long cdMs = kit.cooldownSeconds * 1000L;
                    long remaining = (last + cdMs) - now;
                    if (remaining > 0) {
                        long secs = (remaining + 999) / 1000;
                        ZZKits.instance().getMensajes().send(ctx, "cooldown", java.util.Map.of("secs", String.valueOf(secs)));
                        return;
                    }
                }
            }
        }

        
        boolean overwrite = cfg.overwriteSlotsOnGive;
        String fullMode = cfg.inventoryFullMode;

        PlayerReadyListener.GiveResult res = PlayerReadyListener.giveKitToPlayer(target, targetRef, store, kit, overwrite, fullMode);

        if (!res.success()) {
            // No aplicar cooldown/oneTime si no se entregó
            if (senderRef == null || senderRef.equals(targetRef)) {
                ZZKits.instance().getMensajes().send(ctx, "inventario_sin_espacio");
            } else {
                ZZKits.instance().getMensajes().send(ctx, "objetivo_sin_espacio", java.util.Map.of("player", target.getDisplayName()));
                ZZKits.instance().getMensajes().send(target, "inventario_sin_espacio");
            }
            return;
        }

        // Marcar uso SOLO si se entregó
        usage.markUsedNow(kitName, uuid);
        usage.save();

        var mensajes = ZZKits.instance().getMensajes();
        java.util.Map<String, String> ph = java.util.Map.of(
            "kit", kitName,
            "player", target.getDisplayName(),
            "count", String.valueOf(res.processedStacks())
        );
        mensajes.send(ctx, "kit_entregado", ph);

        if (senderRef == null || !senderRef.equals(targetRef)) {
            mensajes.send(target, "kit_recibido", java.util.Map.of("kit", kitName));
        }

        if (res.droppedQuantity() > 0) {
            mensajes.send(target, "items_tirados_suelo", java.util.Map.of("dropped", String.valueOf(res.droppedQuantity())));
            if (senderRef != null && !senderRef.equals(targetRef)) {
                mensajes.send(ctx, "items_tirados_suelo_otro", java.util.Map.of(
                    "player", target.getDisplayName(),
                    "dropped", String.valueOf(res.droppedQuantity())
                ));
            }
        }
}
}
