package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.data.ConfigData;
import com.zonazeromc.zzkits.data.KitDefinition;
import com.zonazeromc.zzkits.data.KitManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class KitCreateCommand extends AbstractKitTargetCommand {

    private final RequiredArg<String> kitArg;
    private final DefaultArg<Integer> cooldownArg;
    private final OptionalArg<Boolean> oneTimeArg;

    public KitCreateCommand() {
        super("create", "Crea/actualiza un kit desde el inventario/hotbar/equipo del jugador");

        this.kitArg = withRequiredArg("kit", "Nombre del kit", ArgTypes.STRING);
        this.cooldownArg = withDefaultArg("cooldown", "Cooldown en segundos (0 = sin cooldown)", ArgTypes.INTEGER, 0, "0");
        this.oneTimeArg = withOptionalArg("oneTime", "true/false (si es 1 solo uso)", ArgTypes.BOOLEAN);

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
        if (!ctx.sender().hasPermission("kit.admin")) {
            ZZKits.instance().getMensajes().send(ctx, "no_permiso_admin");
            return;
        }

        Player target = store.getComponent(targetRef, Player.getComponentType());
        if (target == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return;
        }

        ConfigData cfg = ZZKits.instance().getConfigManager().get();
        KitManager kitManager = ZZKits.instance().getKitManager();

        String kitName = kitManager.sanitize(kitArg.get(ctx));

        int cooldown = cooldownArg.get(ctx);
        if (cooldown < 0) cooldown = 0;

        boolean oneTime = oneTimeArg.provided(ctx)
            ? Boolean.TRUE.equals(oneTimeArg.get(ctx))
            : cfg.defaultOneTime;

        Inventory inv = target.getInventory();
        if (inv == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_inventario");
            return;
        }

        try {
            KitDefinition def = kitManager.snapshotFromInventory(kitName, inv, cooldown, oneTime);
            kitManager.saveKit(def);

            var mensajes = ZZKits.instance().getMensajes();
            java.util.Map<String, String> ph = java.util.Map.of(
                "kit", def.name,
                "cooldown", String.valueOf(def.cooldownSeconds),
                "oneTime", String.valueOf(def.oneTime)
            );
            mensajes.send(ctx, "kit_guardado", ph);
            mensajes.send(ctx, "kit_permiso_info", java.util.Map.of("kit", def.name));

            ZZKits.LOGGER.atInfo().log("[ZZKits] Kit '%s' creado/actualizado por %s.", def.name, ctx.sender().getDisplayName());
        } catch (Exception e) {
            ZZKits.instance().getMensajes().send(ctx, "error_guardando_kit", java.util.Map.of("error", String.valueOf(e.getMessage())));
        }
    }
}
