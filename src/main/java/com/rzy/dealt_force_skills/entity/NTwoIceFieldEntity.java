package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.advancement.DfsAchievements;
import com.rzy.dealt_force_skills.character.ntwo.NTwoStateManager;
import com.rzy.dealt_force_skills.character.uluru.UluruExplosionHelper;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class NTwoIceFieldEntity extends Entity implements ItemSupplier {
    public static final String ACHIEVEMENT_SOURCE_CONDENSED = "condensed";
    public static final String ACHIEVEMENT_SOURCE_DEWAR = "dewar";

    private UUID ownerId;
    private int lifeTicks = 16 * 20;
    private int intervalTicks = 10;
    private double radius = 6.0D;
    private int coldAmount = 10;
    private float damage = 2.0F;
    private String achievementSource = "";
    private String achievementSourceId = "";

    public NTwoIceFieldEntity(EntityType<? extends NTwoIceFieldEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public NTwoIceFieldEntity(
            EntityType<? extends NTwoIceFieldEntity> type,
            Level level,
            ServerPlayer owner,
            double radius,
            int lifeTicks,
            int intervalTicks,
            int coldAmount,
            float damage
    ) {
        this(type, level);
        ownerId = owner.getUUID();
        this.radius = radius;
        this.lifeTicks = lifeTicks;
        this.intervalTicks = Math.max(1, intervalTicks);
        this.coldAmount = coldAmount;
        this.damage = damage;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.BLUE_ICE);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.SNOWFLAKE,
                        getX() + (random.nextDouble() - 0.5D) * radius * 2.0D,
                        getY() + 0.08D,
                        getZ() + (random.nextDouble() - 0.5D) * radius * 2.0D,
                        0.0D, 0.02D, 0.0D);
            }
            return;
        }
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer owner = owner(level);
        if (owner == null || tickCount >= lifeTicks) {
            discard();
            return;
        }
        if (tickCount % intervalTicks == 0) {
            applyField(level, owner);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        lifeTicks = tag.contains("LifeTicks") ? tag.getInt("LifeTicks") : lifeTicks;
        intervalTicks = tag.contains("IntervalTicks") ? Math.max(1, tag.getInt("IntervalTicks")) : intervalTicks;
        radius = tag.contains("Radius") ? tag.getDouble("Radius") : radius;
        coldAmount = tag.contains("ColdAmount") ? tag.getInt("ColdAmount") : coldAmount;
        damage = tag.contains("Damage") ? tag.getFloat("Damage") : damage;
        achievementSource = tag.getString("AchievementSource");
        achievementSourceId = tag.getString("AchievementSourceId");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("LifeTicks", lifeTicks);
        tag.putInt("IntervalTicks", intervalTicks);
        tag.putDouble("Radius", radius);
        tag.putInt("ColdAmount", coldAmount);
        tag.putFloat("Damage", damage);
        tag.putString("AchievementSource", achievementSource);
        tag.putString("AchievementSourceId", achievementSourceId);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void configureAchievementSource(String source, String sourceId) {
        achievementSource = source == null ? "" : source;
        achievementSourceId = sourceId == null ? "" : sourceId;
    }

    private void applyField(ServerLevel level, ServerPlayer owner) {
        AABB box = new AABB(position(), position()).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (!TargetingUtil.isSelfOrHostileLivingFor(owner, target)
                    || target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).distanceTo(position()) > radius
                    || !UluruExplosionHelper.hasExplosionLineOfSight(level, position(), target)) {
                continue;
            }
            // Self-harm: field damage can hit caster; cold value never stacks on self.
            if (!TargetingUtil.isSelf(owner, target)) {
                boolean froze = NTwoStateManager.addCold(owner, target, coldAmount);
                if (froze) {
                    recordSourceFreeze(owner, target);
                }
            }
            if (damage > 0.0F) {
                Vec3 beforeDamageMovement = target.getDeltaMovement();
                target.invulnerableTime = 0;
                boolean wasAlive = target.isAlive();
                boolean damaged = SkillDamageHelper.hurt(target, SkillDamageHelper.trueDamage(level, this, owner), owner, damage);
                target.setDeltaMovement(beforeDamageMovement);
                target.hurtMarked = true;
                if (damaged && wasAlive && !target.isAlive()) {
                    DfsAchievements.recordNTwoColdDamageKill(owner, target);
                }
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 0.1D, getZ(),
                48, radius * 0.85D, 0.25D, radius * 0.85D, 0.02D);
    }

    private ServerPlayer owner(ServerLevel level) {
        return ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
    }

    private void recordSourceFreeze(ServerPlayer owner, LivingEntity target) {
        if (ACHIEVEMENT_SOURCE_CONDENSED.equals(achievementSource)) {
            DfsAchievements.recordNTwoCondensedFreeze(owner, achievementSourceId, target.getUUID());
        } else if (ACHIEVEMENT_SOURCE_DEWAR.equals(achievementSource)) {
            DfsAchievements.recordNTwoDewarFreeze(owner, achievementSourceId);
        }
    }
}
