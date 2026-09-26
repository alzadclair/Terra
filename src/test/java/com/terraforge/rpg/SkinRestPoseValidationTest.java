package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.EyeOfCthulhuArmature;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.joml.Matrix4f;
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
    @DisplayName("Verify Eye of Cthulhu Phase 1 and Phase 2 skinned meshes have < 1e-4 rest pose identity error")
    void testRestPoseIdentityError() throws Exception {
        String[] skinFiles = {"eye_of_cthulhu_p1.skin.json", "eye_of_cthulhu_p2.skin.json"};
        int[] expectedMinVerts = {4500, 6000};

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

            // 2. Setup Armature and apply bind matrices
            Skeleton skeleton = EyeOfCthulhuArmature.createArmature();
            skeleton.applyBindMatrices(meshData);

            // 3. Create instance and skin in rest pose
            TerraSkinnedMeshInstance instance = meshData.createInstance();
            instance.skin(skeleton);

            // 4. Check bone and normal palettes are identity
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

            // 5. Check all vertices and normals deformed vs bind positions
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
}
