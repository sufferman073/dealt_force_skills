package com.rzy.dealt_force_skills.character.undead;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.shop.UndeadShopEntry;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UndeadSupportManager {
    public static final String SUMMON_OWNER = DealtForceSkillsMod.MODID + ".undead_summon_owner";
    public static final String SKELETON_OWNER = SUMMON_OWNER;

    private static final String SUPPORTS = "Supports";
    private static final String TYPE = "Type";
    private static final String DIMENSION = "Dimension";
    private static final String ORIGIN = "Origin";
    private static final String BLOCKS = "Blocks";
    private static final String SIGN = "Sign";
    private static final String NEXT_SPAWN = "NextSpawn";
    private static final String SPAWNED = "Spawned";

    private UndeadSupportManager() {
    }

    public static boolean deploy(ServerPlayer player, UndeadShopEntry entry) {
        ServerLevel level = player.serverLevel();
        if (!entry.isSupport()) {
            return false;
        }
        BlockPos origin = findOrigin(player, level);
        if (origin == null) {
            return false;
        }
        return switch (entry) {
            case WARM_CAMPFIRE -> deployCampfire(player, level, origin);
            case ATTACK_FLAG -> deployFlag(player, level, origin);
            case STAIRS -> deployStairs(player, level, origin);
            case REST_TOMBSTONE -> deployTombstone(player, level, origin);
            default -> false;
        };
    }

    public static void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (player.tickCount % 20 != 0) {
            return;
        }
        ListTag supports = supports(player);
        ListTag retained = new ListTag();
        for (int i = 0; i < supports.size(); i++) {
            CompoundTag support = supports.getCompound(i);
            if (!dimension(level).equals(support.getString(DIMENSION))) {
                retained.add(support.copy());
                continue;
            }
            BlockPos origin = BlockPos.of(support.getLong(ORIGIN));
            String type = support.getString(TYPE);
            if ("campfire".equals(type)) {
                if (level.getBlockState(origin).is(Blocks.CAMPFIRE)) {
                    if (distanceSqr(player, origin, 0.5D) <= 9.0D) {
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.undead.undead_support_manager.effect.regeneration.0.duration_ticks", 30), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.undead.undead_support_manager.effect.regeneration.0.amplifier", 1),
                                false, false, true));
                    }
                    retained.add(support.copy());
                }
            } else if ("flag".equals(type)) {
                BlockPos banner = BlockPos.of(support.getLong(SIGN));
                if (level.getBlockState(banner).is(Blocks.RED_BANNER)) {
                    if (distanceSqr(player, origin, 0.5D) <= 16.0D) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.undead.undead_support_manager.effect.damage_boost.1.duration_ticks", 11 * 20), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.undead.undead_support_manager.effect.damage_boost.1.amplifier", 1),
                                false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.undead.undead_support_manager.effect.movement_speed.2.duration_ticks", 11 * 20), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.undead.undead_support_manager.effect.movement_speed.2.amplifier", 1),
                                false, false, true));
                    }
                    retained.add(support.copy());
                }
            } else if ("stairs".equals(type)) {
                if (hasAnyPlacedBlock(level, support)) {
                    retained.add(support.copy());
                }
            } else if ("tombstone".equals(type)) {
                BlockPos sign = BlockPos.of(support.getLong(SIGN));
                if (!level.getBlockState(sign).is(Blocks.OAK_WALL_SIGN)) {
                    removeTombstone(level, support);
                    continue;
                }
                tickTombstoneSkeletons(player, level, support, origin);
                retained.add(support.copy());
            }
        }
        UndeadStateManager.rootData(player).put(SUPPORTS, retained);
        clearSkeletonOwnerTargets(player, level);
    }

    public static boolean isTombstoneHidden(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ListTag supports = supports(player);
        for (int i = 0; i < supports.size(); i++) {
            CompoundTag support = supports.getCompound(i);
            if (!"tombstone".equals(support.getString(TYPE))
                    || !dimension(level).equals(support.getString(DIMENSION))) {
                continue;
            }
            BlockPos sign = BlockPos.of(support.getLong(SIGN));
            BlockPos origin = BlockPos.of(support.getLong(ORIGIN));
            if (level.getBlockState(sign).is(Blocks.OAK_WALL_SIGN)
                    && distanceSqr(player, origin, 1.5D) <= 36.0D) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOwnedSkeleton(LivingEntity entity, UUID owner) {
        return entity instanceof Skeleton
                && owner.equals(summonOwner(entity));
    }

    public static UUID summonOwner(Entity entity) {
        return entity != null && entity.getPersistentData().hasUUID(SUMMON_OWNER)
                ? entity.getPersistentData().getUUID(SUMMON_OWNER)
                : null;
    }

    public static boolean shouldCancelFriendlyFire(LivingEntity target, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        UUID targetOwner = summonOwner(target);
        UUID attackerOwner = summonOwner(attacker);
        return targetOwner != null && (targetOwner.equals(attacker.getUUID())
                || targetOwner.equals(attackerOwner))
                || attackerOwner != null && attackerOwner.equals(target.getUUID());
    }

    public static void tickOwnedSummon(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        UUID ownerId = summonOwner(entity);
        if (ownerId == null) {
            return;
        }
        Entity ownerEntity = level.getEntity(ownerId);
        if (!(ownerEntity instanceof ServerPlayer owner) || !owner.isAlive()) {
            mob.setTarget(null);
            return;
        }

        LivingEntity current = mob.getTarget();
        if (current != null && isAlliedWithOwner(current, ownerId)) {
            mob.setTarget(null);
            current = null;
        }
        LivingEntity commanded = owner.getLastHurtMob();
        if (!isValidCommandTarget(commanded, ownerId)) {
            commanded = owner.getLastHurtByMob();
        }
        if (isValidCommandTarget(commanded, ownerId) && commanded != current) {
            mob.setTarget(commanded);
        }
    }

    private static boolean isValidCommandTarget(LivingEntity target, UUID ownerId) {
        return target != null
                && target.isAlive()
                && !isAlliedWithOwner(target, ownerId)
                && TargetingUtil.isTargetableLiving(target);
    }

    private static boolean isAlliedWithOwner(Entity entity, UUID ownerId) {
        UUID entityOwner = summonOwner(entity);
        return ownerId.equals(entity.getUUID()) || ownerId.equals(entityOwner);
    }

    private static boolean deployCampfire(ServerPlayer player, ServerLevel level, BlockPos origin) {
        if (!canReplace(level, List.of(origin))) {
            return false;
        }
        level.setBlock(origin, Blocks.CAMPFIRE.defaultBlockState(), Block.UPDATE_ALL);
        addSupport(player, "campfire", level, origin, List.of(origin), origin);
        return true;
    }

    private static boolean deployFlag(ServerPlayer player, ServerLevel level, BlockPos origin) {
        BlockPos banner = origin.above();
        List<BlockPos> blocks = List.of(origin, banner);
        if (!canReplace(level, blocks)) {
            return false;
        }
        level.setBlock(origin, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(banner, Blocks.RED_BANNER.defaultBlockState(), Block.UPDATE_ALL);
        addSupport(player, "flag", level, origin, blocks, banner);
        return true;
    }

    private static boolean deployStairs(ServerPlayer player, ServerLevel level, BlockPos origin) {
        Direction facing = player.getDirection().getOpposite();
        Direction side = facing.getClockWise();
        List<BlockPos> positions = new ArrayList<>();
        for (int width = -1; width <= 1; width++) {
            for (int height = 0; height < 3; height++) {
                positions.add(origin.relative(side, width).above(height));
            }
        }
        if (!canReplace(level, positions)) {
            return false;
        }
        BlockState state = Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(StairBlock.HALF, Half.BOTTOM);
        positions.forEach(pos -> level.setBlock(pos, state, Block.UPDATE_ALL));
        addSupport(player, "stairs", level, origin, positions, origin);
        return true;
    }

    private static boolean deployTombstone(ServerPlayer player, ServerLevel level, BlockPos origin) {
        Direction outward = player.getDirection();
        List<BlockPos> wood = List.of(
                origin,
                origin.above(),
                origin.above(2),
                origin.above(3),
                origin.above(2).relative(outward.getClockWise()),
                origin.above(2).relative(outward.getCounterClockWise())
        );
        BlockPos sign = origin.above(2).relative(outward);
        List<BlockPos> all = new ArrayList<>(wood);
        all.add(sign);
        if (!canReplace(level, all)) {
            return false;
        }
        wood.forEach(pos -> level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL));
        BlockState signState = Blocks.OAK_WALL_SIGN.defaultBlockState()
                .setValue(WallSignBlock.FACING, outward);
        level.setBlock(sign, signState, Block.UPDATE_ALL);
        addSupport(player, "tombstone", level, origin, all, sign);
        return true;
    }

    private static void tickTombstoneSkeletons(
            ServerPlayer owner,
            ServerLevel level,
            CompoundTag support,
            BlockPos origin
    ) {
        long now = level.getGameTime();
        if (now < support.getLong(NEXT_SPAWN)) {
            return;
        }
        int spawned = support.getInt(SPAWNED);
        if (spawned >= 6) {
            return;
        }
        Skeleton skeleton = EntityType.SKELETON.create(level);
        if (skeleton == null) {
            return;
        }
        BlockPos spawnPos = findSkeletonSpawn(level, origin);
        if (spawnPos == null) {
            support.putLong(NEXT_SPAWN, now + 20L);
            return;
        }
        skeleton.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                owner.getYRot(), 0.0F);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        skeleton.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        skeleton.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        skeleton.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        skeleton.getPersistentData().putUUID(SUMMON_OWNER, owner.getUUID());
        skeleton.setPersistenceRequired();
        level.addFreshEntity(skeleton);
        support.putInt(SPAWNED, spawned + 1);
        support.putLong(NEXT_SPAWN, now + 10L * 20L);
    }

    private static void clearSkeletonOwnerTargets(ServerPlayer owner, ServerLevel level) {
        for (Skeleton skeleton : level.getEntitiesOfClass(
                Skeleton.class,
                owner.getBoundingBox().inflate(96.0D),
                skeleton -> isOwnedSkeleton(skeleton, owner.getUUID()))) {
            if (skeleton.getTarget() == owner) {
                skeleton.setTarget(null);
            }
        }
    }

    private static BlockPos findOrigin(ServerPlayer player, ServerLevel level) {
        BlockPos start = player.blockPosition().relative(player.getDirection(), 2);
        for (int offset = 2; offset >= -3; offset--) {
            BlockPos candidate = start.offset(0, offset, 0);
            if (level.getBlockState(candidate).canBeReplaced()
                    && level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)) {
                return candidate;
            }
        }
        return null;
    }

    private static BlockPos findSkeletonSpawn(ServerLevel level, BlockPos origin) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = origin.relative(direction, 2);
            if (level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir()
                    && level.getBlockState(candidate.below()).isFaceSturdy(
                    level, candidate.below(), Direction.UP)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean canReplace(ServerLevel level, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAnyPlacedBlock(ServerLevel level, CompoundTag support) {
        for (long packed : support.getLongArray(BLOCKS)) {
            BlockPos pos = BlockPos.of(packed);
            if (level.hasChunkAt(pos) && !level.getBlockState(pos).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static void removeTombstone(ServerLevel level, CompoundTag support) {
        for (long packed : support.getLongArray(BLOCKS)) {
            BlockPos pos = BlockPos.of(packed);
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.OAK_PLANKS) || state.is(Blocks.OAK_WALL_SIGN)) {
                level.removeBlock(pos, false);
            }
        }
    }

    private static void addSupport(
            ServerPlayer player,
            String type,
            ServerLevel level,
            BlockPos origin,
            List<BlockPos> blocks,
            BlockPos sign
    ) {
        CompoundTag support = new CompoundTag();
        support.putString(TYPE, type);
        support.putString(DIMENSION, dimension(level));
        support.putLong(ORIGIN, origin.asLong());
        support.putLongArray(BLOCKS, blocks.stream().mapToLong(BlockPos::asLong).toArray());
        support.putLong(SIGN, sign.asLong());
        support.putLong(NEXT_SPAWN, level.getGameTime() + 10L * 20L);
        support.putInt(SPAWNED, 0);
        supports(player).add(support);
    }

    private static ListTag supports(ServerPlayer player) {
        CompoundTag root = UndeadStateManager.rootData(player);
        if (!root.contains(SUPPORTS, Tag.TAG_LIST)) {
            root.put(SUPPORTS, new ListTag());
        }
        return root.getList(SUPPORTS, Tag.TAG_COMPOUND);
    }

    private static String dimension(ServerLevel level) {
        return level.dimension().location().toString();
    }

    private static double distanceSqr(ServerPlayer player, BlockPos pos, double yOffset) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + yOffset, pos.getZ() + 0.5D);
    }
}
