package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.network.C2S_UluruMissileControl;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class UluruMissileController {
    private static final int GUIDED_MAX_COST_X2 = 30 * 20 * 2;
    private static final int LOCAL_FLIGHT_SOUND_INTERVAL_TICKS = 12;
    private static final int MISSING_ENTITY_TIMEOUT_TICKS = 60;
    private static final int RESTORE_FREEZE_TICKS = 20;
    private static final int TERRAIN_RECENTER_TICKS = 120;

    private static int controlledEntityId = -1;
    private static int blackScreenTicks;
    private static int missingEntityTicks;
    private static int remainingCostX2;
    private static int localFlightSoundTicks;
    private static int restoreFreezeTicks;
    private static int terrainRecenterTicks;
    private static boolean hasRestorePoint;
    private static double restoreX;
    private static double restoreY;
    private static double restoreZ;
    private static float restoreYaw;
    private static float restorePitch;

    private UluruMissileController() {
    }

    public static void start(int entityId) {
        controlledEntityId = entityId;
        missingEntityTicks = 0;
        remainingCostX2 = GUIDED_MAX_COST_X2;
        localFlightSoundTicks = 0;
        restoreFreezeTicks = 0;
        terrainRecenterTicks = 0;
        captureRestorePoint();
        setCameraToControlledEntity();
    }

    public static void stop(boolean blackScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            beginRestoreFreeze(minecraft);
            minecraft.setCameraEntity(minecraft.player);
            terrainRecenterTicks = TERRAIN_RECENTER_TICKS;
            recenterTerrainOnPlayer(minecraft, true);
        }
        // Notify server to release control so interactions are unblocked
        if (controlledEntityId >= 0) {
            NetworkHandler.sendToServer(new C2S_UluruMissileControl(controlledEntityId, 0, 0, false, true));
        }
        UluruGhostEntityManager.clear();
        controlledEntityId = -1;
        missingEntityTicks = 0;
        remainingCostX2 = 0;
        localFlightSoundTicks = 0;
        if (blackScreen) {
            blackScreenTicks = 8;
        }
    }

    public static void tick(Minecraft minecraft) {
        if (blackScreenTicks > 0) {
            blackScreenTicks--;
        }
        if (restoreFreezeTicks > 0) {
            applyRestoreFreeze(minecraft);
            restoreFreezeTicks--;
        }
        if (terrainRecenterTicks > 0) {
            recenterTerrainOnPlayer(minecraft, false);
            terrainRecenterTicks--;
        }

        if (controlledEntityId < 0) {
            return;
        }
        if (minecraft.level == null || minecraft.player == null) {
            stop(false);
            return;
        }
        pinPlayerToRestorePoint(minecraft);

        Entity missile = minecraft.level.getEntity(controlledEntityId);
        boolean boosting = minecraft.options.keyJump.isDown();
        float yaw = minecraft.player.getYRot();
        float pitch = minecraft.player.getXRot();
        if (missile == null || !missile.isAlive()) {
            missingEntityTicks++;
            if (missingEntityTicks >= MISSING_ENTITY_TIMEOUT_TICKS) {
                // Missile has been unreachable for too long - force stop control
                stop(true);
                return;
            }
            NetworkHandler.sendToServer(new C2S_UluruMissileControl(
                    controlledEntityId,
                    yaw,
                    pitch,
                    boosting
            ));
            tickLocalFlightSound(minecraft, boosting);
            remainingCostX2 = Math.max(0, remainingCostX2 - (boosting ? 3 : 2));
            return;
        }
        missingEntityTicks = 0;

        if (minecraft.getCameraEntity() != missile) {
            minecraft.setCameraEntity(missile);
        }

        syncLocalMissileRotation(missile, yaw, pitch);
        NetworkHandler.sendToServer(new C2S_UluruMissileControl(
                controlledEntityId,
                yaw,
                pitch,
                boosting
        ));
        tickLocalFlightSound(minecraft, boosting);
        remainingCostX2 = Math.max(0, remainingCostX2 - (boosting ? 3 : 2));
    }

    public static boolean isControlling() {
        return controlledEntityId >= 0;
    }

    public static int blackScreenTicks() {
        return blackScreenTicks;
    }

    public static float remainingFraction() {
        if (!isControlling() || remainingCostX2 <= 0) {
            return 0.0f;
        }
        return Math.min(1.0f, remainingCostX2 / (float) GUIDED_MAX_COST_X2);
    }

    public static int remainingTicks() {
        return Math.max(0, (remainingCostX2 + 1) / 2);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (isControlling()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isControlling()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!isControlling()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (isControlling() && event.getNewScreen() != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!isControlling()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        event.setYaw(minecraft.player.getYRot());
        event.setPitch(minecraft.player.getXRot());
        event.setRoll(0.0f);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!isControlling() && restoreFreezeTicks <= 0) {
            return;
        }
        event.getInput().forwardImpulse = 0.0f;
        event.getInput().leftImpulse = 0.0f;
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
    }

    private static void captureRestorePoint() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            hasRestorePoint = false;
            return;
        }
        restoreX = minecraft.player.getX();
        restoreY = minecraft.player.getY();
        restoreZ = minecraft.player.getZ();
        restoreYaw = minecraft.player.getYRot();
        restorePitch = minecraft.player.getXRot();
        hasRestorePoint = true;
    }

    private static void beginRestoreFreeze(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }
        restoreFreezeTicks = RESTORE_FREEZE_TICKS;
        applyRestoreFreeze(minecraft);
    }

    private static void applyRestoreFreeze(Minecraft minecraft) {
        if (minecraft.player == null) {
            restoreFreezeTicks = 0;
            return;
        }
        pinPlayerToRestorePoint(minecraft);
    }

    private static void setCameraToControlledEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity missile = minecraft.level.getEntity(controlledEntityId);
        if (missile != null) {
            minecraft.setCameraEntity(missile);
        }
    }

    private static void syncLocalMissileRotation(Entity missile, float yaw, float pitch) {
        missile.setYRot(yaw);
        missile.setXRot(pitch);
        missile.yRotO = yaw;
        missile.xRotO = pitch;
        missile.setYHeadRot(yaw);
        missile.setYBodyRot(yaw);
    }

    private static void tickLocalFlightSound(Minecraft minecraft, boolean boosting) {
        if (minecraft.player == null) {
            return;
        }
        if (localFlightSoundTicks > 0) {
            localFlightSoundTicks--;
            return;
        }

        localFlightSoundTicks = LOCAL_FLIGHT_SOUND_INTERVAL_TICKS;
        minecraft.player.playSound(ModSounds.MISSILE_FLY.get(), boosting ? 1.15f : 0.75f, boosting ? 1.45f : 1.0f);
    }

    private static void pinPlayerToRestorePoint(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.setDeltaMovement(Vec3.ZERO);
        minecraft.player.fallDistance = 0.0F;
        minecraft.player.setOnGround(true);
        if (!hasRestorePoint) {
            return;
        }

        double dx = minecraft.player.getX() - restoreX;
        double dy = minecraft.player.getY() - restoreY;
        double dz = minecraft.player.getZ() - restoreZ;
        if (dx * dx + dy * dy + dz * dz > 1.0E-4D) {
            minecraft.player.setPos(restoreX, restoreY, restoreZ);
            minecraft.player.setOldPosAndRot();
        }
    }

    private static void recenterTerrainOnPlayer(Minecraft minecraft, boolean rebuildRenderer) {
        if (minecraft.level == null || minecraft.player == null) {
            terrainRecenterTicks = 0;
            return;
        }
        ChunkPos chunk = minecraft.player.chunkPosition();
        minecraft.level.getChunkSource().updateViewRadius(minecraft.options.getEffectiveRenderDistance());
        minecraft.level.getChunkSource().updateViewCenter(chunk.x, chunk.z);
        if (rebuildRenderer) {
            minecraft.levelRenderer.allChanged();
        } else {
            minecraft.levelRenderer.needsUpdate();
        }
    }
}
