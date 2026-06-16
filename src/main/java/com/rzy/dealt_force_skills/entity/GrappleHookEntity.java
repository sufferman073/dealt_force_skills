package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.character.sineva.SinevaStateManager;
import com.rzy.dealt_force_skills.effect.StunEffect;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
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
    private static final EntityDataAccessor<Integer> OWNER_ENTITY_ID =
            SynchedEntityData.defineId(GrappleHookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RETURNING =
            SynchedEntityData.defineId(GrappleHookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double MAX_DISTANCE_SQR = 35.0 * 35.0;
    private static final int PULL_DELAY_TICKS = 8;
    private static final int MAX_PULL_TICKS = 48;
    private static final int MAX_RETURN_TICKS = 60;
    private static final double RETURN_SPEED_PER_TICK = 1.8D;
    private static final double RETURN_FINISH_DISTANCE = 0.55D;

    private int targetId = -1;
    private int hookedAtTick = -1;
    private int returnStartedTick = -1;

    public GrappleHookEntity(EntityType<? extends GrappleHookEntity> type, Level level) {
        super(type, level);
    }

    public GrappleHookEntity(EntityType<? extends GrappleHookEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        this.setOwner(owner);
        this.entityData.set(OWNER_ENTITY_ID, owner.getId());
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.TRIPWIRE_HOOK);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ENTITY_ID, -1);
        this.entityData.define(RETURNING, false);
    }

    public Entity getRopeOwner() {
        Entity owner = getOwner();
        if (owner != null) {
            return owner;
        }
        int ownerId = this.entityData.get(OWNER_ENTITY_ID);
        return ownerId < 0 ? null : level().getEntity(ownerId);
    }

    public boolean isReturning() {
        return this.entityData.get(RETURNING);
    }

    public static Vec3 ropeOrigin(Entity owner, float partialTick) {
        Vec3 look = owner.getViewVector(partialTick);
        if (look.lengthSqr() < 0.000001D) {
            double yaw = Math.toRadians(owner.getYRot());
            look = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        }
        look = look.normalize();
        Vec3 left = new Vec3(look.z, 0.0D, -look.x);
        if (left.lengthSqr() > 0.000001D) {
            left = left.normalize();
        }
        return owner.getEyePosition(partialTick)
                .add(look.scale(0.58D))
                .add(left.scale(0.34D))
                .add(0.0D, -0.32D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (isReturning()) {
            tickReturning();
            return;
        }

        if (targetId == -1) {
            Entity owner = getOwner();
            if (owner == null) {
                discard();
                return;
            }
            if (distanceToSqr(owner) > MAX_DISTANCE_SQR) {
                beginReturn();
                return;
            }

            Vec3 motion = getDeltaMovement();
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() == HitResult.Type.ENTITY) {
                onHitEntity((EntityHitResult) hit);
                return;
            }
            if (hit.getType() == HitResult.Type.BLOCK) {
                setPos(hit.getLocation());
                beginReturn();
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
            level().playSound(null, sp.blockPosition(), ModSounds.SINEVA_GRAPPLE_HIT_CASTER.get(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            SkillDamageHelper.hurt(le, SkillDamageHelper.sinevaGrapple(sp.serverLevel(), this, sp), sp, 10.0f);
            targetId = target.getId();
            hookedAtTick = tickCount;
            setDeltaMovement(Vec3.ZERO);
        } else {
            beginReturn();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner() && super.canHitEntity(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) {
            beginReturn();
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
        returnStartedTick = tag.getInt("ReturnStartedTick");
        this.entityData.set(RETURNING, tag.getBoolean("Returning"));
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("TargetId", targetId);
        tag.putInt("HookedAtTick", hookedAtTick);
        tag.putInt("ReturnStartedTick", returnStartedTick);
        tag.putBoolean("Returning", isReturning());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void tickHookedTarget() {
        Entity owner = getOwner();
        Entity target = level().getEntity(targetId);
        if (!(owner instanceof net.minecraft.server.level.ServerPlayer sp) || !(target instanceof LivingEntity le) || !target.isAlive()) {
            beginReturn();
            return;
        }

        le.addEffect(new MobEffectInstance(ModEffects.STUN.get(), 5, 0, false, true));
        StunEffect.allowHorizontalMovement(le, 3);
        setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());

        if (tickCount - hookedAtTick < PULL_DELAY_TICKS) {
            return;
        }

        Vec3 anchor = pullAnchor(sp);
        Vec3 toAnchor = anchor.subtract(target.position());
        Vec3 horizontalToAnchor = new Vec3(toAnchor.x, 0.0D, toAnchor.z);
        double horizontalDistance = horizontalToAnchor.length();
        if (horizontalDistance < 1.35D || tickCount - hookedAtTick > MAX_PULL_TICKS) {
            beginReturn();
            return;
        }

        Vec3 desiredHorizontal = horizontalToAnchor.normalize()
                .scale(Math.min(0.95D, 0.22D + horizontalDistance * 0.055D));
        le.move(MoverType.SELF, desiredHorizontal);
        le.setDeltaMovement(Vec3.ZERO);
        le.hurtMarked = true;
        le.hasImpulse = true;
    }

    private void beginReturn() {
        if (isReturning()) {
            return;
        }
        targetId = -1;
        hookedAtTick = -1;
        returnStartedTick = tickCount;
        this.entityData.set(RETURNING, true);
        setDeltaMovement(Vec3.ZERO);
    }

    private void tickReturning() {
        Entity owner = getRopeOwner();
        if (owner == null || !owner.isAlive() || tickCount - returnStartedTick > MAX_RETURN_TICKS) {
            discard();
            return;
        }

        Vec3 destination = ropeOrigin(owner, 1.0F);
        Vec3 offset = destination.subtract(position());
        double distance = offset.length();
        if (distance <= RETURN_FINISH_DISTANCE) {
            discard();
            return;
        }

        Vec3 motion = offset.scale(Math.min(RETURN_SPEED_PER_TICK, distance) / distance);
        setDeltaMovement(motion);
        setPos(position().add(motion));
        checkInsideBlocks();
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
