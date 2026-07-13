package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.client.ClientTeamSpectatorState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_TeamSpectatorTarget(boolean managed, int targetEntityId, String targetName,
                                      String characterId, ItemStack mainHand, ItemStack offHand) {
    public static S2C_TeamSpectatorTarget disabled() {
        return new S2C_TeamSpectatorTarget(false, -1, "", "", ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public static S2C_TeamSpectatorTarget managed(ServerPlayer target) {
        if (target == null) {
            return new S2C_TeamSpectatorTarget(true, -1, "", "", ItemStack.EMPTY, ItemStack.EMPTY);
        }
        return new S2C_TeamSpectatorTarget(true, target.getId(), target.getGameProfile().getName(),
                CharacterSelectionManager.getSelectedCharacterId(target).orElse(""),
                target.getMainHandItem().copy(), target.getOffhandItem().copy());
    }

    public static void encode(S2C_TeamSpectatorTarget msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.managed);
        buf.writeVarInt(msg.targetEntityId);
        buf.writeUtf(msg.targetName);
        buf.writeUtf(msg.characterId);
        buf.writeItem(msg.mainHand);
        buf.writeItem(msg.offHand);
    }

    public static S2C_TeamSpectatorTarget decode(FriendlyByteBuf buf) {
        return new S2C_TeamSpectatorTarget(buf.readBoolean(), buf.readVarInt(), buf.readUtf(), buf.readUtf(),
                buf.readItem(), buf.readItem());
    }

    public static void handle(S2C_TeamSpectatorTarget msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientTeamSpectatorState.sync(msg.managed, msg.targetEntityId, msg.targetName,
                        msg.characterId, msg.mainHand, msg.offHand)));
        ctx.get().setPacketHandled(true);
    }
}
