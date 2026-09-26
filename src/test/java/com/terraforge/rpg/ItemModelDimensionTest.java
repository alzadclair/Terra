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
    private static final Path TEXTURES_DIR = Path.of("src/main/resources/assets/terraforge_rpg/textures");
    private static final Path ASSETS_DIR = Path.of("src/main/resources/assets/terraforge_rpg");

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
    @DisplayName("Verify 0 items use minecraft:item/generated (zero 2D models in entire mod)")
    void testNoFlatGeneratedItemModels() throws Exception {
        File[] files = MODELS_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        List<String> models2D = new ArrayList<>();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                String parent = obj.has("parent") ? obj.get("parent").getAsString() : "";

                boolean isGenerated2D = "minecraft:item/generated".equals(parent) ||
                                        "item/generated".equals(parent) ||
                                        "minecraft:item/handheld".equals(parent);
                if (isGenerated2D && !obj.has("elements")) {
                    models2D.add(file.getName());
                }
            }
        }

        assertTrue(models2D.isEmpty(), "Found 2D generated item models without 3D elements: " + models2D);
    }

    @Test
    @DisplayName("Verify all item models have valid 3D geometry (elements, NeoForge OBJ, or 3D block parent)")
    void testAllItemModelsHave3DGeometry() throws Exception {
        File[] files = MODELS_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        List<String> invalidModels = new ArrayList<>();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

                boolean hasElements = obj.has("elements") && obj.getAsJsonArray("elements").size() > 0;
                boolean isObj = obj.has("loader") && "neoforge:obj".equals(obj.get("loader").getAsString());
                boolean hasBlockParent = obj.has("parent") && obj.get("parent").getAsString().contains("block/");

                if (isObj) {
                    String modelPath = obj.has("model") ? obj.get("model").getAsString() : "";
                    if (modelPath.startsWith("terraforge_rpg:")) {
                        String rel = modelPath.substring("terraforge_rpg:".length());
                        Path onDisk = ASSETS_DIR.resolve(rel);
                        if (!Files.exists(onDisk)) {
                            invalidModels.add(file.getName() + " -> missing OBJ file: " + onDisk);
                            continue;
                        }
                    }
                }

                if (!hasElements && !isObj && !hasBlockParent) {
                    invalidModels.add(file.getName() + " has neither elements, OBJ loader, nor block parent");
                }
            }
        }

        assertTrue(invalidModels.isEmpty(), "Item models without valid 3D geometry:\n" + String.join("\n", invalidModels));
    }

    @Test
    @DisplayName("Verify all referenced item textures exist on disk")
    void testAllReferencedItemTexturesExist() throws Exception {
        File[] files = MODELS_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        List<String> missingTextures = new ArrayList<>();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

                if (obj.has("textures") && obj.get("textures").isJsonObject()) {
                    JsonObject textures = obj.getAsJsonObject("textures");
                    for (String key : textures.keySet()) {
                        String texVal = textures.get(key).getAsString();
                        if (texVal.startsWith("terraforge_rpg:")) {
                            String rel = texVal.substring("terraforge_rpg:".length()) + ".png";
                            Path texPath = TEXTURES_DIR.resolve(rel);
                            if (!Files.exists(texPath)) {
                                missingTextures.add(file.getName() + " [" + key + "] -> " + rel);
                            }
                        }
                    }
                }
            }
        }

        assertTrue(missingTextures.isEmpty(), "Missing referenced item textures:\n" + String.join("\n", missingTextures));
    }
}
