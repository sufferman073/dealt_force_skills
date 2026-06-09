package com.rzy.dealt_force_skills.block;

import com.rzy.dealt_force_skills.registry.ModBlocks;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BladeWireBlockEntity extends BlockEntity {
    private static final int MAX_CORE_HEALTH = 120;
    private static final String OWNER = "Owner";
    private static final String CORE_POS = "CorePos";
    private static final String CORE = "Core";
    private static final String LINKED_WIRE_COUNT = "LinkedWireCount";
    private static final String CORE_HEALTH = "CoreHealth";

    private UUID ownerId;
    private BlockPos corePos;
    private boolean core;
    private int linkedWireCount;
    private int coreHealth = MAX_CORE_HEALTH;

    public BladeWireBlockEntity(BlockPos pos, BlockState state) {
        super(com.rzy.dealt_force_skills.registry.ModBlockEntities.BLADE_WIRE.get(), pos, state);
        this.core = state.getBlock() instanceof BladeWireBlock bladeWire && bladeWire.isCore();
        this.corePos = core ? pos : BlockPos.ZERO;
    }

    public void configureCore(UUID ownerId) {
        this.ownerId = ownerId;
        this.core = true;
        this.corePos = worldPosition;
        this.coreHealth = MAX_CORE_HEALTH;
        setChanged();
    }

    public void configureWire(UUID ownerId, BlockPos corePos) {
        this.ownerId = ownerId;
        this.core = false;
        this.corePos = corePos.immutable();
        this.coreHealth = 0;
        setChanged();
    }

    public void setLinkedWireCount(int linkedWireCount) {
        this.linkedWireCount = linkedWireCount;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BladeWireBlockEntity be) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!be.core && !isBladeWireCore(level, be.corePos)) {
            level.removeBlock(pos, false);
            return;
        }

        if (!be.core) {
            return;
        }

        if (level.getGameTime() % 20 != 0) {
            return;
        }

        int liveWires = be.countLinkedWires(level);
        be.linkedWireCount = liveWires;
        if (liveWires <= 0) {
            level.removeBlock(pos, false);
            return;
        }

        ServerPlayer owner = be.ownerId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(be.ownerId);
        AABB box = new AABB(pos).inflate(5.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && (owner == null || entity != owner))) {
            if (!be.isInsideWireArea(level, target.position())) {
                continue;
            }

            Vec3 beforeDamageMovement = target.getDeltaMovement();
            SkillDamageHelper.hurt(target, SkillDamageHelper.sinevaBladeWire(serverLevel, null, owner), owner, 4.0f);
            target.setDeltaMovement(beforeDamageMovement);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0, true, true));
            level.playSound(null, target.blockPosition(), ModSounds.WIRE_STEP.get(),
                    SoundSource.BLOCKS, 0.35f, 1.0f);
        }
    }

    public void onRemoved() {
        if (level == null || level.isClientSide) return;

        if (core) {
            removeLinkedWires(level);
        } else if (isBladeWireCore(level, corePos)
                && level.getBlockEntity(corePos) instanceof BladeWireBlockEntity coreEntity) {
            if (coreEntity.linkedWireCount <= 0) {
                return;
            }
            int next = Math.max(0, coreEntity.linkedWireCount - 1);
            coreEntity.setLinkedWireCount(next);
            if (next <= 0) {
                level.removeBlock(corePos, false);
            }
        }
    }

    public static void damageWireAt(Level level, BlockPos pos, int amount) {
        if (!(level.getBlockEntity(pos) instanceof BladeWireBlockEntity wire)) {
            return;
        }

        BladeWireBlockEntity coreEntity = wire.coreEntity();
        if (coreEntity == null) {
            level.removeBlock(pos, false);
            return;
        }

        coreEntity.coreHealth = Math.max(0, coreEntity.coreHealth - Math.max(1, amount));
        coreEntity.setChanged();
        if (coreEntity.coreHealth <= 0) {
            level.removeBlock(coreEntity.worldPosition, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerId != null) {
            tag.putUUID(OWNER, ownerId);
        }
        tag.putBoolean(CORE, core);
        tag.putInt(LINKED_WIRE_COUNT, linkedWireCount);
        tag.putInt(CORE_HEALTH, coreHealth);
        if (corePos != null) {
            CompoundTag coreTag = new CompoundTag();
            coreTag.putInt("X", corePos.getX());
            coreTag.putInt("Y", corePos.getY());
            coreTag.putInt("Z", corePos.getZ());
            tag.put(CORE_POS, coreTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ownerId = tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
        core = tag.getBoolean(CORE);
        linkedWireCount = tag.getInt(LINKED_WIRE_COUNT);
        coreHealth = tag.contains(CORE_HEALTH, net.minecraft.nbt.Tag.TAG_INT) ? tag.getInt(CORE_HEALTH) : MAX_CORE_HEALTH;
        if (tag.contains(CORE_POS, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag coreTag = tag.getCompound(CORE_POS);
            corePos = new BlockPos(coreTag.getInt("X"), coreTag.getInt("Y"), coreTag.getInt("Z"));
        } else {
            corePos = worldPosition;
        }
    }

    private boolean isInsideWireArea(Level level, Vec3 position) {
        if (position.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 1.4) {
            return true;
        }

        BlockPos entityPos = BlockPos.containing(position);
        for (BlockPos nearby : BlockPos.betweenClosed(entityPos.offset(-1, -1, -1), entityPos.offset(1, 1, 1))) {
            if (level.getBlockState(nearby).is(ModBlocks.BLADE_WIRE.get())
                    || level.getBlockState(nearby).is(ModBlocks.BLADE_WIRE_CORE.get())) {
                return true;
            }
        }
        return false;
    }

    private BladeWireBlockEntity coreEntity() {
        if (core) {
            return this;
        }
        if (level != null
                && isBladeWireCore(level, corePos)
                && level.getBlockEntity(corePos) instanceof BladeWireBlockEntity coreEntity
                && coreEntity.core) {
            return coreEntity;
        }
        return null;
    }

    private int countLinkedWires(Level level) {
        int count = 0;
        for (BlockPos check : BlockPos.betweenClosed(worldPosition.offset(-4, -2, -4), worldPosition.offset(4, 2, 4))) {
            if (level.getBlockState(check).is(ModBlocks.BLADE_WIRE.get())) {
                count++;
            }
        }
        return count;
    }

    private void removeLinkedWires(Level level) {
        linkedWireCount = 0;
        setChanged();
        for (BlockPos check : BlockPos.betweenClosed(worldPosition.offset(-4, -2, -4), worldPosition.offset(4, 2, 4))) {
            if (level.getBlockState(check).is(ModBlocks.BLADE_WIRE.get())) {
                level.removeBlock(check, false);
            }
        }
    }

    private static boolean isBladeWireCore(Level level, BlockPos pos) {
        return pos != null && level.getBlockState(pos).is(ModBlocks.BLADE_WIRE_CORE.get());
    }
}
