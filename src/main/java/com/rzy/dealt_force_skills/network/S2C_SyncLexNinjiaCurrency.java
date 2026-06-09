package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientLexNinjiaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_SyncLexNinjiaCurrency(long lotusBoxes) {
    public static void encode(S2C_SyncLexNinjiaCurrency msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.lotusBoxes);
    }

    public static S2C_SyncLexNinjiaCurrency decode(FriendlyByteBuf buf) {
        return new S2C_SyncLexNinjiaCurrency(buf.readLong());
    }

    public static void handle(S2C_SyncLexNinjiaCurrency msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientLexNinjiaHudState.syncLotusBoxes(msg.lotusBoxes)
        ));
        ctx.get().setPacketHandled(true);
    }
}
