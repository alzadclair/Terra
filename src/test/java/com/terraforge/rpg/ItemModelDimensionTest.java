package com.terraforge.rpg;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemModelDimensionTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/item");

    @Test
    @DisplayName("Verify all item model JSON files exist and are valid JSON")
    void testAllItemModelsValidJson() throws Exception {
        assertTrue(Files.exists(MODELS_DIR), "Item models directory must exist");

        File[] files = MODELS_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);
        assertTrue(files.length > 50, "Expected at least 50 item models, found: " + files.length);

        int validCount = 0;
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement element = JsonParser.parseReader(reader);
                assertTrue(element.isJsonObject(), "Model file " + file.getName() + " is not a JSON object");
                validCount++;
            }
        }

        assertEquals(files.length, validCount);
    }

    @Test
    @DisplayName("Audit 3D models vs 2D flat item models")
    void testAudit3DModels() throws Exception {
        File[] files = MODELS_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        List<String> models3D = new ArrayList<>();
        List<String> models2D = new ArrayList<>();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                String parent = obj.has("parent") ? obj.get("parent").getAsString() : "";

                // A model is considered 3D if it has custom geometry (elements/obj) or doesn't use minecraft:item/generated
                boolean isGenerated2D = "minecraft:item/generated".equals(parent) || "item/generated".equals(parent);
                if (!isGenerated2D || obj.has("elements")) {
                    models3D.add(file.getName());
                } else {
                    models2D.add(file.getName());
                }
            }
        }

        System.out.println("[ItemModelDimensionTest] Total items: " + files.length +
                " | 3D models: " + models3D.size() + " | 2D models: " + models2D.size());

        // Ensure key 3D weapons are recognized
        assertTrue(models3D.contains("zenith.json") || models3D.contains("terra_blade.json") ||
                   models3D.contains("megashark.json") || models3D.contains("vortex_beater.json") ||
                   models3D.contains("work_bench.json"), "Expected 3D weapons/blocks to have 3D models");
    }
}
