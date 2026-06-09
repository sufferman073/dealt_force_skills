package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_UluruMissileControl {
    private final int entityId;
    private final float yaw;
    private final float pitch;
    private final boolean boosting;
    private final boolean release;

    public C2S_UluruMissileControl(int entityId, float yaw, float pitch, boolean boosting) {
        this(entityId, yaw, pitch, boosting, false);
    }

    public C2S_UluruMissileControl(int entityId, float yaw, float pitch, boolean boosting, boolean release) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
        this.boosting = boosting;
        this.release = release;
    }

    public static void encode(C2S_UluruMissileControl msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
        buf.writeBoolean(msg.boosting);
        buf.writeBoolean(msg.release);
    }

    public static C2S_UluruMissileControl decode(FriendlyByteBuf buf) {
        return new C2S_UluruMissileControl(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(C2S_UluruMissileControl msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            // Client requested release of missile control
            if (msg.release) {
                UluruLoiteringMissileEntity.stopPlayerControl(player);
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player)) {
                return;
            }
            if (player.level().getEntity(msg.entityId) instanceof UluruLoiteringMissileEntity missile
                    && missile.isGuided()
                    && missile.isOwnedBy(player)) {
                missile.applyGuidance(msg.yaw, msg.pitch, msg.boosting);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
