package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientGamblerHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncGamblerState {
    private final int chips;
    private final int shield;
    private final int active1CooldownTicks;
    private final int active2CooldownTicks;
    private final int active2UsesLeft;
    private final int active2ExpiresTicks;
    private final boolean coreActive;
    private final int jackpotTicks;
    private final int invulnerableTicks;
    private final int[] powers;

    public S2C_SyncGamblerState(int chips, int shield, int active1CooldownTicks, int active2CooldownTicks,
                                int active2UsesLeft, int active2ExpiresTicks, boolean coreActive,
                                int jackpotTicks, int invulnerableTicks, int[] powers) {
        this.chips = chips;
        this.shield = shield;
        this.active1CooldownTicks = active1CooldownTicks;
        this.active2CooldownTicks = active2CooldownTicks;
        this.active2UsesLeft = active2UsesLeft;
        this.active2ExpiresTicks = active2ExpiresTicks;
        this.coreActive = coreActive;
        this.jackpotTicks = jackpotTicks;
        this.invulnerableTicks = invulnerableTicks;
        this.powers = powers == null ? new int[0] : powers;
    }

    public static void encode(S2C_SyncGamblerState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.chips);
        buf.writeVarInt(msg.shield);
        buf.writeVarInt(msg.active1CooldownTicks);
        buf.writeVarInt(msg.active2CooldownTicks);
        buf.writeVarInt(msg.active2UsesLeft);
        buf.writeVarInt(msg.active2ExpiresTicks);
        buf.writeBoolean(msg.coreActive);
        buf.writeVarInt(msg.jackpotTicks);
        buf.writeVarInt(msg.invulnerableTicks);
        buf.writeVarInt(msg.powers.length);
        for (int value : msg.powers) {
            buf.writeVarInt(value);
        }
    }

    public static S2C_SyncGamblerState decode(FriendlyByteBuf buf) {
        int chips = buf.readVarInt();
        int shield = buf.readVarInt();
        int active1CooldownTicks = buf.readVarInt();
        int active2CooldownTicks = buf.readVarInt();
        int active2UsesLeft = buf.readVarInt();
        int active2ExpiresTicks = buf.readVarInt();
        boolean coreActive = buf.readBoolean();
        int jackpotTicks = buf.readVarInt();
        int invulnerableTicks = buf.readVarInt();
        int encodedLength = Math.max(0, buf.readVarInt());
        int length = Math.min(96, encodedLength);
        int[] powers = new int[length];
        for (int i = 0; i < encodedLength; i++) {
            int value = buf.readVarInt();
            if (i < length) {
                powers[i] = value;
            }
        }
        return new S2C_SyncGamblerState(chips, shield, active1CooldownTicks, active2CooldownTicks,
                active2UsesLeft, active2ExpiresTicks, coreActive, jackpotTicks, invulnerableTicks, powers);
    }

    public static void handle(S2C_SyncGamblerState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientGamblerHudState.sync(
                        msg.chips,
                        msg.shield,
                        msg.active1CooldownTicks,
                        msg.active2CooldownTicks,
                        msg.active2UsesLeft,
                        msg.active2ExpiresTicks,
                        msg.coreActive,
                        msg.jackpotTicks,
                        msg.invulnerableTicks,
                        msg.powers
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
