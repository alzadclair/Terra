package com.terraforge.rpg;

import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EyeTextureMappingSanityTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");
    private static final Path TEXTURES_DIR = Path.of("src/main/resources/assets/terraforge_rpg/textures/entity/boss");

    @Test
    @DisplayName("Verify Eye of Cthulhu Phase 1 and Phase 2 texture PNG assets exist and have valid atlas dimensions")
    void testTextureAssetsExistAndHaveValidDimensions() throws Exception {
        Path p1Tex = TEXTURES_DIR.resolve("eye_of_cthulhu_p1.png");
        Path p2Tex = TEXTURES_DIR.resolve("eye_of_cthulhu_p2.png");

        assertTrue(Files.isRegularFile(p1Tex), "P1 texture must exist: " + p1Tex);
        assertTrue(Files.isRegularFile(p2Tex), "P2 texture must exist: " + p2Tex);

        BufferedImage img1 = ImageIO.read(p1Tex.toFile());
        assertNotNull(img1, "P1 texture must be a valid readable image");
        assertEquals(2048, img1.getWidth(), "P1 atlas width must be 2048");
        assertEquals(1024, img1.getHeight(), "P1 atlas height must be 1024");

        BufferedImage img2 = ImageIO.read(p2Tex.toFile());
        assertNotNull(img2, "P2 texture must be a valid readable image");
        assertEquals(2048, img2.getWidth(), "P2 atlas width must be 2048");
        assertEquals(1024, img2.getHeight(), "P2 atlas height must be 1024");
    }

    @Test
    @DisplayName("Verify Phase 1 UV mapping: body/pupil/iris in left half, stalk in top-right, glass in bottom-right")
    void testPhase1UvMappingSanity() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        assertTrue(file.exists(), "P1 skin file must exist");

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p1");
        }
        assertNotNull(meshData);

        int totalVerts = 0;
        for (TerraSkinnedMeshData.PartData part : meshData.getParts()) {
            totalVerts += part.vertexCount;
            float[] uvs = part.uvs;
            assertEquals(part.vertexCount * 2, uvs.length, "UV length must be vertexCount * 2");

            float minU = Float.POSITIVE_INFINITY, maxU = Float.NEGATIVE_INFINITY;
            float minV = Float.POSITIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;

            for (int i = 0; i < uvs.length; i += 2) {
                float u = uvs[i];
                float v = uvs[i + 1];

                assertFalse(Float.isNaN(u) || Float.isInfinite(u), "U must be finite");
                assertFalse(Float.isNaN(v) || Float.isInfinite(v), "V must be finite");
                assertTrue(u >= 0.0f && u <= 1.0f, "U must be in [0, 1]: " + u);
                assertTrue(v >= 0.0f && v <= 1.0f, "V must be in [0, 1]: " + v);

                if (u < minU) minU = u;
                if (u > maxU) maxU = u;
                if (v < minV) minV = v;
                if (v > maxV) maxV = v;
            }

            switch (part.name) {
                case "body", "pupil", "iris" -> {
                    assertTrue(maxU <= 0.501f, part.name + " must be in left half (U <= 0.5), found maxU=" + maxU);
                    assertTrue(maxV <= 1.001f, part.name + " V must be <= 1.0, found maxV=" + maxV);
                }
                case "stalk" -> {
                    assertTrue(minU >= 0.499f, "Stalk must be in right half (U >= 0.5), found minU=" + minU);
                    assertTrue(maxV <= 0.501f, "Stalk must be in top-right quadrant (V <= 0.5), found maxV=" + maxV);
                }
                case "glass" -> {
                    assertTrue(minU >= 0.499f, "Glass must be in right half (U >= 0.5), found minU=" + minU);
                    assertTrue(minV >= 0.499f, "Glass must be in bottom-right quadrant (V >= 0.5), found minV=" + minV);
                }
                default -> fail("Unexpected part in Phase 1: " + part.name);
            }
        }

        assertTrue(totalVerts > 500, "Phase 1 must maintain clean non-empty geometry, got: " + totalVerts);
    }

    @Test
    @DisplayName("Verify Phase 2 UV mapping: body in left half, stalk in top-right, teeth and inner_teeth in bottom-right")
    void testPhase2UvMappingSanity() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists(), "P2 skin file must exist");

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p2");
        }
        assertNotNull(meshData);

        int totalVerts = 0;
        for (TerraSkinnedMeshData.PartData part : meshData.getParts()) {
            totalVerts += part.vertexCount;
            float[] uvs = part.uvs;
            assertEquals(part.vertexCount * 2, uvs.length, "UV length must be vertexCount * 2");

            float minU = Float.POSITIVE_INFINITY, maxU = Float.NEGATIVE_INFINITY;
            float minV = Float.POSITIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;

            for (int i = 0; i < uvs.length; i += 2) {
                float u = uvs[i];
                float v = uvs[i + 1];

                assertFalse(Float.isNaN(u) || Float.isInfinite(u), "U must be finite");
                assertFalse(Float.isNaN(v) || Float.isInfinite(v), "V must be finite");
                assertTrue(u >= 0.0f && u <= 1.0f, "U must be in [0, 1]: " + u);
                assertTrue(v >= 0.0f && v <= 1.0f, "V must be in [0, 1]: " + v);

                if (u < minU) minU = u;
                if (u > maxU) maxU = u;
                if (v < minV) minV = v;
                if (v > maxV) maxV = v;
            }

            switch (part.name) {
                case "body" -> {
                    assertTrue(maxU <= 0.501f, "Phase 2 body must be in left half (U <= 0.5), found maxU=" + maxU);
                    assertTrue(maxV <= 1.001f, "Phase 2 body V must be <= 1.0, found maxV=" + maxV);
                }
                case "stalk" -> {
                    assertTrue(minU >= 0.499f, "Stalk must be in right half (U >= 0.5), found minU=" + minU);
                    assertTrue(maxV <= 0.501f, "Stalk must be in top-right quadrant (V <= 0.5), found maxV=" + maxV);
                }
                case "teeth", "inner_teeth" -> {
                    assertTrue(minU >= 0.499f, part.name + " must be in right half (U >= 0.5), found minU=" + minU);
                    assertTrue(minV >= 0.499f, part.name + " must be in bottom-right quadrant (V >= 0.5), found minV=" + minV);
                }
                default -> fail("Unexpected part in Phase 2: " + part.name);
            }
        }

        assertTrue(totalVerts > 500, "Phase 2 must maintain clean non-empty geometry, got: " + totalVerts);
    }
}
