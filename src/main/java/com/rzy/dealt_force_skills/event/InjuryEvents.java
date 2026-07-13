package com.rzy.dealt_force_skills.event;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.effect.InjuryManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class InjuryEvents {
    private InjuryEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0.0F) {
            InjuryManager.onDamage(player, event.getSource(),
                    CommonEvents.estimateEquipmentHitHeightRatio(player, event.getSource()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFall(LivingFallEvent event) {
        if (!event.isCanceled() && event.getDistance() > 3.0F && event.getEntity() instanceof ServerPlayer player) {
            InjuryManager.onFall(player);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            InjuryManager.onJump(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (InjuryManager.shouldPreventSprinting(event.player)) {
            event.player.setSprinting(false);
        }
        if (event.player instanceof ServerPlayer player) {
            InjuryManager.tick(player);
        }
    }
}
