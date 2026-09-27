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

import static org.junit.jupiter.api.Assertions.*;

public class SkinWeightsValidationTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");

    @Test
    @DisplayName("Verify Eye of Cthulhu skin weights are normalized (sum ~ 1.0) and indices within bounds")
    void testSkinWeightsNormalized() throws Exception {
        String[] skinFiles = {"eye_of_cthulhu_p1.skin.json", "eye_of_cthulhu_p2.skin.json"};

        for (String skinFilename : skinFiles) {
            File skinFile = MODELS_DIR.resolve(skinFilename).toFile();
            assertTrue(skinFile.exists(), "Skin file must exist: " + skinFilename);

            try (FileReader reader = new FileReader(skinFile)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray bones = root.getAsJsonArray("bones");
                assertNotNull(bones);
                int boneCount = bones.size();
                assertTrue(boneCount >= 10, "Expected at least 10 canonical bones, found: " + boneCount);

                JsonArray parts = root.getAsJsonArray("parts");
                assertNotNull(parts);
                assertTrue(parts.size() > 0, "Skin must contain at least 1 mesh part");

                int totalVertsChecked = 0;

                for (JsonElement partElem : parts) {
                    JsonObject part = partElem.getAsJsonObject();
                    int vertexCount = part.get("vertexCount").getAsInt();
                    JsonArray weights = part.getAsJsonArray("boneWeights");
                    JsonArray indices = part.getAsJsonArray("boneIndices");

                    assertEquals(vertexCount * 4, weights.size(), "Weights array length must be vertexCount * 4");
                    assertEquals(vertexCount * 4, indices.size(), "Indices array length must be vertexCount * 4");

                    for (int v = 0; v < vertexCount; v++) {
                        float sum = 0.0f;
                        for (int k = 0; k < 4; k++) {
                            float w = weights.get(v * 4 + k).getAsFloat();
                            int bIdx = indices.get(v * 4 + k).getAsInt();

                            assertTrue(w >= 0.0f && w <= 1.0001f,
                                    "Weight out of [0, 1] range: " + w + " in " + skinFilename + " vertex " + v);
                            assertTrue(bIdx >= 0 && bIdx < boneCount,
                                    "Bone index out of bounds: " + bIdx + " (boneCount=" + boneCount + ") in " + skinFilename);
                            sum += w;
                        }

                        assertEquals(1.0f, sum, 0.02f,
                                "Weights must sum to 1.0 at vertex " + v + " in " + skinFilename);
                        totalVertsChecked++;
                    }
                }

                assertTrue(totalVertsChecked > 500, "Expected valid non-empty geometry in " + skinFilename + ", checked: " + totalVertsChecked);
            }
        }
    }
}
