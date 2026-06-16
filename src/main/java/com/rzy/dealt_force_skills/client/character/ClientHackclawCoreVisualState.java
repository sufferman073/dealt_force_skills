package com.rzy.dealt_force_skills.client.character;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public final class ClientHackclawCoreVisualState {
    public static final int PHASE_NONE = 0;
    public static final int PHASE_CHANNEL = 1;
    public static final int PHASE_SCAN = 2;
    public static final int CHANNEL_TICKS = 16;

    private static final Map<Integer, State> STATES = new HashMap<>();

    private ClientHackclawCoreVisualState() {
    }

    public static void sync(int entityId, int phase, int remainingTicks, int round) {
        if (phase == PHASE_NONE || remainingTicks <= 0) {
            STATES.remove(entityId);
            return;
        }
        STATES.put(entityId, new State(phase, remainingTicks, Math.max(0, round)));
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            STATES.clear();
            return;
        }
        STATES.replaceAll((id, state) -> state.tick());
        STATES.entrySet().removeIf(entry ->
                entry.getValue().remainingTicks() <= 0
                        || minecraft.level.getEntity(entry.getKey()) == null);
    }

    public static boolean isActive(Player player) {
        return state(player).phase() != PHASE_NONE;
    }

    public static int phase(Player player) {
        return state(player).phase();
    }

    public static int remainingTicks(Player player) {
        return state(player).remainingTicks();
    }

    public static int round(Player player) {
        return state(player).round();
    }

    private static State state(Player player) {
        return player == null ? State.NONE : STATES.getOrDefault(player.getId(), State.NONE);
    }

    private record State(int phase, int remainingTicks, int round) {
        private static final State NONE = new State(PHASE_NONE, 0, 0);

        private State tick() {
            return new State(phase, Math.max(0, remainingTicks - 1), round);
        }
    }
}
