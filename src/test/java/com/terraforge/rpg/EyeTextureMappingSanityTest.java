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
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EyeTextureMappingSanityTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");
    private static final Path TEXTURES_DIR = Path.of("src/main/resources/assets/terraforge_rpg/textures/entity/boss");
    private static final Path EYE_TEXTURES_DIR = TEXTURES_DIR.resolve("eye");

    @Test
    @DisplayName("Verify Eye of Cthulhu Phase 1 and Phase 2 textures exist on disk with valid dimensions")
    void testTextureAssetsExistAndHaveValidDimensions() throws Exception {
        // Fallback atlases
        Path p1Tex = TEXTURES_DIR.resolve("eye_of_cthulhu_p1.png");
        Path p2Tex = TEXTURES_DIR.resolve("eye_of_cthulhu_p2.png");
        assertTrue(Files.isRegularFile(p1Tex), "P1 fallback texture must exist: " + p1Tex);
        assertTrue(Files.isRegularFile(p2Tex), "P2 fallback texture must exist: " + p2Tex);

        // Per-part textures
        String[] p1Parts = {"p1_body.png", "p1_iris.png", "p1_pupil.png", "p1_stalk.png", "p1_glass.png"};
        for (String p : p1Parts) {
            Path path = EYE_TEXTURES_DIR.resolve(p);
            assertTrue(Files.isRegularFile(path), "P1 per-part texture must exist: " + path);
            BufferedImage img = ImageIO.read(path.toFile());
            assertNotNull(img, "Texture must be valid image: " + path);
            assertTrue(img.getWidth() > 0 && img.getHeight() > 0);
        }

        String[] p2Parts = {"p2_body.png", "p2_stalk.png", "p2_teeth.png", "p2_inner_teeth.png"};
        for (String p : p2Parts) {
            Path path = EYE_TEXTURES_DIR.resolve(p);
            assertTrue(Files.isRegularFile(path), "P2 per-part texture must exist: " + path);
            BufferedImage img = ImageIO.read(path.toFile());
            assertNotNull(img, "Texture must be valid image: " + path);
            assertTrue(img.getWidth() > 0 && img.getHeight() > 0);
        }
    }

    @Test
    @DisplayName("Verify Section 37 & 38: Phase 1 multi-primitive preservation and distinct material/texture mapping")
    void testPhase1MultiPrimitiveAndMaterialPreservation() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        assertTrue(file.exists(), "P1 skin file must exist");

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p1");
        }
        assertNotNull(meshData);

        // Section 37: source primitive count == exported primitive count (5 parts)
        assertEquals(5, meshData.getParts().size(), "Phase 1 must preserve all 5 source primitives/parts without silent loss");

        Set<String> partNames = new HashSet<>();
        int totalVerts = 0;

        for (TerraSkinnedMeshData.PartData part : meshData.getParts()) {
            partNames.add(part.name);
            totalVerts += part.vertexCount;
            assertTrue(part.vertexCount > 0, "Part " + part.name + " must have vertices");
            assertEquals(part.vertexCount * 2, part.uvs.length, "UV length must be vertexCount * 2");

            // Section 38: Verify distinct per-part texture
            assertNotNull(part.texture, "Part " + part.name + " must have a texture assigned");
            assertNotNull(part.textureLocation, "Part " + part.name + " must have a valid ResourceLocation");

            float[] uvs = part.uvs;
            for (int i = 0; i < uvs.length; i += 2) {
                float u = uvs[i];
                float v = uvs[i + 1];

                assertFalse(Float.isNaN(u) || Float.isInfinite(u), "U must be finite");
                assertFalse(Float.isNaN(v) || Float.isInfinite(v), "V must be finite");
                assertTrue(u >= -0.01f && u <= 1.01f, "U must be in [0, 1]: " + u);
                assertTrue(v >= -0.01f && v <= 1.01f, "V must be in [0, 1]: " + v);
            }
        }

        assertTrue(partNames.contains("stalk"), "Must contain stalk");
        assertTrue(partNames.contains("body"), "Must contain body");
        assertTrue(partNames.contains("pupil"), "Must contain pupil");
        assertTrue(partNames.contains("iris"), "Must contain iris");
        assertTrue(partNames.contains("glass"), "Must contain glass");
        assertTrue(totalVerts >= 4000, "Phase 1 non-LOD total vertices must be >= 4000, got: " + totalVerts);
    }

    @Test
    @DisplayName("Verify Section 37 & 38: Phase 2 multi-primitive preservation and distinct material/texture mapping")
    void testPhase2MultiPrimitiveAndMaterialPreservation() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists(), "P2 skin file must exist");

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p2");
        }
        assertNotNull(meshData);

        // Section 37: source primitive count == exported primitive count (4 parts)
        assertEquals(4, meshData.getParts().size(), "Phase 2 must preserve all 4 source primitives/parts without silent loss");

        Set<String> partNames = new HashSet<>();
        int totalVerts = 0;

        for (TerraSkinnedMeshData.PartData part : meshData.getParts()) {
            partNames.add(part.name);
            totalVerts += part.vertexCount;
            assertTrue(part.vertexCount > 0, "Part " + part.name + " must have vertices");
            assertEquals(part.vertexCount * 2, part.uvs.length, "UV length must be vertexCount * 2");

            assertNotNull(part.texture, "Part " + part.name + " must have a texture assigned");
            assertNotNull(part.textureLocation, "Part " + part.name + " must have a valid ResourceLocation");

            float[] uvs = part.uvs;
            for (int i = 0; i < uvs.length; i += 2) {
                float u = uvs[i];
                float v = uvs[i + 1];

                assertFalse(Float.isNaN(u) || Float.isInfinite(u), "U must be finite");
                assertFalse(Float.isNaN(v) || Float.isInfinite(v), "V must be finite");
                assertTrue(u >= -0.01f && u <= 1.01f, "U must be in [0, 1]: " + u);
                assertTrue(v >= -0.01f && v <= 1.01f, "V must be in [0, 1]: " + v);
            }
        }

        assertTrue(partNames.contains("stalk"), "Must contain stalk");
        assertTrue(partNames.contains("body"), "Must contain body");
        assertTrue(partNames.contains("teeth"), "Must contain teeth");
        assertTrue(partNames.contains("inner_teeth"), "Must contain inner_teeth");
        assertTrue(totalVerts >= 5000, "Phase 2 non-LOD total vertices must be >= 5000, got: " + totalVerts);
    }
}
