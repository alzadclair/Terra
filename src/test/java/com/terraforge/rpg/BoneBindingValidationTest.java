package com.terraforge.rpg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BoneBindingValidationTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");

    @Test
    @DisplayName("Verify Eye of Cthulhu Phase 2 has real vertex bindings for upper/lower jaws and all tendrils")
    void testPhase2JawsAndTendrilsBinding() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists(), "eye_of_cthulhu_p2.skin.json must exist");

        Map<String, Integer> boneVertexCounts = new HashMap<>();

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray bones = root.getAsJsonArray("bones");
            for (JsonElement b : bones) {
                boneVertexCounts.put(b.getAsString(), 0);
            }

            JsonArray parts = root.getAsJsonArray("parts");
            for (JsonElement partElem : parts) {
                JsonObject part = partElem.getAsJsonObject();
                int vc = part.get("vertexCount").getAsInt();
                JsonArray weights = part.getAsJsonArray("boneWeights");
                JsonArray indices = part.getAsJsonArray("boneIndices");

                for (int v = 0; v < vc; v++) {
                    for (int k = 0; k < 4; k++) {
                        float w = weights.get(v * 4 + k).getAsFloat();
                        int bIdx = indices.get(v * 4 + k).getAsInt();
                        if (w > 0.05f) {
                            String boneName = bones.get(bIdx).getAsString();
                            boneVertexCounts.put(boneName, boneVertexCounts.get(boneName) + 1);
                        }
                    }
                }
            }
        }

        // Assert crucial Phase 2 bones have non-trivial vertex influences
        assertTrue(boneVertexCounts.get("upper_jaw") > 500,
                "upper_jaw must influence > 500 vertices (found: " + boneVertexCounts.get("upper_jaw") + ")");
        assertTrue(boneVertexCounts.get("lower_jaw") > 500,
                "lower_jaw must influence > 500 vertices (found: " + boneVertexCounts.get("lower_jaw") + ")");
        assertTrue(boneVertexCounts.get("body") > 1000,
                "body must influence > 1000 vertices (found: " + boneVertexCounts.get("body") + ")");

        // All 6 tendrils must move actual geometry
        for (int i = 1; i <= 6; i++) {
            String tendril = "tendril_0" + i;
            assertTrue(boneVertexCounts.get(tendril) > 0,
                    tendril + " must influence at least 1 vertex (found: " + boneVertexCounts.get(tendril) + ")");
        }
    }

    @Test
    @DisplayName("Verify Eye of Cthulhu Phase 1 has real vertex bindings for pupil, iris, optic back, and tendrils")
    void testPhase1PupilIrisAndTendrilsBinding() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        assertTrue(file.exists(), "eye_of_cthulhu_p1.skin.json must exist");

        Map<String, Integer> boneVertexCounts = new HashMap<>();

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray bones = root.getAsJsonArray("bones");
            for (JsonElement b : bones) {
                boneVertexCounts.put(b.getAsString(), 0);
            }

            JsonArray parts = root.getAsJsonArray("parts");
            for (JsonElement partElem : parts) {
                JsonObject part = partElem.getAsJsonObject();
                int vc = part.get("vertexCount").getAsInt();
                JsonArray weights = part.getAsJsonArray("boneWeights");
                JsonArray indices = part.getAsJsonArray("boneIndices");

                for (int v = 0; v < vc; v++) {
                    for (int k = 0; k < 4; k++) {
                        float w = weights.get(v * 4 + k).getAsFloat();
                        int bIdx = indices.get(v * 4 + k).getAsInt();
                        if (w > 0.05f) {
                            String boneName = bones.get(bIdx).getAsString();
                            boneVertexCounts.put(boneName, boneVertexCounts.get(boneName) + 1);
                        }
                    }
                }
            }
        }

        assertTrue(boneVertexCounts.get("pupil") > 500,
                "pupil must influence > 500 vertices in Phase 1 (found: " + boneVertexCounts.get("pupil") + ")");
        assertTrue(boneVertexCounts.get("iris") > 100,
                "iris must influence > 100 vertices in Phase 1 (found: " + boneVertexCounts.get("iris") + ")");
        assertTrue(boneVertexCounts.get("optic_back") > 500,
                "optic_back must influence > 500 vertices in Phase 1 (found: " + boneVertexCounts.get("optic_back") + ")");

        for (int i = 1; i <= 6; i++) {
            String tendril = "tendril_0" + i;
            assertTrue(boneVertexCounts.get(tendril) > 0,
                    tendril + " must influence at least 1 vertex in Phase 1 (found: " + boneVertexCounts.get(tendril) + ")");
        }
    }
}
