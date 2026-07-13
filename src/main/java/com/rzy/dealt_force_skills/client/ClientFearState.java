package com.rzy.dealt_force_skills.client;

public final class ClientFearState {
    private static int stacks;

    private ClientFearState() {
    }

    public static void setStacks(int value) {
        stacks = Math.max(0, value);
    }

    public static int stacks() {
        return stacks;
    }
}
