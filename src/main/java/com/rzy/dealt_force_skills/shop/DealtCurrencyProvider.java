package com.rzy.dealt_force_skills.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface DealtCurrencyProvider {
    String id();

    Component displayName();

    boolean supports(ServerPlayer player);

    default long balance(ServerPlayer player) {
        return grant(player, 0L);
    }

    long grant(ServerPlayer player, long amount);

    default long set(ServerPlayer player, long amount) {
        return balance(player);
    }

    default long remove(ServerPlayer player, long amount) {
        return amount <= 0L ? balance(player) : set(player, Math.max(0L, balance(player) - amount));
    }
}
