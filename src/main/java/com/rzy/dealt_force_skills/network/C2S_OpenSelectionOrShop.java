package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.CharacterAvailability;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.saeed.SaeedStateManager;
import com.rzy.dealt_force_skills.shop.GhrothArmoryManager;
import com.rzy.dealt_force_skills.shop.HaffShopManager;
import com.rzy.dealt_force_skills.shop.LexNinjiaShopManager;
import com.rzy.dealt_force_skills.shop.SaeedRecruitManager;
import com.rzy.dealt_force_skills.shop.UndeadShopManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_OpenSelectionOrShop {
    public static void encode(C2S_OpenSelectionOrShop msg, FriendlyByteBuf buf) {
    }

    public static C2S_OpenSelectionOrShop decode(FriendlyByteBuf buf) {
        return new C2S_OpenSelectionOrShop();
    }

    public static void handle(C2S_OpenSelectionOrShop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator()) {
                return;
            }
            if (CharacterSelectionManager.hasCharacterReselection(player)) {
                CharacterAvailability.syncToClient(player);
                NetworkHandler.sendToPlayer(new S2C_OpenCharacterSelection(true), player);
            } else if (LexNinjiaShopManager.shouldOpen(player)) {
                LexNinjiaShopManager.open(player);
            } else if (UndeadShopManager.shouldOpen(player)) {
                UndeadShopManager.open(player);
            } else if (SaeedStateManager.isSaeed(player)
                    && player.isShiftKeyDown()
                    && HaffShopManager.shouldOpenShop(player, true)) {
                HaffShopManager.openShop(player, true);
            } else if (SaeedRecruitManager.shouldOpen(player)) {
                SaeedRecruitManager.open(player);
            } else if (GhrothArmoryManager.shouldOpen(player)) {
                GhrothArmoryManager.open(player);
            } else if (HaffShopManager.shouldOpenShop(player)) {
                HaffShopManager.openShop(player);
            } else if (CharacterSelectionManager.isNormalPlayer(player)) {
                return;
            } else {
                CharacterAvailability.syncToClient(player);
                NetworkHandler.sendToPlayer(new S2C_OpenCharacterSelection(), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
