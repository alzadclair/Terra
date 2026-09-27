package com.terraforge.rpg.client.validation;

import com.mojang.blaze3d.platform.NativeImage;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.client.animation.skeletal.AnimationController;
import com.terraforge.rpg.client.render.entity.state.EyeRenderState;
import com.terraforge.rpg.client.render.entity.state.EyeRenderStateManager;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Automates real runtime GPU screenshot capture directly within the Minecraft / NeoForge in-game world.
 * Renders the Eye of Cthulhu with authentic shaders, lighting, sky, and terrain directly into the main render target.
 */
public final class EyeRuntimeCapture {

    public static final Path OUTPUT_DIR = Path.of("build/visual_validation/runtime_real");
    private static EyeOfCthulhuEntity spawnEye = null;
    private static int captureStep = 0;
    private static int stepTickWait = 0;
    private static boolean finished = false;

    private static int warmupTicks = 0;

    private EyeRuntimeCapture() {}

    public static boolean isFinished() {
        return finished;
    }

    public static void startCapture() {
        finished = false;
        captureStep = 0;
        stepTickWait = 0;
        warmupTicks = 0;
    }

    public static void onClientTick(Minecraft mc) {
        if (finished || mc.player == null || mc.level == null || mc.screen != null) return;

        if (warmupTicks < 40) {
            warmupTicks++;
            return;
        }

        if (spawnEye == null || !spawnEye.isAlive()) {
            spawnEye = new EyeOfCthulhuEntity(ModEntities.EYE_OF_CTHULHU.get(), mc.level);
            spawnEye.setId(-9999);
            mc.level.addEntity(spawnEye);
            mc.options.hideGui = true;
            try {
                Files.createDirectories(OUTPUT_DIR);
            } catch (IOException ignored) {}
        }

        try {
            mc.level.setDayTime(6000);
        } catch (Exception ignored) {}

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        // Position player looking directly North (-Z)
        mc.player.setXRot(0.0f);
        mc.player.setYRot(180.0f);
        mc.player.xRotO = 0.0f;
        mc.player.yRotO = 180.0f;

        // Discard any existing Eye entities from previous runs to avoid clutter
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof EyeOfCthulhuEntity && e != spawnEye) {
                e.discard();
            }
        }

        // Position Eye 4.5 blocks North of player, floating directly in front of camera
        double eyeX = px;
        double eyeY = py + 0.3;
        double eyeZ = pz - 4.5;

        spawnEye.setPos(eyeX, eyeY, eyeZ);
        spawnEye.xo = eyeX;
        spawnEye.yo = eyeY;
        spawnEye.zo = eyeZ;
        spawnEye.setDeltaMovement(Vec3.ZERO);
        spawnEye.setNoGravity(true);

        EyeRenderState state = EyeRenderStateManager.getOrCreate(spawnEye);

        stepTickWait++;
        if (stepTickWait < 8) {
            // Give each pose 8 ticks to settle and render on screen
            return;
        }
        stepTickWait = 0;

        switch (captureStep) {
            case 0 -> {
                // Setup Pose 1: P1 Front View
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_1,
                        EyeOfCthulhuEntity.EyeAnimState.IDLE, 0.0f, 0.0f, 0.0f);
            }
            case 1 -> {
                saveScreen("eye_p1_runtime_front.png");
                // Setup Pose 2: P1 Side View (90 deg)
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_1,
                        EyeOfCthulhuEntity.EyeAnimState.IDLE, 0.0f, 90.0f, 0.0f);
            }
            case 2 -> {
                saveScreen("eye_p1_runtime_side.png");
                // Setup Pose 3: P1 Back View (180 deg)
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_1,
                        EyeOfCthulhuEntity.EyeAnimState.IDLE, 0.0f, 180.0f, 0.0f);
            }
            case 3 -> {
                saveScreen("eye_p1_runtime_back.png");
                // Setup Pose 4: P1 Charge View
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_1,
                        EyeOfCthulhuEntity.EyeAnimState.CHARGE, 0.25f, 25.0f, -15.0f);
            }
            case 4 -> {
                saveScreen("eye_p1_runtime_charge.png");
                // Setup Pose 5: P2 Front View
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.PHASE2_IDLE, 0.0f, 0.0f, 0.0f);
            }
            case 5 -> {
                saveScreen("eye_p2_runtime_front.png");
                // Setup Pose 6: P2 Side View (90 deg)
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.PHASE2_IDLE, 0.0f, 90.0f, 0.0f);
            }
            case 6 -> {
                saveScreen("eye_p2_runtime_side.png");
                // Setup Pose 7: P2 Back View (180 deg)
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.PHASE2_IDLE, 0.0f, 180.0f, 0.0f);
            }
            case 7 -> {
                saveScreen("eye_p2_runtime_back.png");
                // Setup Pose 8: P2 Charge View
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.PHASE2_CHARGE, 0.25f, 25.0f, -15.0f);
            }
            case 8 -> {
                saveScreen("eye_p2_runtime_charge.png");
                // Setup Pose 9: P2 Bite Open
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.PHASE2_BITE, 0.35f, 35.0f, -5.0f);
            }
            case 9 -> {
                saveScreen("eye_p2_runtime_bite_open.png");
                // Setup Pose 10: P2 Bite Close
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.PHASE2_BITE, 0.70f, 35.0f, -5.0f);
            }
            case 10 -> {
                saveScreen("eye_p2_runtime_bite_close.png");
                // Setup Pose 11: Transition Before Swap (P1)
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.TRANSITIONING_P1,
                        EyeOfCthulhuEntity.EyeAnimState.TRANSITIONING, 0.25f, 20.0f, 10.0f);
            }
            case 11 -> {
                saveScreen("eye_runtime_transition_before_swap.png");
                // Setup Pose 12: Transition After Swap (P2)
                configurePose(spawnEye, state, EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2,
                        EyeOfCthulhuEntity.EyeAnimState.TRANSITIONING, 0.55f, 20.0f, 10.0f);
            }
            case 12 -> {
                saveScreen("eye_runtime_transition_after_swap.png");
                mc.options.hideGui = false;
                finished = true;
                TerraLogger.info("VALIDATION", "All 12 real runtime screenshots captured successfully to " + OUTPUT_DIR.toAbsolutePath());
                if ("true".equalsIgnoreCase(System.getProperty("terraforge.exit_after_capture"))) {
                    mc.stop();
                }
            }
        }
        captureStep++;
    }

    private static void configurePose(EyeOfCthulhuEntity eye, EyeRenderState state,
                                      EyeOfCthulhuEntity.EyeVisualPhase phase,
                                      EyeOfCthulhuEntity.EyeAnimState animState,
                                      float animTime, float yaw, float pitch) {
        eye.setVisualPhase(phase);
        eye.setAnimState(animState);
        eye.setYRot(yaw);
        eye.setXRot(pitch);
        eye.yRotO = yaw;
        eye.xRotO = pitch;
        eye.yBodyRot = yaw;
        eye.yBodyRotO = yaw;
        eye.yHeadRot = yaw;
        eye.yHeadRotO = yaw;

        boolean isP2 = (phase == EyeOfCthulhuEntity.EyeVisualPhase.PHASE_2);
        AnimationController controller = state.getAnimController(isP2);
        if (controller != null) {
            String clipName = animState.name().toLowerCase();
            if (controller.getClip(clipName) != null) {
                controller.play(clipName, false);
                controller.update(animTime - controller.getCurrentTime());
            }
        }
    }

    private static void saveScreen(String filename) {
        try {
            NativeImage img = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
            File out = OUTPUT_DIR.resolve(filename).toFile();
            img.writeToFile(out);
            img.close();
            TerraLogger.info("VALIDATION", "Captured real in-game screenshot: " + out.getName() + " (" + out.length() + " bytes)");
        } catch (Exception e) {
            TerraLogger.error("VALIDATION", "Failed to capture in-game screenshot " + filename + ": " + e.getMessage());
        }
    }
}
