package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.client.ClientTeamSpectatorState;
import com.rzy.dealt_force_skills.skill.SkillModelVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class ClientSkillModelVisualState {
    private static final float CAST_PLAYBACK_SPEED = 3.0F;
    private static final Map<Integer, State> STATES = new HashMap<>();

    private ClientSkillModelVisualState() {
    }

    public static void sync(int entityId, int visualId, int durationTicks) {
        SkillModelVisual visual = SkillModelVisual.byId(visualId);
        if (visual == null || durationTicks <= 0) {
            STATES.remove(entityId);
            return;
        }
        STATES.put(entityId, new State(visual, durationTicks, 0));
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            STATES.clear();
            return;
        }
        STATES.replaceAll((id, state) -> state.tick());
        STATES.entrySet().removeIf(entry ->
                entry.getValue().remainingTicks() <= 0
                        || minecraft.level.getEntity(entry.getKey()) == null
                        && !ClientTeamSpectatorState.isTargetEntity(entry.getKey()));
    }

    public static void reset() {
        STATES.clear();
    }

    public static SkillModelVisual visual(Entity entity) {
        State state = entity == null ? null : STATES.get(entity.getId());
        return state == null ? null : state.visual();
    }

    public static SkillModelVisual visual(int entityId) {
        State state = STATES.get(entityId);
        return state == null ? null : state.visual();
    }

    public static float animationSeconds(Entity entity, float partialTick) {
        State state = entity == null ? null : STATES.get(entity.getId());
        return state == null ? 0.0F : (state.ageTicks() + partialTick) / 20.0F * CAST_PLAYBACK_SPEED;
    }

    public static float animationSeconds(int entityId, float partialTick) {
        State state = STATES.get(entityId);
        return state == null ? 0.0F : (state.ageTicks() + partialTick) / 20.0F * CAST_PLAYBACK_SPEED;
    }

    public static float ageSeconds(Entity entity, float partialTick) {
        State state = entity == null ? null : STATES.get(entity.getId());
        return state == null ? 0.0F : (state.ageTicks() + partialTick) / 20.0F;
    }

    public static float ageSeconds(int entityId, float partialTick) {
        State state = STATES.get(entityId);
        return state == null ? 0.0F : (state.ageTicks() + partialTick) / 20.0F;
    }

    public static int remainingTicks(Entity entity) {
        State state = entity == null ? null : STATES.get(entity.getId());
        return state == null ? 0 : state.remainingTicks();
    }

    public static int remainingTicks(int entityId) {
        State state = STATES.get(entityId);
        return state == null ? 0 : state.remainingTicks();
    }

    private record State(SkillModelVisual visual, int remainingTicks, int ageTicks) {
        private State tick() {
            return new State(visual, Math.max(0, remainingTicks - 1), ageTicks + 1);
        }
    }
}
