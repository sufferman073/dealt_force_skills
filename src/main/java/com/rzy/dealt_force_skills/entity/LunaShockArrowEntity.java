package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import com.rzy.dealt_force_skills.util.RangedSoundHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LunaShockArrowEntity extends Projectile implements ItemSupplier {
    private static final int MAX_PULSE_TICKS = 5 * 20;
    private static final int PULSE_INTERVAL_TICKS = 10;
    private static final double PULSE_RADIUS = 4.0D;
    private static final Map<UUID, Integer> PULL_TICKS = new HashMap<>();

    private boolean bounceEnabled;
    private boolean bounced;
    private boolean stuck;
    private int pulseTicksRemaining = MAX_PULSE_TICKS;
    private UUID stuckEntityId;

    public LunaShockArrowEntity(EntityType<? extends LunaShockArrowEntity> type, Level level) {
        super(type, level);
    }

    public LunaShockArrowEntity(EntityType<? extends LunaShockArrowEntity> type, Level level, LivingEntity owner, boolean bounceEnabled) {
        super(type, level);
        setOwner(owner);
        this.bounceEnabled = bounceEnabled;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.SPECTRAL_ARROW);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (stuck) {
            tickStuck();
            spawnClientTrail();
            return;
        }

        Vec3 motion = getDeltaMovement();
        Vec3 start = position();
        Vec3 next = start.add(motion);
        HitResult blockHit = level().clip(new ClipContext(start, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            next = blockHit.getLocation();
        }

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level(), this, start, next,
                getBoundingBox().expandTowards(motion).inflate(0.45D), this::canHitEntity);
        if (entityHit != null) {
            hitEntity(entityHit);
            spawnClientTrail();
            return;
        }

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            hitBlock((BlockHitResult) blockHit);
            spawnClientTrail();
            return;
        }

        setPos(next.x, next.y, next.z);
        setDeltaMovement(motion.add(0.0D, -0.035D, 0.0D).scale(0.99D));
        if (tickCount > 160) {
            discard();
        }
        spawnClientTrail();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        bounceEnabled = tag.getBoolean("BounceEnabled");
        bounced = tag.getBoolean("Bounced");
        stuck = tag.getBoolean("Stuck");
        pulseTicksRemaining = tag.getInt("PulseTicksRemaining");
        if (pulseTicksRemaining <= 0) {
            pulseTicksRemaining = MAX_PULSE_TICKS;
        }
        if (tag.hasUUID("StuckEntity")) {
            stuckEntityId = tag.getUUID("StuckEntity");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("BounceEnabled", bounceEnabled);
        tag.putBoolean("Bounced", bounced);
        tag.putBoolean("Stuck", stuck);
        tag.putInt("PulseTicksRemaining", pulseTicksRemaining);
        if (stuckEntityId != null) {
            tag.putUUID("StuckEntity", stuckEntityId);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static void reportPullHold(ServerPlayer player, boolean holding) {
        if (!holding) {
            PULL_TICKS.remove(player.getUUID());
            return;
        }
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            PULL_TICKS.remove(player.getUUID());
            return;
        }

        AABB box = player.getBoundingBox().inflate(2.0D);
        var arrows = player.serverLevel().getEntitiesOfClass(LunaShockArrowEntity.class, box,
                arrow -> arrow.stuckEntityId != null && arrow.stuckEntityId.equals(player.getUUID()));
        if (arrows.isEmpty()) {
            PULL_TICKS.remove(player.getUUID());
            return;
        }

        int ticks = PULL_TICKS.merge(player.getUUID(), 1, Integer::sum);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.luna.pull_arrow", Math.min(2, (ticks + 19) / 20)), true);
        if (ticks >= 40) {
            for (LunaShockArrowEntity arrow : arrows) {
                arrow.discard();
            }
            PULL_TICKS.remove(player.getUUID());
            player.level().playSound(null, player.blockPosition(), ModSounds.LUNA_SHOCK_ARROW_PULSE.get(),
                    SoundSource.PLAYERS, 0.5f, 0.75f);
        }
    }

    private void hitEntity(EntityHitResult hit) {
        Entity entity = hit.getEntity();
        setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
        setDeltaMovement(Vec3.ZERO);
        stuck = true;
        if (entity instanceof LivingEntity living) {
            stuckEntityId = living.getUUID();
            if (level() instanceof ServerLevel serverLevel) {
                living.invulnerableTime = 0;
                SkillDamageHelper.hurt(living, SkillDamageHelper.lunaShockArrow(serverLevel, this, getOwner()), getOwner() instanceof LivingEntity owner ? owner : null, 10.0f);
                RangedSoundHelper.playThrottled(serverLevel, living.position(), ModSounds.LUNA_SHOCK_ARROW_PULSE.get(),
                        SoundSource.PLAYERS, 0.9f, 1.2f, 16.0D, 4, 3.0D);
            }
        }
    }

    private void hitBlock(BlockHitResult hit) {
        if (bounceEnabled && !bounced) {
            Direction direction = hit.getDirection();
            Vec3 reflected = reflect(getDeltaMovement(), direction);
            if (reflected.lengthSqr() > 0.05D) {
                bounced = true;
                Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
                setPos(hit.getLocation().x + normal.x * 0.05D, hit.getLocation().y + normal.y * 0.05D, hit.getLocation().z + normal.z * 0.05D);
                setDeltaMovement(reflected);
                return;
            }
        }

        setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
        setDeltaMovement(Vec3.ZERO);
        stuck = true;
    }

    private void tickStuck() {
        if (stuckEntityId != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(stuckEntityId);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                setPos(living.getX(), living.getY() + living.getBbHeight() * 0.62D, living.getZ());
            } else {
                discard();
                return;
            }
        }

        if (!level().isClientSide && pulseTicksRemaining-- > 0 && tickCount % PULSE_INTERVAL_TICKS == 0) {
            pulse();
        }
        if (!level().isClientSide && pulseTicksRemaining <= 0) {
            discard();
        }
    }

    private void pulse() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        Vec3 center = position();
        RangedSoundHelper.playThrottled(serverLevel, center, ModSounds.LUNA_SHOCK_ARROW_PULSE.get(),
                SoundSource.PLAYERS, 0.8f, 1.0f, 18.0D, 4, 3.0D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z,
                18, 0.55D, 0.35D, 0.55D, 0.03D);
        AABB box = new AABB(center, center).inflate(PULSE_RADIUS);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && entity != owner && entity.distanceToSqr(center) <= PULSE_RADIUS * PULSE_RADIUS)) {
            if (!hasLineOfSight(center, target)) {
                continue;
            }
            target.invulnerableTime = 0;
            SkillDamageHelper.hurt(target, SkillDamageHelper.lunaShockArrow(serverLevel, this, owner), owner, 4.0f);
        }
    }

    private boolean hasLineOfSight(Vec3 center, LivingEntity target) {
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        HitResult result = level().clip(new ClipContext(center, targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(targetCenter) < 0.25D;
    }

    private Vec3 reflect(Vec3 motion, Direction direction) {
        return switch (direction.getAxis()) {
            case X -> new Vec3(-motion.x * 0.65D, motion.y * 0.85D, motion.z * 0.65D);
            case Y -> new Vec3(motion.x * 0.65D, -motion.y * 0.55D, motion.z * 0.65D);
            case Z -> new Vec3(motion.x * 0.65D, motion.y * 0.85D, -motion.z * 0.65D);
        };
    }

    private void spawnClientTrail() {
        if (level().isClientSide && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}
