package com.rzy.dealt_force_skills.client.visual;

import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ClientSinevaVisualState {
    private static final int BASH_ANIMATION_TICKS = 8;
    private static final int CHARGE_ANIMATION_TICKS = 12;

    private static int bashTicks;
    private static int chargeTicks;

    private ClientSinevaVisualState() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !ClientSinevaHudState.shouldRender()) {
            reset();
            return;
        }

        if (bashTicks > 0) {
            bashTicks--;
        }
        if (chargeTicks > 0) {
            chargeTicks--;
        }
    }

    public static float bashProgress(float partialTick) {
        return progress(bashTicks, BASH_ANIMATION_TICKS, partialTick);
    }

    public static float chargeProgress(float partialTick) {
        return progress(chargeTicks, CHARGE_ANIMATION_TICKS, partialTick);
    }

    public static void startBashAnimation() {
        bashTicks = BASH_ANIMATION_TICKS;
    }

    public static void startChargeAnimation() {
        chargeTicks = CHARGE_ANIMATION_TICKS;
    }

    private static float progress(int ticksRemaining, int maxTicks, float partialTick) {
        if (ticksRemaining <= 0) {
            return 0.0f;
        }
        return Mth.clamp((ticksRemaining + partialTick) / maxTicks, 0.0f, 1.0f);
    }

    private static void reset() {
        bashTicks = 0;
        chargeTicks = 0;
    }
}
