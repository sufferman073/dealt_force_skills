package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.character.sineva.SinevaKnockdownState;
import com.rzy.dealt_force_skills.character.sineva.SinevaStateManager;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.util.RangedSoundHelper;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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

public class VyronTigerCannonEntity extends Projectile implements ItemSupplier {
    private static final int FUSE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.vyrontigercannonentity.fuse_ticks", 5 * 20);
    private static final int KNOCKDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.vyrontigercannonentity.knockdown_ticks", 5 * 20);
    private static final double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.vyrontigercannonentity.radius", 4.0D);
    private static final int MAX_FLOOR_BOUNCES = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.vyron_tiger_cannon_entity.max_floor_bounces", 2);

    private int fuseRemaining = FUSE_TICKS;
    private int floorBounces;

    public VyronTigerCannonEntity(EntityType<? extends VyronTigerCannonEntity> type, Level level) {
        super(type, level);
    }

    public VyronTigerCannonEntity(EntityType<? extends VyronTigerCannonEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            warnPlayers();
            if (--fuseRemaining <= 0) {
                explode();
                return;
            }
        }

        Vec3 motion = getDeltaMovement();
        Vec3 next = position().add(motion);
        HitResult hit = level().clip(new ClipContext(position(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            hitBlock((BlockHitResult) hit);
            spawnClientTrail();
            return;
        }

        setPos(next.x, next.y, next.z);
        setDeltaMovement(motion.add(0.0D, -0.045D, 0.0D).scale(0.992D));
        spawnClientTrail();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        fuseRemaining = tag.getInt("FuseRemaining");
        if (fuseRemaining <= 0) {
            fuseRemaining = FUSE_TICKS;
        }
        floorBounces = tag.getInt("FloorBounces");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("FuseRemaining", fuseRemaining);
        tag.putInt("FloorBounces", floorBounces);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void hitBlock(BlockHitResult hit) {
        Direction direction = hit.getDirection();
        Vec3 motion = getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
        setPos(hit.getLocation().x + normal.x * 0.05D,
                hit.getLocation().y + normal.y * 0.05D,
                hit.getLocation().z + normal.z * 0.05D);

        if (direction == Direction.UP) {
            floorBounces++;
            if (floorBounces > MAX_FLOOR_BOUNCES) {
                setDeltaMovement(new Vec3(motion.x * 0.45D, 0.0D, motion.z * 0.45D));
                return;
            }
        }

        setDeltaMovement(bounce(direction, motion));
        if (level() instanceof ServerLevel serverLevel) {
            RangedSoundHelper.playThrottled(serverLevel, position(), ModSounds.VYRON_TIGER_CANNON_BOUNCE.get(),
                    SoundSource.PLAYERS, 0.7f, 0.9f + random.nextFloat() * 0.2f, 14.0D, 6, 3.0D);
        }
    }

    private Vec3 bounce(Direction direction, Vec3 motion) {
        return switch (direction.getAxis()) {
            case X -> new Vec3(-motion.x * 0.72D, motion.y * 0.85D, motion.z * 0.72D);
            case Y -> new Vec3(motion.x * 0.72D, -motion.y * 0.46D, motion.z * 0.72D);
            case Z -> new Vec3(motion.x * 0.72D, motion.y * 0.85D, -motion.z * 0.72D);
        };
    }

    private void warnPlayers() {
        if (fuseRemaining % 10 != 0 || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = position();
        AABB box = new AABB(center, center).inflate(RADIUS);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, box,
                target -> TargetingUtil.isTargetablePlayer(target) && target.distanceToSqr(center) <= RADIUS * RADIUS)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.tiger_warning"), true);
        }
    }

    private void explode() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        Vec3 center = position();
        RangedSoundHelper.playThrottled(serverLevel, center, ModSounds.VYRON_TIGER_CANNON_EXPLODE.get(),
                SoundSource.PLAYERS, 1.15f, 1.0f, 22.0D, 3, 4.0D);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.15D, center.z,
                8, 0.45D, 0.25D, 0.45D, 0.02D);

        boolean hitAny = false;
        AABB box = new AABB(center, center).inflate(RADIUS);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                entity -> TargetingUtil.isTargetableLiving(entity) && entity.distanceToSqr(center) <= RADIUS * RADIUS)) {
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (!hasLineOfSight(center, targetCenter) || isBlockedBySinevaShield(target, center)) {
                continue;
            }
            target.addEffect(new MobEffectInstance(ModEffects.STUN.get(), KNOCKDOWN_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.vyron_tiger_cannon_entity.effect.stun.0.amplifier", 0), false, true, true));
            if (owner instanceof ServerPlayer attacker) {
                SinevaKnockdownState.apply(attacker, target, KNOCKDOWN_TICKS);
            }
            hitAny = true;
        }
        if (hitAny && owner instanceof ServerPlayer attacker) {
            RangedSoundHelper.playThrottled(serverLevel, attacker.position(), ModSounds.VYRON_TIGER_CANNON_HIT_FEEDBACK.get(),
                    SoundSource.PLAYERS, 0.85f, 1.0f, 8.0D, 4, 2.0D);
        }
        discard();
    }

    private boolean isBlockedBySinevaShield(LivingEntity target, Vec3 source) {
        if (!(target instanceof Player player) || !SinevaStateManager.isShieldDeployed(player)) {
            return false;
        }
        Vec3 toSource = source.subtract(player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D));
        if (toSource.lengthSqr() < 0.0001D) {
            return true;
        }
        return player.getLookAngle().normalize().dot(toSource.normalize()) > 0.0D;
    }

    private boolean hasLineOfSight(Vec3 center, Vec3 targetCenter) {
        HitResult result = level().clip(new ClipContext(center, targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(targetCenter) < 0.25D;
    }

    private void spawnClientTrail() {
        if (level().isClientSide && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}
