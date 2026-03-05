package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.data.KitDefinition;
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

public class KitGetCommand extends AbstractKitTargetCommand {

    private final RequiredArg<String> kitArg;

    public KitGetCommand() {
        super("get", "Da un kit a un jugador específico");
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
        if (!ctx.sender().hasPermission("kit.admin")) {
            ZZKits.instance().getMensajes().send(ctx, "no_permiso_admin");
            return;
        }

        // En /kit get queremos obligar a especificar jugador (para diferenciarlo de /kit <nombre>)
        if (senderRef != null && senderRef.equals(targetRef)) {
            ZZKits.instance().getMensajes().send(ctx, "uso_kit_get");
return;
        }

        String kitName = ZZKits.instance().getKitManager().sanitize(kitArg.get(ctx));
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

        
        boolean overwrite = ZZKits.instance().getConfigManager().get().overwriteSlotsOnGive;
        String fullMode = ZZKits.instance().getConfigManager().get().inventoryFullMode;

        PlayerReadyListener.GiveResult res = PlayerReadyListener.giveKitToPlayer(target, targetRef, store, kit, overwrite, fullMode);

        if (!res.success()) {
            // No se entregó por falta de espacio
            ZZKits.instance().getMensajes().send(ctx, "objetivo_sin_espacio", java.util.Map.of("player", target.getDisplayName()));
            ZZKits.instance().getMensajes().send(target, "inventario_sin_espacio");
            return;
        }

        var mensajes = ZZKits.instance().getMensajes();
        java.util.Map<String, String> ph = java.util.Map.of(
            "kit", kit.name,
            "player", target.getDisplayName(),
            "count", String.valueOf(res.processedStacks())
        );
        mensajes.send(ctx, "kit_entregado", ph);
        mensajes.send(target, "kit_recibido_admin", java.util.Map.of("kit", kit.name));

        if (res.droppedQuantity() > 0) {
            mensajes.send(target, "items_tirados_suelo", java.util.Map.of("dropped", String.valueOf(res.droppedQuantity())));
            mensajes.send(ctx, "items_tirados_suelo_otro", java.util.Map.of(
                "player", target.getDisplayName(),
                "dropped", String.valueOf(res.droppedQuantity())
            ));
        }
}
}
