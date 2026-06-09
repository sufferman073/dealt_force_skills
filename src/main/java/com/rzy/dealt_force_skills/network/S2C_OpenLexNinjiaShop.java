package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.LexNinjiaShopScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_OpenLexNinjiaShop(long lotusBoxes, CompoundTag data) {
    public S2C_OpenLexNinjiaShop {
        lotusBoxes = Math.max(0L, lotusBoxes);
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static void encode(S2C_OpenLexNinjiaShop msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.lotusBoxes);
        buf.writeNbt(msg.data);
    }

    public static S2C_OpenLexNinjiaShop decode(FriendlyByteBuf buf) {
        long lotusBoxes = buf.readLong();
        CompoundTag data = buf.readNbt();
        return new S2C_OpenLexNinjiaShop(lotusBoxes, data);
    }

    public static void handle(S2C_OpenLexNinjiaShop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> LexNinjiaShopScreen.open(msg.lotusBoxes, msg.data)
        ));
        ctx.get().setPacketHandled(true);
    }
}
