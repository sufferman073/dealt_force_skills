package com.rzy.dealt_force_skills.effect;

import com.rzy.dealt_force_skills.character.ntwo.NTwoStateManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class NTwoFrozenEffect extends MobEffect {
    public NTwoFrozenEffect() {
        super(MobEffectCategory.HARMFUL, 0x8FDFFF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setDeltaMovement(0.0D, Math.min(0.0D, entity.getDeltaMovement().y), 0.0D);
        entity.hurtMarked = true;
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
        NTwoStateManager.clearCold(entity);
    }
}
