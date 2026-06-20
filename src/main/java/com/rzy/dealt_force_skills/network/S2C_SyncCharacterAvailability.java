package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class S2C_SyncCharacterAvailability {
    private final Map<String, String> unavailableReasons;

    public S2C_SyncCharacterAvailability(Map<String, String> unavailableReasons) {
        this.unavailableReasons = Map.copyOf(unavailableReasons);
    }

    public static void encode(S2C_SyncCharacterAvailability msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.unavailableReasons.size());
        msg.unavailableReasons.forEach((characterId, reasonKey) -> {
            buf.writeUtf(characterId);
            buf.writeUtf(reasonKey);
        });
    }

    public static S2C_SyncCharacterAvailability decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, String> unavailableReasons = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            unavailableReasons.put(buf.readUtf(256), buf.readUtf(256));
        }
        return new S2C_SyncCharacterAvailability(unavailableReasons);
    }

    public static void handle(S2C_SyncCharacterAvailability msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientCharacterSelectionState.syncCharacterAvailability(msg.unavailableReasons)
        ));
        ctx.get().setPacketHandled(true);
    }
}
