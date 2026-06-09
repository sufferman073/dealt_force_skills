package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.manba.ManbaStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_ManbaLoadout {
    private final int[] talents;
    private final int[] bulbs;
    private final int lens;
    private final int[] batteries;

    public C2S_ManbaLoadout(int[] talents, int[] bulbs, int lens, int[] batteries) {
        this.talents = talents == null ? new int[0] : talents;
        this.bulbs = bulbs == null ? new int[0] : bulbs;
        this.lens = lens;
        this.batteries = batteries == null ? new int[0] : batteries;
    }

    public static void encode(C2S_ManbaLoadout msg, FriendlyByteBuf buf) {
        writeArray(buf, msg.talents);
        writeArray(buf, msg.bulbs);
        buf.writeVarInt(msg.lens);
        writeArray(buf, msg.batteries);
    }

    public static C2S_ManbaLoadout decode(FriendlyByteBuf buf) {
        int[] talents = readArray(buf, 12);
        int[] bulbs = readArray(buf, 5);
        int lens = buf.readVarInt();
        int[] batteries = readArray(buf, 3);
        return new C2S_ManbaLoadout(talents, bulbs, lens, batteries);
    }

    public static void handle(C2S_ManbaLoadout msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ManbaStateManager.configure(player, msg.talents, msg.bulbs, msg.lens, msg.batteries);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void writeArray(FriendlyByteBuf buf, int[] values) {
        buf.writeVarInt(values.length);
        for (int value : values) {
            buf.writeVarInt(value);
        }
    }

    private static int[] readArray(FriendlyByteBuf buf, int maxLength) {
        int encodedLength = Math.max(0, buf.readVarInt());
        int length = Math.min(maxLength, encodedLength);
        int[] values = new int[length];
        for (int i = 0; i < encodedLength; i++) {
            int value = buf.readVarInt();
            if (i < length) {
                values[i] = value;
            }
        }
        return values;
    }
}
