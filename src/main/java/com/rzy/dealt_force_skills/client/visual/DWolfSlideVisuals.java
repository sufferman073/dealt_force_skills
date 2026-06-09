package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.alrex.parcool.client.animation.Animator;
import com.alrex.parcool.client.animation.impl.SlidingAnimator;
import com.alrex.parcool.client.animation.PlayerModelRotator;
import com.alrex.parcool.client.animation.PlayerModelTransformer;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.impl.Slide;
import com.alrex.parcool.common.capability.Animation;
import com.alrex.parcool.common.capability.Parkourability;
import com.alrex.parcool.utilities.Easing;
import com.rzy.dealt_force_skills.client.DWolfInputHandler;
import com.rzy.dealt_force_skills.client.character.ClientDWolfHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class DWolfSlideVisuals {
    private static final int MAX_TRANSITION_TICK = 5;
    private static final Map<Integer, SlideVisual> ACTIVE = new HashMap<>();
    private static final Map<Integer, YsmParcoolSnapshot> YSM_PARCOOL_SNAPSHOTS = new HashMap<>();
    private static final Field PARCOOL_ANIMATOR_FIELD = findParcoolAnimatorField();
    private static final Field ACTION_DOING_FIELD = findField(Action.class, "doing");
    private static final Field ACTION_DOING_TICK_FIELD = findField(Action.class, "doingTick");
    private static final Field SLIDE_VECTOR_FIELD = findField(Slide.class, "slidingVec");

    private DWolfSlideVisuals() {
    }

    private static boolean isYesSteveModelLoaded() {
        return ModList.get().getMods().stream().anyMatch(modInfo -> {
            String id = modInfo.getModId().toLowerCase(Locale.ROOT);
            return id.equals("yes_steve_model")
                    || id.equals("yesstevemodel")
                    || id.equals("ysm")
                    || id.contains("yes_steve")
                    || id.contains("yessteve");
        });
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ACTIVE.clear();
            return;
        }

        Set<Integer> seenSliding = new HashSet<>();
        for (Player player : minecraft.level.players()) {
            if (!shouldAnimateSlide(player, minecraft)) {
                continue;
            }

            Vec3 direction = slideDirection(player, minecraft);
            if (direction.lengthSqr() < 0.0001D) {
                continue;
            }

            int entityId = player.getId();
            seenSliding.add(entityId);
            SlideVisual visual = ACTIVE.get(entityId);
            if (visual == null) {
                visual = new SlideVisual(direction);
                ACTIVE.put(entityId, visual);
                installParcoolAnimator(player);
            } else {
                visual.missedTicks = 0;
                visual.activeTicks++;
            }
        }

        Iterator<Map.Entry<Integer, SlideVisual>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, SlideVisual> entry = iterator.next();
            if (seenSliding.contains(entry.getKey())) {
                continue;
            }
            entry.getValue().missedTicks++;
            if (entry.getValue().missedTicks > 1) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!isYesSteveModelLoaded()) {
            return;
        }
        Player player = event.getEntity();
        Vec3 direction = directionFor(player.getId());
        if (direction == null || direction.lengthSqr() < 0.0001D) {
            return;
        }

        // YSM already has its own ParCool slide compatibility. Instead of rotating the
        // whole custom model here (that caused the "zombie leaning upward" pose), briefly
        // expose our slide as a real ParCool Slide state only during rendering. The actual
        // DWolf slide movement remains our own logic, and the original non-YSM animator is
        // left untouched.
        prepareParcoolSlideForYsm(player, direction);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        restoreParcoolSlideAfterYsm(event.getEntity());
    }

    private static boolean shouldAnimateSlide(Player player, Minecraft minecraft) {
        if (player.isInvisible() || player.isSpectator()) {
            return false;
        }

        if (player == minecraft.player) {
            return ClientDWolfHudState.shouldRender() && DWolfInputHandler.isSlideActiveForVisual();
        }

        return player.getPose() == Pose.SWIMMING
                && !player.isInWater()
                && player.getDeltaMovement().horizontalDistanceSqr() > 0.005D;
    }

    private static Vec3 slideDirection(Player player, Minecraft minecraft) {
        Vec3 direction = Vec3.ZERO;
        if (player == minecraft.player) {
            Vec3 look = player.getLookAngle();
            direction = new Vec3(look.x, 0.0D, look.z);
        }
        if (direction.lengthSqr() < 0.0001D) {
            Vec3 motion = player.getDeltaMovement();
            direction = new Vec3(motion.x, 0.0D, motion.z);
        }
        if (direction.lengthSqr() < 0.0001D) {
            Vec3 look = player.getLookAngle();
            direction = new Vec3(look.x, 0.0D, look.z);
        }
        return direction.lengthSqr() < 0.0001D ? Vec3.ZERO : direction.normalize();
    }

    private static void installParcoolAnimator(Player player) {
        Animation animation = Animation.get(player);
        if (animation != null) {
            setParcoolAnimator(animation, new DWolfParcoolSlideAnimator(player.getId()));
        }
    }

    private static Field findParcoolAnimatorField() {
        return findField(Animation.class, "animator");
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void setParcoolAnimator(Animation animation, Animator animator) {
        if (PARCOOL_ANIMATOR_FIELD == null) {
            return;
        }
        try {
            PARCOOL_ANIMATOR_FIELD.set(animation, animator);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Animator getParcoolAnimator(Animation animation) {
        if (PARCOOL_ANIMATOR_FIELD == null) {
            return null;
        }
        try {
            Object value = PARCOOL_ANIMATOR_FIELD.get(animation);
            return value instanceof Animator animator ? animator : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void prepareParcoolSlideForYsm(Player player, Vec3 direction) {
        if (ACTION_DOING_FIELD == null || ACTION_DOING_TICK_FIELD == null || SLIDE_VECTOR_FIELD == null) {
            return;
        }
        Parkourability parkourability = Parkourability.get(player);
        Animation animation = Animation.get(player);
        if (parkourability == null || animation == null) {
            return;
        }
        try {
            Slide slide = parkourability.get(Slide.class);
            int entityId = player.getId();
            if (!YSM_PARCOOL_SNAPSHOTS.containsKey(entityId)) {
                YSM_PARCOOL_SNAPSHOTS.put(entityId, new YsmParcoolSnapshot(
                        ACTION_DOING_FIELD.getBoolean(slide),
                        ACTION_DOING_TICK_FIELD.getInt(slide),
                        (Vec3) SLIDE_VECTOR_FIELD.get(slide),
                        getParcoolAnimator(animation)
                ));
            }
            ACTION_DOING_FIELD.setBoolean(slide, true);
            ACTION_DOING_TICK_FIELD.setInt(slide, Math.min(MAX_TRANSITION_TICK, Math.max(1, activeTick(player.getId()))));
            SLIDE_VECTOR_FIELD.set(slide, direction);
            setParcoolAnimator(animation, new SlidingAnimator());
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private static void restoreParcoolSlideAfterYsm(Player player) {
        YsmParcoolSnapshot snapshot = YSM_PARCOOL_SNAPSHOTS.remove(player.getId());
        if (snapshot == null || ACTION_DOING_FIELD == null || ACTION_DOING_TICK_FIELD == null || SLIDE_VECTOR_FIELD == null) {
            return;
        }
        Parkourability parkourability = Parkourability.get(player);
        Animation animation = Animation.get(player);
        if (parkourability == null) {
            return;
        }
        try {
            Slide slide = parkourability.get(Slide.class);
            ACTION_DOING_FIELD.setBoolean(slide, snapshot.doing());
            ACTION_DOING_TICK_FIELD.setInt(slide, snapshot.doingTick());
            SLIDE_VECTOR_FIELD.set(slide, snapshot.slidingVector());
            if (animation != null) {
                setParcoolAnimator(animation, snapshot.animator());
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private static Vec3 directionFor(int entityId) {
        SlideVisual visual = ACTIVE.get(entityId);
        return visual == null ? null : visual.direction;
    }

    private static int activeTick(int entityId) {
        SlideVisual visual = ACTIVE.get(entityId);
        return visual == null ? 1 : visual.activeTicks;
    }

    private static float transition(int tick, float partialTick) {
        float animFactor = (tick + partialTick) / MAX_TRANSITION_TICK;
        if (animFactor > 1.0F) {
            animFactor = 1.0F;
        }
        return new Easing(animFactor)
                .sinInOut(0.0F, 1.0F, 0.0F, 1.0F)
                .get();
    }

    private static float yawFromDirection(Vec3 direction) {
        return (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG - 90.0D);
    }

    private static final class SlideVisual {
        private Vec3 direction;
        private int missedTicks;
        private int activeTicks = 1;

        private SlideVisual(Vec3 direction) {
            this.direction = direction;
        }
    }

    private record YsmParcoolSnapshot(boolean doing, int doingTick, Vec3 slidingVector, Animator animator) {
    }

    private static final class DWolfParcoolSlideAnimator extends Animator {
        private final int entityId;

        private DWolfParcoolSlideAnimator(int entityId) {
            this.entityId = entityId;
        }

        @Override
        public boolean shouldRemoved(Player player, Parkourability parkourability) {
            return !ACTIVE.containsKey(entityId);
        }

        @Override
        public void animatePost(Player player, Parkourability parkourability, PlayerModelTransformer transformer) {
            float animFactor = transition(getTick(), transformer.getPartialTick());

            transformer
                    .translateLeftLeg(
                            0.0F,
                            -1.2F * animFactor,
                            -2.0F * animFactor
                    )
                    .translateRightArm(
                            0.0F,
                            1.2F * animFactor,
                            1.2F * animFactor
                    )
                    .translateHead(0.0F, 0.0F, -animFactor)
                    .rotateHeadPitch(50.0F * animFactor)
                    .rotateAdditionallyHeadYaw(50.0F * animFactor)
                    .rotateAdditionallyHeadRoll(-10.0F * animFactor)
                    .rotateRightArm((float) Math.toRadians(50.0D), (float) Math.toRadians(-40.0D), 0.0F, animFactor)
                    .rotateLeftArm((float) Math.toRadians(20.0D), 0.0F, (float) Math.toRadians(-100.0D), animFactor)
                    .rotateRightLeg((float) Math.toRadians(-30.0D), (float) Math.toRadians(40.0D), 0.0F, animFactor)
                    .rotateLeftLeg((float) Math.toRadians(40.0D), (float) Math.toRadians(-30.0D), (float) Math.toRadians(15.0D), animFactor)
                    .makeLegsLittleMoving()
                    .makeArmsNatural()
                    .end();
        }

        @Override
        public boolean rotatePre(Player player, Parkourability parkourability, PlayerModelRotator rotator) {
            Vec3 direction = directionFor(entityId);
            if (direction == null) {
                return false;
            }

            float animFactor = transition(getTick(), rotator.getPartialTick());
            float yRot = yawFromDirection(direction);
            rotator
                    .rotateYawRightward(180.0F + yRot)
                    .rotatePitchFrontward(-55.0F * animFactor)
                    .translate(0.35F * animFactor, 0.0F, 0.0F)
                    .rotateYawRightward(-55.0F * animFactor)
                    .translate(0.0F, -0.7F * animFactor, -0.3F * animFactor);
            return true;
        }
    }
}
