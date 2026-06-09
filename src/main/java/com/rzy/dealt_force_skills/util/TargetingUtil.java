package com.rzy.dealt_force_skills.util;

import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationStateManager;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class TargetingUtil {
    private TargetingUtil() {
    }

    public static boolean isTargetablePlayer(Player player) {
        return player.isAlive()
                && !player.isSpectator()
                && !player.getAbilities().instabuild
                && !DepartmentOfTransportationStateManager.isFullyConcealed(player)
                && (!(player instanceof ServerPlayer serverPlayer)
                || !UndeadStateManager.blocksIncomingAttack(serverPlayer));
    }

    public static boolean isTargetableLiving(LivingEntity entity) {
        return entity.isAlive() && (!(entity instanceof Player player) || isTargetablePlayer(player));
    }
}
