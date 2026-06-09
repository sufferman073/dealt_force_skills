package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.HaffShopScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_OpenHaffShop {
    private final long coins;

    public S2C_OpenHaffShop(long coins) {
        this.coins = Math.max(0L, coins);
    }

    public static void encode(S2C_OpenHaffShop msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.coins);
    }

    public static S2C_OpenHaffShop decode(FriendlyByteBuf buf) {
        return new S2C_OpenHaffShop(buf.readLong());
    }

    public static void handle(S2C_OpenHaffShop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HaffShopScreen.open(msg.coins)
        ));
        ctx.get().setPacketHandled(true);
    }
}
