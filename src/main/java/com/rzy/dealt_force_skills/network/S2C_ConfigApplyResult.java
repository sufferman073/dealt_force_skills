package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.DealtForceConfigScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class S2C_ConfigApplyResult {
    private final boolean success;
    private final int appliedCount;
    private final String code;

    public S2C_ConfigApplyResult(boolean success, int appliedCount, String code) {
        this.success = success;
        this.appliedCount = appliedCount;
        this.code = code == null ? "" : code;
    }

    public static void encode(S2C_ConfigApplyResult msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.success);
        buf.writeVarInt(msg.appliedCount);
        buf.writeUtf(msg.code, 64);
    }

    public static S2C_ConfigApplyResult decode(FriendlyByteBuf buf) {
        return new S2C_ConfigApplyResult(buf.readBoolean(), buf.readVarInt(), buf.readUtf(64));
    }

    public static void handle(S2C_ConfigApplyResult msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                DealtForceConfigScreen.onApplyResult(msg.success, msg.appliedCount, msg.code)));
        ctx.get().setPacketHandled(true);
    }
}
