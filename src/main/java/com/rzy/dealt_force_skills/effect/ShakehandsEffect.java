package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Marker effect: while held, the entity takes no damage from an attack and the attacker
 * instead takes reflected true damage, with negative/control/special-negative effects the
 * attack would have inflicted redirected back onto the attacker. See
 * {@link ShakehandsCombatHandler} for the actual combat logic; this class carries no tick
 * behaviour of its own.
 */
public class ShakehandsEffect extends MobEffect {
    public ShakehandsEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4C542);
    }
}
