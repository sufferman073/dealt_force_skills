package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.character.undead.UndeadSkillInputAction;
import com.rzy.dealt_force_skills.character.undead.UndeadSkills;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_UndeadSkillInput(SkillSlot slot, UndeadSkillInputAction action, int heldTicks) {
    public static void encode(C2S_UndeadSkillInput msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot.ordinal());
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(Math.max(0, msg.heldTicks));
    }

    public static C2S_UndeadSkillInput decode(FriendlyByteBuf buf) {
        int slotOrdinal = buf.readVarInt();
        int actionOrdinal = buf.readVarInt();
        int heldTicks = Math.max(0, buf.readVarInt());
        SkillSlot[] slots = SkillSlot.values();
        UndeadSkillInputAction[] actions = UndeadSkillInputAction.values();
        SkillSlot slot = slotOrdinal >= 0 && slotOrdinal < slots.length
                ? slots[slotOrdinal]
                : SkillSlot.PASSIVE;
        UndeadSkillInputAction action = actionOrdinal >= 0 && actionOrdinal < actions.length
                ? actions[actionOrdinal]
                : UndeadSkillInputAction.RELEASE;
        return new C2S_UndeadSkillInput(slot, action, heldTicks);
    }

    public static void handle(C2S_UndeadSkillInput msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                UndeadSkills.handleInput(player, msg.slot, msg.action, msg.heldTicks);
                UndeadStateManager.syncToClient(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
