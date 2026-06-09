package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientLunaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_LunaRevealEntities {
    private final List<Integer> entityIds;
    private final int ticks;

    public S2C_LunaRevealEntities(List<Integer> entityIds, int ticks) {
        this.entityIds = List.copyOf(entityIds);
        this.ticks = ticks;
    }

    public static void encode(S2C_LunaRevealEntities msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.ticks);
        buf.writeVarInt(msg.entityIds.size());
        for (Integer entityId : msg.entityIds) {
            buf.writeVarInt(entityId);
        }
    }

    public static S2C_LunaRevealEntities decode(FriendlyByteBuf buf) {
        int ticks = buf.readVarInt();
        int count = buf.readVarInt();
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(buf.readVarInt());
        }
        return new S2C_LunaRevealEntities(ids, ticks);
    }

    public static void handle(S2C_LunaRevealEntities msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientLunaHudState.revealEntities(msg.entityIds, msg.ticks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
