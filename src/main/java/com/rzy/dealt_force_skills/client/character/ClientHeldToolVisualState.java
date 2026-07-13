package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.skill.HeldToolVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class ClientHeldToolVisualState {
    private static final int TTL_TICKS = 12;
    private static final Map<Integer, State> STATES = new HashMap<>();

    private ClientHeldToolVisualState() {
    }

    public static void sync(int entityId, int visualId) {
        HeldToolVisual visual = HeldToolVisual.byId(visualId);
        if (visual == HeldToolVisual.NONE) {
            STATES.remove(entityId);
            return;
        }
        STATES.put(entityId, new State(visual, TTL_TICKS));
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            STATES.clear();
            return;
        }
        STATES.replaceAll((id, state) -> state.tick());
        STATES.entrySet().removeIf(entry ->
                entry.getValue().ttlTicks() <= 0
                        || minecraft.level.getEntity(entry.getKey()) == null);
    }

    public static void reset() {
        STATES.clear();
    }

    public static HeldToolVisual visual(Entity entity) {
        State state = entity == null ? null : STATES.get(entity.getId());
        return state == null ? HeldToolVisual.NONE : state.visual();
    }

    public static HeldToolVisual visual(int entityId) {
        State state = STATES.get(entityId);
        return state == null ? HeldToolVisual.NONE : state.visual();
    }

    private record State(HeldToolVisual visual, int ttlTicks) {
        private State tick() {
            return new State(visual, ttlTicks - 1);
        }
    }
}
