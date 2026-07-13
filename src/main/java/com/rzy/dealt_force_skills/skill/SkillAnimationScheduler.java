package com.rzy.dealt_force_skills.skill;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class SkillAnimationScheduler {
    private static final Map<UUID, List<ScheduledAction>> ACTIONS = new HashMap<>();

    private SkillAnimationScheduler() {
    }

    public static void schedule(ServerPlayer player, int delayTicks, Consumer<ServerPlayer> action) {
        if (delayTicks <= 0) {
            action.accept(player);
            return;
        }
        long executeAt = player.level().getGameTime() + delayTicks;
        ACTIONS.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>())
                .add(new ScheduledAction(executeAt, action));
    }

    public static void cancel(ServerPlayer player) {
        ACTIONS.remove(player.getUUID());
    }

    public static void clearAllRuntimeCaches() {
        ACTIONS.clear();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        List<ScheduledAction> actions = ACTIONS.get(player.getUUID());
        if (actions == null) {
            return;
        }
        if (!player.isAlive() || player.isRemoved()) {
            ACTIONS.remove(player.getUUID());
            return;
        }
        long now = player.level().getGameTime();
        Iterator<ScheduledAction> iterator = actions.iterator();
        while (iterator.hasNext()) {
            ScheduledAction action = iterator.next();
            if (now >= action.executeAt()) {
                iterator.remove();
                action.action().accept(player);
            }
        }
        if (actions.isEmpty()) {
            ACTIONS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIONS.remove(event.getEntity().getUUID());
    }

    private record ScheduledAction(long executeAt, Consumer<ServerPlayer> action) {
    }
}
