package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientUndeadHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_SyncUndeadState(
        int professionOrdinal,
        float energy,
        float maxEnergy,
        int disorientedTicks,
        int coreCooldownTicks,
        int knightShieldTicks,
        float knightShield,
        boolean warriorMight,
        int warriorBloodlustTicks,
        int explorerMeditationTicks,
        int explorerSpaceTicks,
        boolean explorerCanSeeEntities,
        boolean explorerExtendedGuidance,
        int rogueInvisibleTicks,
        int scholarRitualTicks,
        boolean hunterScatter,
        int hunterExhaustedTicks
) {
    public static void encode(S2C_SyncUndeadState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.professionOrdinal);
        buf.writeFloat(msg.energy);
        buf.writeFloat(msg.maxEnergy);
        buf.writeVarInt(msg.disorientedTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.knightShieldTicks);
        buf.writeFloat(msg.knightShield);
        buf.writeBoolean(msg.warriorMight);
        buf.writeVarInt(msg.warriorBloodlustTicks);
        buf.writeVarInt(msg.explorerMeditationTicks);
        buf.writeVarInt(msg.explorerSpaceTicks);
        buf.writeBoolean(msg.explorerCanSeeEntities);
        buf.writeBoolean(msg.explorerExtendedGuidance);
        buf.writeVarInt(msg.rogueInvisibleTicks);
        buf.writeVarInt(msg.scholarRitualTicks);
        buf.writeBoolean(msg.hunterScatter);
        buf.writeVarInt(msg.hunterExhaustedTicks);
    }

    public static S2C_SyncUndeadState decode(FriendlyByteBuf buf) {
        return new S2C_SyncUndeadState(
                buf.readVarInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncUndeadState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientUndeadHudState.sync(
                        msg.professionOrdinal,
                        msg.energy,
                        msg.maxEnergy,
                        msg.disorientedTicks,
                        msg.coreCooldownTicks,
                        msg.knightShieldTicks,
                        msg.knightShield,
                        msg.warriorMight,
                        msg.warriorBloodlustTicks,
                        msg.explorerMeditationTicks,
                        msg.explorerSpaceTicks,
                        msg.explorerCanSeeEntities,
                        msg.explorerExtendedGuidance,
                        msg.rogueInvisibleTicks,
                        msg.scholarRitualTicks,
                        msg.hunterScatter,
                        msg.hunterExhaustedTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
