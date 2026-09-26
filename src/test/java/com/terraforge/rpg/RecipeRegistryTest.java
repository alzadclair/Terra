package com.terraforge.rpg;

import com.google.gson.JsonArray;
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

public class RecipeRegistryTest {

    private static final Path RECIPES_DIR = Path.of("src/main/resources/data/terraforge_rpg/recipe");

    @Test
    @DisplayName("Verify all recipe JSON files parse cleanly and have valid result definitions")
    void testAllRecipesValid() throws Exception {
        assertTrue(Files.exists(RECIPES_DIR), "Recipes directory must exist");

        File[] files = RECIPES_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Expected at least 1 recipe file, found: " + files.length);

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject recipe = JsonParser.parseReader(reader).getAsJsonObject();
                assertTrue(recipe.has("type"), "Recipe " + file.getName() + " missing 'type'");

                // Verify result format (Minecraft 1.21 uses 'result.id' or 'result.item' or string 'result')
                if (recipe.has("result")) {
                    JsonElement resultElem = recipe.get("result");
                    String resultId = null;
                    if (resultElem.isJsonObject()) {
                        JsonObject resObj = resultElem.getAsJsonObject();
                        if (resObj.has("id")) {
                            resultId = resObj.get("id").getAsString();
                        } else if (resObj.has("item")) {
                            resultId = resObj.get("item").getAsString();
                        }
                    } else if (resultElem.isJsonPrimitive()) {
                        resultId = resultElem.getAsString();
                    }

                    assertNotNull(resultId, "Recipe " + file.getName() + " result has no identifiable ID");
                    assertTrue(resultId.contains(":"), "Result ID must be namespaced: " + resultId);
                }
            }
        }
    }

    @Test
    @DisplayName("Verify ingredient IDs are namespaced and valid")
    void testIngredientIdsValid() throws Exception {
        File[] files = RECIPES_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject recipe = JsonParser.parseReader(reader).getAsJsonObject();

                // Shapeless ingredients
                if (recipe.has("ingredients") && recipe.get("ingredients").isJsonArray()) {
                    JsonArray ingredients = recipe.getAsJsonArray("ingredients");
                    for (JsonElement ingElem : ingredients) {
                        if (ingElem.isJsonObject()) {
                            JsonObject ingObj = ingElem.getAsJsonObject();
                            if (ingObj.has("item")) {
                                String itemId = ingObj.get("item").getAsString();
                                assertTrue(itemId.contains(":"), "Ingredient item must be namespaced: " + itemId);
                            } else if (ingObj.has("tag")) {
                                String tagId = ingObj.get("tag").getAsString();
                                assertTrue(tagId.contains(":"), "Ingredient tag must be namespaced: " + tagId);
                            }
                        }
                    }
                }

                // Shaped ingredients (key map)
                if (recipe.has("key") && recipe.get("key").isJsonObject()) {
                    JsonObject keyMap = recipe.getAsJsonObject("key");
                    for (String key : keyMap.keySet()) {
                        JsonObject ingObj = keyMap.getAsJsonObject(key);
                        if (ingObj.has("item")) {
                            String itemId = ingObj.get("item").getAsString();
                            assertTrue(itemId.contains(":"), "Key ingredient item must be namespaced: " + itemId);
                        } else if (ingObj.has("tag")) {
                            String tagId = ingObj.get("tag").getAsString();
                            assertTrue(tagId.contains(":"), "Key ingredient tag must be namespaced: " + tagId);
                        }
                    }
                }
            }
        }
    }
}
