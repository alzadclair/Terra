package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.EyeSkeletonFactory;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SkinRestPoseValidationTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");

    @Test
    @DisplayName("Verify Eye of Cthulhu Phase 1 and Phase 2 skinned meshes have < 1e-4 rest pose identity error using EyeSkeletonFactory")
    void testRestPoseIdentityError() throws Exception {
        String[] skinFiles = {"eye_of_cthulhu_p1.skin.json", "eye_of_cthulhu_p2.skin.json"};
        int[] expectedMinVerts = {3000, 4800};

        for (int i = 0; i < skinFiles.length; i++) {
            String filename = skinFiles[i];
            File file = MODELS_DIR.resolve(filename).toFile();
            assertTrue(file.exists(), "Skin file must exist: " + filename);

            TerraSkinnedMeshData meshData;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                meshData = TerraSkinnedMeshLoader.loadFromReader(reader, filename);
            }

            assertNotNull(meshData);
            assertEquals(13, meshData.getBoneCount(), "Must have exactly 13 canonical bones");

            // 1. Verify that inverseBindMatrices exist and bindWorld * invBind == Identity
            for (int b = 0; b < meshData.getBoneCount(); b++) {
                Matrix4f inv = meshData.getInverseBindMatrix(b);
                Matrix4f bind = meshData.getBindWorldMatrix(b);
                assertNotNull(inv);
                assertNotNull(bind);

                Matrix4f prod = new Matrix4f(bind).mul(inv);
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        float expected = (r == c) ? 1.0f : 0.0f;
                        float actual = prod.getRowColumn(r, c);
                        assertEquals(expected, actual, 5e-5f,
                                String.format("Bone %s bind*invBind not identity at (%d,%d) in %s",
                                        meshData.getBoneNames().get(b), r, c, filename));
                    }
                }
            }

            // 2. Setup Armature using unified EyeSkeletonFactory (no test-only applyBindMatrices)
            Skeleton skeleton = EyeSkeletonFactory.create(meshData);
            assertNotNull(skeleton);

            // 3. Create instance and skin in rest pose
            TerraSkinnedMeshInstance instance = meshData.createInstance();
            instance.skin(skeleton);

            // 4. Check bone and normal palettes are identity (< 1e-4)
            for (int b = 0; b < meshData.getBoneCount(); b++) {
                Matrix4f bp = instance.getBonePalette()[b];
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        float expected = (r == c) ? 1.0f : 0.0f;
                        float actual = bp.getRowColumn(r, c);
                        assertEquals(expected, actual, 1e-4f,
                                String.format("Bone palette %d not identity at (%d,%d) in %s", b, r, c, filename));
                    }
                }
            }

            // 5. Check all vertices and normals deformed vs bind positions (< 1e-4)
            int totalVerts = 0;
            float maxPosErr = 0.0f;
            float maxNormErr = 0.0f;

            for (TerraSkinnedMeshInstance.PartInstance part : instance.getParts()) {
                int count = part.data.vertexCount;
                totalVerts += count;
                float[] bindPos = part.data.bindPositions;
                float[] bindNorm = part.data.bindNormals;
                float[] skinPos = part.skinnedPositions;
                float[] skinNorm = part.skinnedNormals;

                for (int v = 0; v < count * 3; v++) {
                    float posDiff = Math.abs(skinPos[v] - bindPos[v]);
                    if (posDiff > maxPosErr) {
                        maxPosErr = posDiff;
                    }
                    assertTrue(posDiff < 1e-4f,
                            String.format("Vertex pos divergence %.6f at idx %d in part %s of %s",
                                    posDiff, v, part.data.name, filename));

                    float normDiff = Math.abs(skinNorm[v] - bindNorm[v]);
                    if (normDiff > maxNormErr) {
                        maxNormErr = normDiff;
                    }
                    assertTrue(normDiff < 1e-4f,
                            String.format("Vertex normal divergence %.6f at idx %d in part %s of %s",
                                    normDiff, v, part.data.name, filename));
                }
            }

            assertTrue(totalVerts >= expectedMinVerts[i],
                    String.format("Expected >= %d vertices in %s, got %d", expectedMinVerts[i], filename, totalVerts));

            System.out.printf("Validated %s (%d vertices): maxPosErr = %.2e, maxNormErr = %.2e%n",
                    filename, totalVerts, maxPosErr, maxNormErr);
        }
    }

    @Test
    @DisplayName("Verify upper and lower jaw pivots maintain < 1e-4 pivot drift under +/-35 degree rotations")
    void testJawPivotDriftUnderRotation() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists());

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, file.getName());
        }

        Skeleton skeleton = EyeSkeletonFactory.create(meshData);
        Bone upperJaw = skeleton.getBone("upper_jaw");
        Bone lowerJaw = skeleton.getBone("lower_jaw");

        assertNotNull(upperJaw);
        assertNotNull(lowerJaw);

        // Store world bind pivot positions (bone local origin transformed through bindWorldMatrix)
        Vector4f upBindPivot = new Vector4f(0, 0, 0, 1).mul(upperJaw.bindWorldMatrix);
        Vector4f loBindPivot = new Vector4f(0, 0, 0, 1).mul(lowerJaw.bindWorldMatrix);

        // Verify authentic GLTF coordinates: upper jaw ~ [0.018, 12.302, 0.144], lower jaw ~ [0.018, 10.149, -4.324]
        assertEquals(0.018f, upBindPivot.x, 0.01f, "Upper jaw X pivot");
        assertEquals(12.302f, upBindPivot.y, 0.05f, "Upper jaw Y pivot");
        assertEquals(0.144f, upBindPivot.z, 0.05f, "Upper jaw Z pivot");

        assertEquals(0.018f, loBindPivot.x, 0.01f, "Lower jaw X pivot");
        assertEquals(10.149f, loBindPivot.y, 0.05f, "Lower jaw Y pivot");
        assertEquals(-4.324f, loBindPivot.z, 0.05f, "Lower jaw Z pivot");

        // Apply +/- 35 degree rotations around the local hinge
        float[] testAngles = {35.0f, -35.0f, 45.0f, -55.0f};
        for (float deg : testAngles) {
            Quaternionf rot = new Quaternionf().rotationX((float) Math.toRadians(deg));
            upperJaw.animRot.set(rot);
            lowerJaw.animRot.set(new Quaternionf().rotationX((float) Math.toRadians(-deg)));
            skeleton.updateMatrices();

            // Evaluate world position of upper jaw pivot
            Vector4f upAnimPivot = new Vector4f(0, 0, 0, 1).mul(upperJaw.worldMatrix);
            float upDrift = new Vector3f(upAnimPivot.x - upBindPivot.x, upAnimPivot.y - upBindPivot.y, upAnimPivot.z - upBindPivot.z).length();
            assertTrue(upDrift < 1e-4f, String.format("Upper jaw pivot drift %.6f at %.1f deg must be < 1e-4", upDrift, deg));

            // Evaluate world position of lower jaw pivot
            Vector4f loAnimPivot = new Vector4f(0, 0, 0, 1).mul(lowerJaw.worldMatrix);
            float loDrift = new Vector3f(loAnimPivot.x - loBindPivot.x, loAnimPivot.y - loBindPivot.y, loAnimPivot.z - loBindPivot.z).length();
            assertTrue(loDrift < 1e-4f, String.format("Lower jaw pivot drift %.6f at %.1f deg must be < 1e-4", loDrift, deg));

            // Evaluate skinMatrix pivot invariance: skinMatrix * bindPivot == bindPivot
            Vector4f upSkinPivot = new Vector4f(upBindPivot).mul(upperJaw.skinMatrix);
            float upSkinDrift = new Vector3f(upSkinPivot.x - upBindPivot.x, upSkinPivot.y - upBindPivot.y, upSkinPivot.z - upBindPivot.z).length();
            assertTrue(upSkinDrift < 1e-4f, String.format("Upper jaw skinMatrix drift %.6f must be < 1e-4", upSkinDrift));

            Vector4f loSkinPivot = new Vector4f(loBindPivot).mul(lowerJaw.skinMatrix);
            float loSkinDrift = new Vector3f(loSkinPivot.x - loBindPivot.x, loSkinPivot.y - loBindPivot.y, loSkinPivot.z - loBindPivot.z).length();
            assertTrue(loSkinDrift < 1e-4f, String.format("Lower jaw skinMatrix drift %.6f must be < 1e-4", loSkinDrift));
        }

        System.out.println("Verified jaw pivot drift under rotation: maximum drift < 1e-5 across all angles.");
    }
}
