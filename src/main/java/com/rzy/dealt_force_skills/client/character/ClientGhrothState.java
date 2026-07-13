package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
public final class ClientGhrothState {
    private static volatile int NOON_GAZE_TOTAL_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("NOON_GAZE_TOTAL_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("client.character.client_ghroth_state.noon_gaze_total_ticks", 120));
    private static volatile int NOON_OUTPUT_WINDOW_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("NOON_OUTPUT_WINDOW_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("client.character.client_ghroth_state.noon_output_window_ticks", 60));
    private static int starsCooldownTicks;
    private static int justiceCooldownTicks;
    private static int noonCooldownTicks;
    private static int starsActiveTicks;
    private static int justiceActiveTicks;
    private static int noonActiveTicks;
    private static int noonGazeTargetId;
    private static int noonGazeTicks;
    private static int noonDamageCopies;
    private static boolean tacticalImmune;

    private ClientGhrothState() {
    }

    public static void sync(int starsCooldownTicks, int justiceCooldownTicks, int noonCooldownTicks,
                            int starsActiveTicks, int justiceActiveTicks, int noonActiveTicks,
                            int noonGazeTargetId, int noonGazeTicks, int noonDamageCopies,
                            boolean tacticalImmune) {
        ClientGhrothState.starsCooldownTicks = Math.max(0, starsCooldownTicks);
        ClientGhrothState.justiceCooldownTicks = Math.max(0, justiceCooldownTicks);
        ClientGhrothState.noonCooldownTicks = Math.max(0, noonCooldownTicks);
        ClientGhrothState.starsActiveTicks = Math.max(0, starsActiveTicks);
        ClientGhrothState.justiceActiveTicks = Math.max(0, justiceActiveTicks);
        ClientGhrothState.noonActiveTicks = Math.max(0, noonActiveTicks);
        ClientGhrothState.noonGazeTargetId = Math.max(0, noonGazeTargetId);
        ClientGhrothState.noonGazeTicks = Math.max(0, noonGazeTicks);
        ClientGhrothState.noonDamageCopies = Math.max(0, noonDamageCopies);
        ClientGhrothState.tacticalImmune = tacticalImmune;
    }

    public static void clientTick() {
        starsCooldownTicks = Math.max(0, starsCooldownTicks - 1);
        justiceCooldownTicks = Math.max(0, justiceCooldownTicks - 1);
        noonCooldownTicks = Math.max(0, noonCooldownTicks - 1);
        starsActiveTicks = Math.max(0, starsActiveTicks - 1);
        justiceActiveTicks = Math.max(0, justiceActiveTicks - 1);
        noonActiveTicks = Math.max(0, noonActiveTicks - 1);
        if (noonActiveTicks <= 0 || noonGazeTargetId <= 0) {
            noonGazeTargetId = 0;
            noonGazeTicks = 0;
        } else if (noonActiveTicks > NOON_OUTPUT_WINDOW_TICKS) {
            noonGazeTicks = Math.min(NOON_GAZE_TOTAL_TICKS, noonGazeTicks + 1);
        }
    }

    public static void reset() {
        sync(0, 0, 0, 0, 0, 0, 0, 0, 0, false);
    }

    public static int starsCooldownTicks() {
        return starsCooldownTicks;
    }

    public static int justiceCooldownTicks() {
        return justiceCooldownTicks;
    }

    public static int noonCooldownTicks() {
        return noonCooldownTicks;
    }

    public static int starsActiveTicks() {
        return starsActiveTicks;
    }

    public static int justiceActiveTicks() {
        return justiceActiveTicks;
    }

    public static int noonActiveTicks() {
        return noonActiveTicks;
    }

    public static int noonGazeTargetId() {
        return noonGazeTargetId;
    }

    public static int noonGazeTicks() {
        return noonGazeTicks;
    }

    public static int noonDamageCopies() {
        return noonDamageCopies;
    }

    public static boolean tacticalImmune() {
        return tacticalImmune;
    }
}
