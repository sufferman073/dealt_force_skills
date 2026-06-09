package com.rzy.dealt_force_skills.effect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;

public class ManbaBlindedEffect extends MobEffect {
    private static final String LIGHT_WARRIOR_SLOW_UUID = "e134cf46-5f93-4e65-a5fd-3d137c2fd743";

    public ManbaBlindedEffect() {
        super(MobEffectCategory.NEUTRAL, 0xF6F3DA);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, LIGHT_WARRIOR_SLOW_UUID,
                -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        suppressHostileTargeting(entity);
    }

    public static void suppressHostileTargeting(LivingEntity entity) {
        if (entity.level().isClientSide || !(entity instanceof Mob mob)) {
            return;
        }
        if (!(mob instanceof Enemy) && mob.getTarget() == null) {
            return;
        }

        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.setLastHurtByMob(null);
        mob.setLastHurtByPlayer(null);
        mob.setLastHurtMob(null);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return amplifier >= 1 ? super.getAttributeModifierValue(amplifier, modifier) : 0.0D;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
