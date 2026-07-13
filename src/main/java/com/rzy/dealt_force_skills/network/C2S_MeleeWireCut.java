package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.BladeWireBlockEntity;
import com.rzy.dealt_force_skills.compat.LesRaisinsTacticalCompat;
import com.rzy.dealt_force_skills.util.MeleeWeaponCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_MeleeWireCut {
    private static final double DIRECT_TARGET_RANGE = 5.0D;
    private static final String LAST_SUCCESS_TICK = "dealt_force_skills.melee_wire_cut_packet_tick";

    private final boolean specialAttack;

    public C2S_MeleeWireCut(boolean specialAttack) {
        this.specialAttack = specialAttack;
    }

    public static void encode(C2S_MeleeWireCut msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.specialAttack);
    }

    public static C2S_MeleeWireCut decode(FriendlyByteBuf buf) {
        return new C2S_MeleeWireCut(buf.readBoolean());
    }

    public static void handle(C2S_MeleeWireCut msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator() || !player.isAlive()
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            long now = level.getGameTime();
            if (player.getPersistentData().getLong(LAST_SUCCESS_TICK) == now) {
                return;
            }

            ItemStack weapon = player.getMainHandItem();
            if (!MeleeWeaponCompat.isMeleeWeapon(weapon)) {
                return;
            }

            boolean cut = false;
            if (MeleeWeaponCompat.isLesRaisinsMelee(weapon)) {
                LesRaisinsTacticalCompat.MeleeCutShape shape =
                        LesRaisinsTacticalCompat.inputAttackShape(player, msg.specialAttack);
                cut = BladeWireBlockEntity.destroyWiresInMeleeArc(player, shape.range(), shape.halfWidth(), shape.height());
                if (!cut) {
                    cut = BladeWireBlockEntity.destroyTargetedWire(player, DIRECT_TARGET_RANGE);
                }
            } else if (!msg.specialAttack) {
                cut = BladeWireBlockEntity.destroyTargetedWire(player, DIRECT_TARGET_RANGE);
            }

            if (cut) {
                player.getPersistentData().putLong(LAST_SUCCESS_TICK, now);
                damageCuttingWeapon(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void damageCuttingWeapon(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.isEmpty() && !player.getAbilities().instabuild) {
            weapon.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }
}
