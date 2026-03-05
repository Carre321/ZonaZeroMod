package com.zonazeromc.zzkits.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.time.Duration;
import java.time.Instant;

/**
 * Utilidades para anti-combat.
 *
 * NOTA: Esto no implementa "combat tag" propio, usa el dato que el servidor ya registra
 * en DamageDataComponent#setLastCombatAction (actualizado por DamageSystems.RecordLastCombat).
 */
public final class CombatUtil {

    private CombatUtil() {}

    /**
     * Devuelve segundos restantes de "en combate". 0 => fuera de combate.
     */
    public static long secondsLeftInCombat(Store<EntityStore> store, Ref<EntityStore> entityRef, int combatTagSeconds) {
        if (store == null || entityRef == null || !entityRef.isValid()) return 0;
        if (combatTagSeconds <= 0) return 0;

        DamageDataComponent dmg = store.getComponent(entityRef, DamageDataComponent.getComponentType());
        if (dmg == null) return 0;

        Instant last = dmg.getLastCombatAction();
        if (last == null) return 0;

        Instant now = now(store);
        long elapsed = Duration.between(last, now).getSeconds();
        long remaining = (long) combatTagSeconds - elapsed;
        return Math.max(0, remaining);
    }

    public static boolean isInCombat(Store<EntityStore> store, Ref<EntityStore> entityRef, int combatTagSeconds) {
        return secondsLeftInCombat(store, entityRef, combatTagSeconds) > 0;
    }

    private static Instant now(Store<EntityStore> store) {
        try {
            TimeResource tr = store.getResource(TimeResource.getResourceType());
            if (tr != null && tr.getNow() != null) return tr.getNow();
        } catch (Exception ignored) {}
        return Instant.now();
    }
}
