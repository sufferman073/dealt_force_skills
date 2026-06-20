package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.saeed.SaeedStateManager;
import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_SaeedGuardCommand {
    private final int entityId;
    private final SkillSlot slot;
    private final float yaw;
    private final float pitch;
    private final int targetEntityId;
    private final boolean hasAimPoint;
    private final double aimX;
    private final double aimY;
    private final double aimZ;

    public C2S_SaeedGuardCommand(int entityId, SkillSlot slot, float yaw, float pitch) {
        this(entityId, slot, yaw, pitch, -1, null);
    }

    public C2S_SaeedGuardCommand(int entityId, SkillSlot slot, float yaw, float pitch, int targetEntityId, Vec3 aimPoint) {
        this.entityId = entityId;
        this.slot = slot == null ? SkillSlot.CORE : slot;
        this.yaw = yaw;
        this.pitch = pitch;
        this.targetEntityId = targetEntityId;
        this.hasAimPoint = aimPoint != null;
        this.aimX = aimPoint == null ? 0.0D : aimPoint.x;
        this.aimY = aimPoint == null ? 0.0D : aimPoint.y;
        this.aimZ = aimPoint == null ? 0.0D : aimPoint.z;
    }

    public static void encode(C2S_SaeedGuardCommand msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeEnum(msg.slot);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
        buf.writeVarInt(msg.targetEntityId);
        buf.writeBoolean(msg.hasAimPoint);
        if (msg.hasAimPoint) {
            buf.writeDouble(msg.aimX);
            buf.writeDouble(msg.aimY);
            buf.writeDouble(msg.aimZ);
        }
    }

    public static C2S_SaeedGuardCommand decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        SkillSlot slot = buf.readEnum(SkillSlot.class);
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        int targetEntityId = buf.readVarInt();
        boolean hasAimPoint = buf.readBoolean();
        Vec3 aimPoint = hasAimPoint ? new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()) : null;
        return new C2S_SaeedGuardCommand(entityId, slot, yaw, pitch, targetEntityId, aimPoint);
    }

    public static void handle(C2S_SaeedGuardCommand msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SaeedStateManager.commandGuardSkill(player, msg.entityId, msg.slot, msg.targetEntityId,
                        msg.hasAimPoint ? new Vec3(msg.aimX, msg.aimY, msg.aimZ) : null, msg.yaw, msg.pitch);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
