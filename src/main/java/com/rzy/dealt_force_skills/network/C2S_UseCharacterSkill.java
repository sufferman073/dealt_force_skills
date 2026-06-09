package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.skill.SkillDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_UseCharacterSkill {
    private final SkillSlot slot;
    private final boolean alternate;

    public C2S_UseCharacterSkill(SkillSlot slot) {
        this(slot, false);
    }

    public C2S_UseCharacterSkill(SkillSlot slot, boolean alternate) {
        this.slot = slot;
        this.alternate = alternate;
    }

    public static void encode(C2S_UseCharacterSkill msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot.ordinal());
        buf.writeBoolean(msg.alternate);
    }

    public static C2S_UseCharacterSkill decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        SkillSlot[] values = SkillSlot.values();
        SkillSlot slot = ordinal >= 0 && ordinal < values.length ? values[ordinal] : SkillSlot.PASSIVE;
        boolean alternate = buf.readBoolean();
        return new C2S_UseCharacterSkill(slot, alternate);
    }

    public static void handle(C2S_UseCharacterSkill msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            SkillDispatcher.useSkill(player, msg.slot, msg.alternate);
        });
        ctx.get().setPacketHandled(true);
    }
}
