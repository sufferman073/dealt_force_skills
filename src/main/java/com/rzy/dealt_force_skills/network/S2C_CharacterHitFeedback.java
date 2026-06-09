package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.CharacterHitFeedbackOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_CharacterHitFeedback {
    private final int hurtEntityId;
    private final float amount;

    public S2C_CharacterHitFeedback(int hurtEntityId, float amount) {
        this.hurtEntityId = hurtEntityId;
        this.amount = amount;
    }

    public static void encode(S2C_CharacterHitFeedback msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hurtEntityId);
        buf.writeFloat(msg.amount);
    }

    public static S2C_CharacterHitFeedback decode(FriendlyByteBuf buf) {
        return new S2C_CharacterHitFeedback(buf.readVarInt(), buf.readFloat());
    }

    public static void handle(S2C_CharacterHitFeedback msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    if (Minecraft.getInstance().level != null
                            && Minecraft.getInstance().level.getEntity(msg.hurtEntityId) != null) {
                        CharacterHitFeedbackOverlay.markHit(msg.amount);
                    }
                }
        ));
        ctx.get().setPacketHandled(true);
    }
}
