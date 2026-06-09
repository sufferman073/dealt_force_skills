package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.UndeadShopScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_OpenUndeadShop(long souls, int professionOrdinal, CompoundTag upgrades) {
    public S2C_OpenUndeadShop {
        souls = Math.max(0L, souls);
        upgrades = upgrades == null ? new CompoundTag() : upgrades.copy();
    }

    public static void encode(S2C_OpenUndeadShop msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.souls);
        buf.writeVarInt(msg.professionOrdinal);
        buf.writeNbt(msg.upgrades);
    }

    public static S2C_OpenUndeadShop decode(FriendlyByteBuf buf) {
        CompoundTag upgrades;
        long souls = buf.readLong();
        int profession = buf.readVarInt();
        upgrades = buf.readNbt();
        return new S2C_OpenUndeadShop(souls, profession, upgrades);
    }

    public static void handle(S2C_OpenUndeadShop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> UndeadShopScreen.open(msg.souls, msg.professionOrdinal, msg.upgrades)
        ));
        ctx.get().setPacketHandled(true);
    }
}
