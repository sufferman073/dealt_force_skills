package com.rzy.dealt_force_skills.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface DealtCurrencyProvider {
    String id();

    Component displayName();

    boolean supports(ServerPlayer player);

    long grant(ServerPlayer player, long amount);
}
