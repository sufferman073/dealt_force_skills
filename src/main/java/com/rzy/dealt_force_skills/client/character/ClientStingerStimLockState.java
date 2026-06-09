package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.stinger.StingerStimMode;

public final class ClientStingerStimLockState {
    private static long warningUntilMs;
    private static StingerStimMode mode = StingerStimMode.HEAL;

    private ClientStingerStimLockState() {
    }

    public static void warn(int modeOrdinal, int durationTicks) {
        StingerStimMode[] modes = StingerStimMode.values();
        mode = modeOrdinal >= 0 && modeOrdinal < modes.length ? modes[modeOrdinal] : StingerStimMode.HEAL;
        warningUntilMs = System.currentTimeMillis() + Math.max(1, durationTicks) * 50L;
    }

    public static boolean active() {
        return System.currentTimeMillis() < warningUntilMs;
    }

    public static StingerStimMode mode() {
        return mode;
    }
}
