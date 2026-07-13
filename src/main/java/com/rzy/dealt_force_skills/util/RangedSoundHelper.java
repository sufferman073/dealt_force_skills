package com.rzy.dealt_force_skills.util;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class RangedSoundHelper {
    public static final double TRAP_SOUND_RANGE = 16.0D;
    private static final Map<String, Long> RECENT_SOUNDS = new HashMap<>();
    private static long lastCleanupTick = Long.MIN_VALUE;

    private RangedSoundHelper() {
    }

    public static void playTrapSound(ServerLevel level, Vec3 position, SoundEvent sound, float volume, float pitch) {
        play(level, position, sound, SoundSource.PLAYERS, volume, pitch, TRAP_SOUND_RANGE);
    }

    public static void playFollowingPlayer(ServerPlayer player, SoundEvent sound, SoundSource source,
                                           float volume, float pitch, double radius) {
        ServerLevel level = player.serverLevel();
        playFollowingEntity(level, player, sound, source, volume, pitch, radius);
    }

    public static void playFollowingEntity(ServerLevel level, Entity entity, SoundEvent sound, SoundSource source,
                                           float volume, float pitch, double radius) {
        Vec3 position = entity.position();
        long seed = level.getRandom().nextLong();
        double radiusSqr = radius * radius;
        for (ServerPlayer listener : level.players()) {
            if (listener.distanceToSqr(position.x, position.y, position.z) > radiusSqr) {
                continue;
            }
            listener.connection.send(new ClientboundSoundEntityPacket(
                    Holder.direct(sound),
                    source,
                    entity,
                    volume,
                    pitch,
                    seed
            ));
        }
    }

    public static void playTrapSoundThrottled(ServerLevel level, Vec3 position, SoundEvent sound, float volume, float pitch,
                                              int minIntervalTicks, double mergeRadius) {
        playThrottled(level, position, sound, SoundSource.PLAYERS, volume, pitch,
                TRAP_SOUND_RANGE, minIntervalTicks, mergeRadius);
    }

    public static boolean playThrottled(ServerLevel level, Vec3 position, SoundEvent sound, SoundSource source,
                                        float volume, float pitch, double radius,
                                        int minIntervalTicks, double mergeRadius) {
        if (!markAllowed(level, position, sound, minIntervalTicks, mergeRadius)) {
            return false;
        }
        play(level, position, sound, source, volume, pitch, radius);
        return true;
    }

    public static void play(ServerLevel level, Vec3 position, SoundEvent sound, SoundSource source,
                            float volume, float pitch, double radius) {
        double radiusSqr = radius * radius;
        long seed = level.getRandom().nextLong();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(position.x, position.y, position.z) > radiusSqr) {
                continue;
            }
            player.connection.send(new ClientboundSoundPacket(
                    Holder.direct(sound),
                    source,
                    position.x,
                    position.y,
                    position.z,
                    volume,
                    pitch,
                    seed
            ));
        }
    }

    public static void stop(ServerLevel level, SoundEvent sound, SoundSource source) {
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundStopSoundPacket(sound.getLocation(), source));
        }
    }

    public static void stop(ServerLevel level, Vec3 position, SoundEvent sound, SoundSource source, double radius) {
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(position.x, position.y, position.z) <= radiusSqr) {
                player.connection.send(new ClientboundStopSoundPacket(sound.getLocation(), source));
            }
        }
    }

    public static void clearRuntimeCaches() {
        RECENT_SOUNDS.clear();
        lastCleanupTick = Long.MIN_VALUE;
    }

    private static boolean markAllowed(ServerLevel level, Vec3 position, SoundEvent sound,
                                       int minIntervalTicks, double mergeRadius) {
        if (minIntervalTicks <= 0) {
            return true;
        }
        long now = level.getGameTime();
        cleanup(now);
        String key = key(level, position, sound, Math.max(0.25D, mergeRadius));
        Long last = RECENT_SOUNDS.get(key);
        if (last != null && now - last < minIntervalTicks) {
            return false;
        }
        RECENT_SOUNDS.put(key, now);
        return true;
    }

    private static String key(ServerLevel level, Vec3 position, SoundEvent sound, double mergeRadius) {
        ResourceLocation soundId = sound.getLocation();
        int x = (int) Math.floor(position.x / mergeRadius);
        int y = (int) Math.floor(position.y / mergeRadius);
        int z = (int) Math.floor(position.z / mergeRadius);
        return level.dimension().location() + "|" + soundId + "|" + x + "," + y + "," + z;
    }

    private static void cleanup(long now) {
        if (now - lastCleanupTick < 200L) {
            return;
        }
        lastCleanupTick = now;
        Iterator<Map.Entry<String, Long>> iterator = RECENT_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getValue() > 200L) {
                iterator.remove();
            }
        }
    }
}
