package com.zzdisconect;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class PermissionService {

    private final ZZDisconect plugin;

    public PermissionService(ZZDisconect plugin) {
        this.plugin = plugin;
    }

    public boolean hasNode(PlayerRef player, String node) {
        if (player == null || !player.isValid() || node == null || node.isBlank()) {
            return false;
        }
        try {
            return PermissionsModule.get().hasPermission(player.getUuid(), node);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isOp(PlayerRef playerRef) {
        try {
            String opGroup = plugin.getMainConfig().get().getOpGroupName();
            return PermissionsModule.get().getGroupsForUser(playerRef.getUuid()).contains(opGroup);
        } catch (Throwable t) {
            return false;
        }
    }
}
