package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientManbaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_ManbaDuelView {
    private final boolean active;
    private final int ownerEntityId;
    private final int targetEntityId;
    private final String ownerName;
    private final String targetName;
    private final int affection;
    private final int remainingTicks;

    public S2C_ManbaDuelView(boolean active, int ownerEntityId, int targetEntityId,
                             String ownerName, String targetName, int affection, int remainingTicks) {
        this.active = active;
        this.ownerEntityId = ownerEntityId;
        this.targetEntityId = targetEntityId;
        this.ownerName = ownerName == null ? "" : ownerName;
        this.targetName = targetName == null ? "" : targetName;
        this.affection = affection;
        this.remainingTicks = remainingTicks;
    }

    public static void encode(S2C_ManbaDuelView msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.ownerEntityId);
        buf.writeVarInt(msg.targetEntityId);
        buf.writeUtf(msg.ownerName, 96);
        buf.writeUtf(msg.targetName, 96);
        buf.writeVarInt(msg.affection);
        buf.writeVarInt(msg.remainingTicks);
    }

    public static S2C_ManbaDuelView decode(FriendlyByteBuf buf) {
        return new S2C_ManbaDuelView(
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readUtf(96),
                buf.readUtf(96),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_ManbaDuelView msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientManbaHudState.syncDuelView(
                        msg.active,
                        msg.ownerEntityId,
                        msg.targetEntityId,
                        msg.ownerName,
                        msg.targetName,
                        msg.affection,
                        msg.remainingTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
