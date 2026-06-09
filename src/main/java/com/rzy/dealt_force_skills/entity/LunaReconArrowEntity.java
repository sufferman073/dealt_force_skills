package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.character.luna.LunaStateManager;
import com.rzy.dealt_force_skills.registry.ModSounds;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LunaReconArrowEntity extends Projectile implements ItemSupplier {
    private static final int MAX_BOUNCES = 8;
    private static final int MAX_LIFE_TICKS = 20 * 20;
    private static final int MOVE_SCAN_DELAY_TICKS = 2 * 20;
    private static final double SCAN_RADIUS = 40.0D;
    private static final double MOVE_THRESHOLD_SQR = 0.025D;

    private int bounces;
    private final Set<UUID> scanned = new HashSet<>();
    private final Map<UUID, PendingScan> pendingMoveScans = new HashMap<>();

    public LunaReconArrowEntity(EntityType<? extends LunaReconArrowEntity> type, Level level) {
        super(type, level);
    }

    public LunaReconArrowEntity(EntityType<? extends LunaReconArrowEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            scanNearby();
            tickMoveScans();
            if (tickCount % 8 == 0 && level() instanceof ServerLevel serverLevel) {
                RangedSoundHelper.playThrottled(serverLevel, position(), ModSounds.LUNA_RECON_ARROW_FLY.get(),
                        SoundSource.PLAYERS, 0.45f, 1.1f, 16.0D, 8, 3.0D);
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
        setDeltaMovement(motion.add(0.0D, -0.025D, 0.0D).scale(0.995D));
        if (tickCount > MAX_LIFE_TICKS) {
            discard();
        }
        spawnClientTrail();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        bounces = tag.getInt("Bounces");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Bounces", bounces);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void hitBlock(BlockHitResult hit) {
        if (bounces >= MAX_BOUNCES) {
            discard();
            return;
        }

        bounces++;
        Direction direction = hit.getDirection();
        Vec3 reflected = reflect(getDeltaMovement(), direction);
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
        setPos(hit.getLocation().x + normal.x * 0.05D,
                hit.getLocation().y + normal.y * 0.05D,
                hit.getLocation().z + normal.z * 0.05D);
        setDeltaMovement(reflected);
    }

    private Vec3 reflect(Vec3 motion, Direction direction) {
        return switch (direction.getAxis()) {
            case X -> new Vec3(-motion.x * 0.9D, motion.y * 0.95D, motion.z * 0.9D);
            case Y -> new Vec3(motion.x * 0.9D, -motion.y * 0.75D, motion.z * 0.9D);
            case Z -> new Vec3(motion.x * 0.9D, motion.y * 0.95D, -motion.z * 0.9D);
        };
    }

    private void scanNearby() {
        if (!(getOwner() instanceof ServerPlayer owner) || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 center = position();
        AABB box = new AABB(center, center).inflate(SCAN_RADIUS);
        boolean foundPlayer = false;
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && entity != owner && entity.distanceToSqr(center) <= SCAN_RADIUS * SCAN_RADIUS)) {
            if (scanned.contains(target.getUUID()) || !hasLineOfSight(center, target)) {
                continue;
            }
            scanned.add(target.getUUID());
            pendingMoveScans.put(target.getUUID(), new PendingScan(target.position(), MOVE_SCAN_DELAY_TICKS));
            LunaStateManager.revealToOwner(owner, target, LunaStateManager.RECON_REVEAL_TICKS, target instanceof ServerPlayer);
            if (target instanceof ServerPlayer) {
                foundPlayer = true;
            }
        }
        if (foundPlayer) {
            owner.displayClientMessage(Component.translatable("message.dealt_force_skills.luna.recon_player_scanned"), true);
        }
        if (!pendingMoveScans.isEmpty() && tickCount % 20 == 0) {
            owner.displayClientMessage(Component.translatable("message.dealt_force_skills.luna.recon_moving_scan"), true);
        }
    }

    private void tickMoveScans() {
        if (!(getOwner() instanceof ServerPlayer owner) || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        var iterator = pendingMoveScans.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingScan> entry = iterator.next();
            PendingScan pending = entry.getValue();
            pending.remainingTicks--;
            if (pending.remainingTicks > 0) {
                continue;
            }

            Entity entity = serverLevel.getEntity(entry.getKey());
            if (entity instanceof LivingEntity target && target.isAlive()
                    && target.position().distanceToSqr(pending.position) >= MOVE_THRESHOLD_SQR
                    && hasLineOfSight(position(), target)) {
                LunaStateManager.revealToOwner(owner, target, LunaStateManager.RECON_REVEAL_TICKS, target instanceof ServerPlayer);
            }
            iterator.remove();
        }
    }

    private boolean hasLineOfSight(Vec3 center, LivingEntity target) {
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        HitResult result = level().clip(new ClipContext(center, targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(targetCenter) < 0.25D;
    }

    private void spawnClientTrail() {
        if (level().isClientSide && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private static final class PendingScan {
        private final Vec3 position;
        private int remainingTicks;

        private PendingScan(Vec3 position, int remainingTicks) {
            this.position = position;
            this.remainingTicks = remainingTicks;
        }
    }
}
