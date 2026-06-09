package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.character.uluru.UluruExplosionHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class UluruFireFieldEntity extends Entity implements ItemSupplier {
    private static final int LIFE_TICKS = 20 * 20;
    private static final int DAMAGE_INTERVAL_TICKS = 8;
    private static final double RADIUS = 6.0;
    private UUID ownerId;

    public UluruFireFieldEntity(EntityType<? extends UluruFireFieldEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public UluruFireFieldEntity(EntityType<? extends UluruFireFieldEntity> type, Level level, LivingEntity owner) {
        this(type, level);
        if (owner != null) {
            ownerId = owner.getUUID();
        }
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
        if (level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double x = getX() + (random.nextDouble() - 0.5) * RADIUS * 2.0;
                double z = getZ() + (random.nextDouble() - 0.5) * RADIUS * 2.0;
                level().addParticle(ParticleTypes.FLAME, x, getY() + 0.08, z, 0.0, 0.02, 0.0);
            }
            return;
        }

        if (tickCount % DAMAGE_INTERVAL_TICKS == 0 && level() instanceof ServerLevel serverLevel) {
            LivingEntity owner = ownerId == null ? null : findOwner(serverLevel);
            UluruExplosionHelper.damageRadiusIgnoringInvulnerability(serverLevel, position(), this, owner, RADIUS,
                    3.0f, 3.0f, false, true, 20, true);
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.1, getZ(),
                    48, RADIUS * 0.95, 0.25, RADIUS * 0.95, 0.01);
        }

        if (tickCount >= LIFE_TICKS) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private LivingEntity findOwner(ServerLevel level) {
        Entity entity = level.getEntity(ownerId);
        return entity instanceof LivingEntity living ? living : null;
    }
}
