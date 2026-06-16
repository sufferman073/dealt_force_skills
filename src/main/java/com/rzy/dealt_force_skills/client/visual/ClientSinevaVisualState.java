package com.rzy.dealt_force_skills.client.visual;

import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

public final class ClientSinevaVisualState {
    private static final int BASH_ANIMATION_TICKS = 5;
    private static final int CHARGE_ANIMATION_TICKS = 14;
    private static final int READY_ANIMATION_TICKS = 9;

    private static int bashTicks;
    private static int chargeTicks;
    private static int readyTicks;
    private static String bashAnimation = "shield_hit_random1";
    private static boolean shieldWasDeployed;

    private ClientSinevaVisualState() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !ClientSinevaHudState.shouldRender()) {
            reset();
            return;
        }

        boolean shieldDeployed = ClientSinevaHudState.shieldDeployed();
        if (shieldDeployed && !shieldWasDeployed) {
            readyTicks = READY_ANIMATION_TICKS;
        }
        shieldWasDeployed = shieldDeployed;
        if (bashTicks > 0) {
            bashTicks--;
        }
        if (chargeTicks > 0) {
            chargeTicks--;
        }
        if (readyTicks > 0) {
            readyTicks--;
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
        bashAnimation = ThreadLocalRandom.current().nextBoolean()
                ? "shield_hit_random1"
                : "shield_hit_random2";
    }

    public static void startChargeAnimation() {
        chargeTicks = CHARGE_ANIMATION_TICKS;
    }

    public static String shieldAnimation() {
        if (chargeTicks > 0) {
            return "shield_crash";
        }
        if (bashTicks > 0) {
            return bashAnimation;
        }
        if (readyTicks > 0) {
            return "shield_ready";
        }
        return "idle";
    }

    public static float shieldAnimationSeconds(float partialTick) {
        if (chargeTicks > 0) {
            return elapsed(chargeTicks, CHARGE_ANIMATION_TICKS, partialTick);
        }
        if (bashTicks > 0) {
            return elapsed(bashTicks, BASH_ANIMATION_TICKS, partialTick);
        }
        if (readyTicks > 0) {
            return elapsed(readyTicks, READY_ANIMATION_TICKS, partialTick);
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0.0F
                : (minecraft.level.getGameTime() + partialTick) / 20.0F;
    }

    private static float progress(int ticksRemaining, int maxTicks, float partialTick) {
        if (ticksRemaining <= 0) {
            return 0.0f;
        }
        return Mth.clamp((ticksRemaining + partialTick) / maxTicks, 0.0f, 1.0f);
    }

    private static float elapsed(int ticksRemaining, int maxTicks, float partialTick) {
        return Math.max(0.0F, (maxTicks - ticksRemaining + partialTick) / 20.0F);
    }

    private static void reset() {
        bashTicks = 0;
        chargeTicks = 0;
        readyTicks = 0;
        shieldWasDeployed = false;
    }
}
