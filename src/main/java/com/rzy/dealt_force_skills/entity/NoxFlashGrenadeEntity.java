package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import com.rzy.dealt_force_skills.util.RangedSoundHelper;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class NoxFlashGrenadeEntity extends Projectile implements ItemSupplier {
    private static final int DEFAULT_FUSE_TICKS = 4 * 20;
    private static final int BOUNCE_FUSE_TICKS = 12;
    private static final double BOUNCE_FACTOR = 0.66D;
    private static final double RADIUS = 24.0D;
    private static final int MAX_FLASH_TICKS = 15 * 20;
    private static final int MIN_FLASH_TICKS = 2;
    private static final double MAX_FLASH_ANGLE = 90.0D;

    private UUID ownerId;
    private int fuseRemaining = DEFAULT_FUSE_TICKS;
    private boolean bounced;

    public NoxFlashGrenadeEntity(EntityType<? extends NoxFlashGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public NoxFlashGrenadeEntity(EntityType<? extends NoxFlashGrenadeEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
        ownerId = owner.getUUID();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.GLOWSTONE_DUST);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && --fuseRemaining <= 0) {
            explode(position());
            return;
        }

        Vec3 motion = getDeltaMovement();
        Vec3 next = position().add(motion);
        HitResult hit = level().clip(new ClipContext(position(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            handleBlockHit((BlockHitResult) hit);
            spawnClientTrail();
            return;
        }

        setPos(next.x, next.y, next.z);
        setDeltaMovement(motion.add(0.0D, -0.045D, 0.0D).scale(0.985D));
        checkInsideBlocks();
        spawnClientTrail();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        fuseRemaining = tag.getInt("FuseRemaining");
        if (fuseRemaining <= 0) {
            fuseRemaining = DEFAULT_FUSE_TICKS;
        }
        bounced = tag.getBoolean("Bounced");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("FuseRemaining", fuseRemaining);
        tag.putBoolean("Bounced", bounced);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void handleBlockHit(BlockHitResult hit) {
        Direction direction = hit.getDirection();
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
        setPos(hit.getLocation().x + normal.x * 0.04D,
                hit.getLocation().y + normal.y * 0.04D,
                hit.getLocation().z + normal.z * 0.04D);
        setDeltaMovement(bounce(direction, getDeltaMovement()));

        if (!bounced && level() instanceof ServerLevel level) {
            RangedSoundHelper.playThrottled(level, hit.getLocation(), ModSounds.NOX_FLASH_BOUNCE.get(),
                    SoundSource.PLAYERS, 0.55f, 1.0f, 14.0D, 6, 3.0D);
        }
        bounced = true;
        if (!level().isClientSide && fuseRemaining > BOUNCE_FUSE_TICKS) {
            fuseRemaining = BOUNCE_FUSE_TICKS;
        }
    }

    private Vec3 bounce(Direction direction, Vec3 motion) {
        return switch (direction.getAxis()) {
            case X -> new Vec3(-motion.x * BOUNCE_FACTOR, motion.y * 0.80D, motion.z * BOUNCE_FACTOR);
            case Y -> new Vec3(motion.x * BOUNCE_FACTOR, -motion.y * 0.45D, motion.z * BOUNCE_FACTOR);
            case Z -> new Vec3(motion.x * BOUNCE_FACTOR, motion.y * 0.80D, -motion.z * BOUNCE_FACTOR);
        };
    }

    private void explode(Vec3 center) {
        if (!(level() instanceof ServerLevel level)) {
            discard();
            return;
        }

        LivingEntity owner = owner(level);
        RangedSoundHelper.playThrottled(level, center, ModSounds.NOX_FLASH_EXPLODE.get(),
                SoundSource.PLAYERS, 1.2f, 1.0f, 30.0D, 3, 4.0D);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 0.2D, center.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.2D, center.z,
                80, 1.8D, 0.7D, 1.8D, 0.05D);

        AABB box = new AABB(center, center).inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (!TargetingUtil.isTargetableLiving(target)) {
                continue;
            }
            int flashTicks = flashDuration(center, target);
            if (flashTicks <= 0) {
                continue;
            }

            Vec3 before = target.getDeltaMovement();
            target.invulnerableTime = 0;
            SkillDamageHelper.hurtUnscaled(target, SkillDamageHelper.noxFlash(level, this, owner), 1.0f);
            target.setDeltaMovement(before);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(ModEffects.NOX_FLASHED.get(),
                    flashTicks, 0, false, false, true), owner);
            target.level().playSound(null, target.blockPosition(), ModSounds.NOX_FLASH_HIT.get(),
                    SoundSource.PLAYERS, 0.7f, 1.0f);
        }
        discard();
    }

    private int flashDuration(Vec3 center, LivingEntity target) {
        Vec3 flashPos = center.add(0.0D, 0.65D, 0.0D);
        Vec3 eyes = target.getEyePosition(1.0F);
        Vec3 toFlash = flashPos.subtract(eyes);
        double distance = toFlash.length();
        if (distance > RADIUS || distance < 0.001D) {
            return 0;
        }
        if (!hasLineOfSight(eyes, flashPos)) {
            return 0;
        }

        Vec3 view = target.getViewVector(1.0F).normalize();
        double dot = Mth.clamp(view.dot(toFlash.normalize()), -1.0D, 1.0D);
        double angle = Math.toDegrees(Math.acos(dot));
        if (angle >= MAX_FLASH_ANGLE) {
            return MIN_FLASH_TICKS;
        }

        double angleFactor = 1.0D - angle / MAX_FLASH_ANGLE;
        return Math.max(MIN_FLASH_TICKS,
                (int) Math.round(MIN_FLASH_TICKS + (MAX_FLASH_TICKS - MIN_FLASH_TICKS) * angleFactor));
    }

    private boolean hasLineOfSight(Vec3 start, Vec3 end) {
        HitResult result = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(end) < 0.35D;
    }

    private LivingEntity owner(ServerLevel level) {
        if (getOwner() instanceof LivingEntity living) {
            return living;
        }
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        return owner instanceof LivingEntity living ? living : null;
    }

    private void spawnClientTrail() {
        if (level().isClientSide && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY() + 0.05D, getZ(), 0.0D, 0.01D, 0.0D);
        }
    }
}
