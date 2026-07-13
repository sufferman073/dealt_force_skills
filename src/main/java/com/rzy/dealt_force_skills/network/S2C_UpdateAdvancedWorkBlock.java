package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.AdvancedWorkBlockData;
import com.rzy.dealt_force_skills.client.screen.AdvancedWorkBlockScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_UpdateAdvancedWorkBlock {
    private final AdvancedWorkBlockData data;

    public S2C_UpdateAdvancedWorkBlock(AdvancedWorkBlockData data) {
        this.data = data;
    }

    public static void encode(S2C_UpdateAdvancedWorkBlock msg, FriendlyByteBuf buf) {
        S2C_OpenAdvancedWorkBlock.writeData(msg.data, buf);
    }

    public static S2C_UpdateAdvancedWorkBlock decode(FriendlyByteBuf buf) {
        return new S2C_UpdateAdvancedWorkBlock(S2C_OpenAdvancedWorkBlock.readData(buf));
    }

    public static void handle(S2C_UpdateAdvancedWorkBlock msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> AdvancedWorkBlockScreen.update(msg.data)
        ));
        ctx.get().setPacketHandled(true);
    }
}
