package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import com.rzy.dealt_force_skills.character.toxik.ToxikStateManager;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModParticles;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.UUID;

public class ToxikTearGasCloudEntity extends Entity implements ItemSupplier {
    public static volatile int LIFE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("LIFE_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.toxikteargascloudentity.life_ticks", 400));
    public static volatile double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("RADIUS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.toxikteargascloudentity.radius", 4.8));
    private static final int PARTICLE_REFRESH_TICKS = 15;
    private static volatile int BASE_BLIND_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("BASE_BLIND_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.toxikteargascloudentity.base_blind_ticks", 60));
    private UUID ownerId;
    private int lifeTicks = LIFE_TICKS;

    public ToxikTearGasCloudEntity(EntityType<? extends ToxikTearGasCloudEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public ToxikTearGasCloudEntity(EntityType<? extends ToxikTearGasCloudEntity> type, Level level, UUID ownerId, int lifeTicks) {
        this(type, level);
        this.ownerId = ownerId;
        this.lifeTicks = lifeTicks;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.CYAN_STAINED_GLASS);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnClientParticles();
            return;
        }
        if (level() instanceof ServerLevel serverLevel) {
            applyBlind(serverLevel);
            suppressTargetsThroughSmoke(serverLevel);
        }
        if (tickCount >= lifeTicks) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        lifeTicks = tag.getInt("LifeTicks");
        if (lifeTicks <= 0) {
            lifeTicks = LIFE_TICKS;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("LifeTicks", lifeTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void applyBlind(ServerLevel level) {
        if (tickCount % 10 != 0) {
            return;
        }
        LivingEntity owner = owner(level);
        AABB box = new AABB(position(), position()).inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (!TargetingUtil.isHostileLivingFor(owner, target)) {
                continue;
            }
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (center.distanceTo(position()) > RADIUS) {
                continue;
            }
            if (owner instanceof ServerPlayer toxik && ToxikStateManager.isToxik(toxik)) {
                ToxikStateManager.addToxikScaledEffect(toxik, target,
                        ModEffects.TOXIK_TEAR_GAS_BLIND.get(), BASE_BLIND_TICKS, 0);
            } else {
                target.addEffect(new MobEffectInstance(ModEffects.TOXIK_TEAR_GAS_BLIND.get(),
                        BASE_BLIND_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.toxik_tear_gas_cloud_entity.effect.toxik_tear_gas_blind.0.amplifier", 0), false, true, true), owner);
            }
        }
    }

    private void suppressTargetsThroughSmoke(ServerLevel level) {
        if (ownerId == null || !(level.getEntity(ownerId) instanceof LivingEntity owner) || !owner.isAlive()) {
            return;
        }
        AABB box = new AABB(position(), position()).inflate(40.0D);
        boolean ownerInside = owner.position().distanceTo(position()) <= RADIUS;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, mob -> mob.isAlive() && mob.getTarget() == owner)) {
            boolean mobInside = mob.position().distanceTo(position()) <= RADIUS;
            if (ownerInside && mobInside) {
                continue;
            }
            if (segmentIntersectsSmoke(mob.getEyePosition(), owner.getEyePosition())) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
        }
    }

    private boolean segmentIntersectsSmoke(Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr < 0.0001D) {
            return start.distanceTo(position()) <= RADIUS;
        }
        double t = position().subtract(start).dot(segment) / lengthSqr;
        t = Math.max(0.0D, Math.min(1.0D, t));
        Vec3 closest = start.add(segment.scale(t));
        return closest.distanceTo(position()) <= RADIUS;
    }

    private LivingEntity owner(ServerLevel level) {
        return ownerId == null || !(level.getEntity(ownerId) instanceof LivingEntity owner) ? null : owner;
    }

    private void spawnClientParticles() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            return;
        }
        if (tickCount > 1 && tickCount % PARTICLE_REFRESH_TICKS != 0) {
            return;
        }
        level().addParticle(ModParticles.TOXIK_LARGE_SMOKE.get(),
                getX(), getY() + 1.4D, getZ(), 0.0D, 0.0D, 0.0D);
    }
}
