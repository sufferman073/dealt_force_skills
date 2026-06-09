package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.character.sineva.SinevaStateManager;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Grapple hook projectile.
 *
 * <p>你本地编译报错的根因是 {@code ThrownItemProjectile} 在你的映射环境里解析失败，
 * 导致继承链断掉并引发大量连锁错误。这里改为继承更基础的 {@link Projectile}，
 * 并实现 {@link ItemSupplier} 以继续沿用 {@link net.minecraft.client.renderer.entity.ThrownItemRenderer}。
 */
public class GrappleHookEntity extends Projectile implements ItemSupplier {
    private static final double MAX_DISTANCE_SQR = 35.0 * 35.0;
    private static final int PULL_DELAY_TICKS = 8;
    private static final int MAX_PULL_TICKS = 48;

    private int targetId = -1;
    private int hookedAtTick = -1;
    private LivingEntity hookedTarget;

    public GrappleHookEntity(EntityType<? extends GrappleHookEntity> type, Level level) {
        super(type, level);
    }

    public GrappleHookEntity(EntityType<? extends GrappleHookEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        this.setOwner(owner);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.TRIPWIRE_HOOK);
    }

    @Override
    protected void defineSynchedData() {
        // no-op
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (targetId == -1) {
            Entity owner = getOwner();
            if (owner != null && distanceToSqr(owner) > MAX_DISTANCE_SQR) {
                discard();
                return;
            }

            Vec3 motion = getDeltaMovement();
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() == HitResult.Type.ENTITY) {
                onHitEntity((EntityHitResult) hit);
                return;
            }
            if (hit.getType() == HitResult.Type.BLOCK) {
                discard();
                return;
            }

            setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
            checkInsideBlocks();
            return;
        }

        tickHookedTarget();
    }

    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;

        Entity owner = getOwner();
        Entity target = r.getEntity();
        if (!(owner instanceof net.minecraft.server.level.ServerPlayer sp)) {
            discard();
            return;
        }
        if (!SinevaStateManager.isSineva(sp)) {
            discard();
            return;
        }

        level().playSound(null, blockPosition(), ModSounds.GRAPPLE_HIT.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);

        if (target instanceof LivingEntity le) {
            SkillDamageHelper.hurt(le, SkillDamageHelper.sinevaGrapple(sp.serverLevel(), this, sp), sp, 10.0f);
            targetId = target.getId();
            hookedAtTick = tickCount;
            hookedTarget = le;
            setDeltaMovement(Vec3.ZERO);
        } else {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner() && super.canHitEntity(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) {
            discard();
        }
        return true;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
    }
    
    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        targetId = tag.getInt("TargetId");
        hookedAtTick = tag.getInt("HookedAtTick");
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("TargetId", targetId);
        tag.putInt("HookedAtTick", hookedAtTick);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void tickHookedTarget() {
        Entity owner = getOwner();
        Entity target = level().getEntity(targetId);
        if (!(owner instanceof net.minecraft.server.level.ServerPlayer sp) || !(target instanceof LivingEntity le) || !target.isAlive()) {
            discard();
            return;
        }

        le.addEffect(new MobEffectInstance(ModEffects.STUN.get(), 5, 0, false, true));
        setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());

        if (tickCount - hookedAtTick < PULL_DELAY_TICKS) {
            return;
        }

        Vec3 anchor = pullAnchor(sp);
        Vec3 toAnchor = anchor.subtract(target.position());
        double dist = toAnchor.length();
        if (dist < 1.35 || tickCount - hookedAtTick > MAX_PULL_TICKS) {
            discard();
            return;
        }

        Vec3 direction = toAnchor.normalize();
        Vec3 motion = direction.scale(Math.min(1.55, 0.45 + dist * 0.12));
        Vec3 step = direction.scale(Math.min(0.75, dist * 0.35));

        le.teleportTo(target.getX() + step.x, target.getY() + step.y, target.getZ() + step.z);
        le.setDeltaMovement(motion);
        le.hurtMarked = true;
        le.hasImpulse = true;
    }

    private static Vec3 pullAnchor(net.minecraft.server.level.ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
        if (horizontal.lengthSqr() < 0.001) {
            horizontal = new Vec3(-Math.sin(Math.toRadians(player.getYRot())), 0.0, Math.cos(Math.toRadians(player.getYRot())));
        }
        return player.position().add(horizontal.normalize().scale(1.6)).add(0.0, 0.05, 0.0);
    }
}
