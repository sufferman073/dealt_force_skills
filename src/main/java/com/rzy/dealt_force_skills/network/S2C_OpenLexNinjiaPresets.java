package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.LexNinjiaPresetScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_OpenLexNinjiaPresets(boolean configure, CompoundTag data) {
    public S2C_OpenLexNinjiaPresets {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static void encode(S2C_OpenLexNinjiaPresets msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.configure);
        buf.writeNbt(msg.data);
    }

    public static S2C_OpenLexNinjiaPresets decode(FriendlyByteBuf buf) {
        return new S2C_OpenLexNinjiaPresets(buf.readBoolean(), buf.readNbt());
    }

    public static void handle(S2C_OpenLexNinjiaPresets msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> LexNinjiaPresetScreen.open(msg.configure, msg.data)
        ));
        ctx.get().setPacketHandled(true);
    }
}
