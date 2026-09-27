package com.terraforge.rpg;

import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EyeFacingMathTest {

    /**
     * In Minecraft model space (after EyeOfCthulhuModel's rotationX(PI/2) from skin space):
     * The cornea / front of the eye points along -Z: (0, 0, -1).
     *
     * In EyeOfCthulhuRenderer.setupRotations:
     * poseStack is rotated by (180.0F - yaw) around +Y.
     *
     * In EyeOfCthulhuModel.renderToBuffer:
     * poseStack is rotated by -pitch around +X (OpenGL +Y up coordinate system).
     *
     * This test verifies that for any target/charge attack vector (dx, dy, dz):
     * The resulting world forward vector points directly at (dx, dy, dz) with dot product > 0.999!
     */
    @Test
    @DisplayName("Verify Eye of Cthulhu facing math aligns forward vector with attack vector (dot product > 0.999)")
    void testFacingMathAlignment() {
        double[][] testVectors = {
                {10.0, 0.0, 0.0},       // Due East
                {-10.0, 0.0, 0.0},      // Due West
                {0.0, 0.0, 10.0},       // Due South
                {0.0, 0.0, -10.0},      // Due North
                {5.0, -8.0, 5.0},       // Diving attack (downward diagonal)
                {-7.0, 6.0, 3.0},       // Rising attack (upward diagonal)
                {0.0, -12.0, 0.1},      // Vertical dive
                {0.0, 12.0, 0.1},       // Vertical ascent
                {12.3, -4.5, -9.8},     // Arbitrary 3D angle
                {-15.2, -18.7, 22.4}    // Steep dive angle
        };

        for (double[] vec : testVectors) {
            double dx = vec[0];
            double dy = vec[1];
            double dz = vec[2];

            double hDist = Math.sqrt(dx * dx + dz * dz);
            float targetYaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            float targetPitch = (float)(-(Mth.atan2(dy, hDist) * (180.0 / Math.PI)));

            // Compute cumulative rotation matrix:
            // 1. setupRotations: rotateY(180 - targetYaw)
            // 2. renderToBuffer: rotateX(-targetPitch)
            Matrix4f worldTransform = new Matrix4f()
                    .rotateY((float) Math.toRadians(180.0F - targetYaw))
                    .rotateX((float) Math.toRadians(-targetPitch));

            // Local forward in Minecraft model space is (0, 0, -1)
            Vector4f localForward = new Vector4f(0.0f, 0.0f, -1.0f, 0.0f);
            Vector4f worldForward = worldTransform.transform(localForward);

            // Normalize target attack vector
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            float expectedX = (float)(dx / len);
            float expectedY = (float)(dy / len);
            float expectedZ = (float)(dz / len);

            float dot = worldForward.x * expectedX + worldForward.y * expectedY + worldForward.z * expectedZ;

            assertTrue(dot > 0.999f, String.format(
                    "Facing alignment failed for vector [%.1f, %.1f, %.1f]: dot product = %.6f (expected > 0.999), worldForward=[%.4f, %.4f, %.4f], target=[%.4f, %.4f, %.4f]",
                    dx, dy, dz, dot, worldForward.x, worldForward.y, worldForward.z, expectedX, expectedY, expectedZ
            ));
        }
    }
}
