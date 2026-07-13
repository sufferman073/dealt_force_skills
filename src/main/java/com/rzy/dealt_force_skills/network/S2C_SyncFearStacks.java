package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.ClientFearState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncFearStacks {
    private final int stacks;

    public S2C_SyncFearStacks(int stacks) {
        this.stacks = stacks;
    }

    public static void encode(S2C_SyncFearStacks msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.stacks);
    }

    public static S2C_SyncFearStacks decode(FriendlyByteBuf buf) {
        return new S2C_SyncFearStacks(buf.readVarInt());
    }

    public static void handle(S2C_SyncFearStacks msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientFearState.setStacks(msg.stacks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
