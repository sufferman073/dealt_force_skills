package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientNTwoHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_NTwoRevealEntities {
    private final List<Integer> entityIds;

    public S2C_NTwoRevealEntities(List<Integer> entityIds) {
        this.entityIds = List.copyOf(entityIds);
    }

    public static void encode(S2C_NTwoRevealEntities msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityIds.size());
        for (Integer entityId : msg.entityIds) {
            buf.writeVarInt(entityId);
        }
    }

    public static S2C_NTwoRevealEntities decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(buf.readVarInt());
        }
        return new S2C_NTwoRevealEntities(ids);
    }

    public static void handle(S2C_NTwoRevealEntities msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientNTwoHudState.revealEntities(msg.entityIds)
        ));
        ctx.get().setPacketHandled(true);
    }
}
