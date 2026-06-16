package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientSkillModelVisualState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_SkillModelVisual(int entityId, int visualId, int durationTicks) {
    public static void encode(S2C_SkillModelVisual msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.visualId);
        buf.writeVarInt(msg.durationTicks);
    }

    public static S2C_SkillModelVisual decode(FriendlyByteBuf buf) {
        return new S2C_SkillModelVisual(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(S2C_SkillModelVisual msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSkillModelVisualState.sync(
                        msg.entityId, msg.visualId, msg.durationTicks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
