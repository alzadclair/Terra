package com.terraforge.rpg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeRegistryTest {

    private static final Path RECIPES_DIR = Path.of("src/main/resources/data/terraforge_rpg/recipe");
    private static Set<String> registeredTerraItems = new HashSet<>();

    @BeforeAll
    static void setup() {
        try {
            java.lang.reflect.Method ofMethod = Class.forName("net.neoforged.fml.loading.LoadingModList")
                    .getMethod("of", java.util.List.class, java.util.List.class, java.util.List.class, java.util.List.class, java.util.Map.class);
            ofMethod.invoke(null, java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of());
        } catch (Throwable ignored) {
        }
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            java.lang.reflect.Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.setBoolean(net.minecraft.core.registries.BuiltInRegistries.ITEM, false);
        } catch (Throwable ignored) {
        }

        // Collect registered TerraForge RPG items
        registeredTerraItems = ModItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::getId)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
    }

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
    @DisplayName("Resolve all recipe items against BuiltInRegistries.ITEM or ModItems, failing on non-existent items")
    void testAllRecipeItemsResolveAgainstRegistry() throws Exception {
        assertFalse(registeredTerraItems.isEmpty(), "ModItems registry entries must be loaded");

        File[] files = RECIPES_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        List<String> errors = new ArrayList<>();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject recipe = JsonParser.parseReader(reader).getAsJsonObject();

                // Check result
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

                    if (resultId != null) {
                        validateItemId(resultId, file.getName(), "result", errors);
                    }
                }

                // Check shapeless ingredients
                if (recipe.has("ingredients") && recipe.get("ingredients").isJsonArray()) {
                    JsonArray ingredients = recipe.getAsJsonArray("ingredients");
                    for (JsonElement ingElem : ingredients) {
                        if (ingElem.isJsonObject()) {
                            JsonObject ingObj = ingElem.getAsJsonObject();
                            if (ingObj.has("item")) {
                                String itemId = ingObj.get("item").getAsString();
                                validateItemId(itemId, file.getName(), "ingredient", errors);
                            }
                        }
                    }
                }

                // Check shaped ingredients
                if (recipe.has("key") && recipe.get("key").isJsonObject()) {
                    JsonObject keyMap = recipe.getAsJsonObject("key");
                    for (String key : keyMap.keySet()) {
                        JsonObject ingObj = keyMap.getAsJsonObject(key);
                        if (ingObj.has("item")) {
                            String itemId = ingObj.get("item").getAsString();
                            validateItemId(itemId, file.getName(), "key['" + key + "']", errors);
                        }
                    }
                }
            }
        }

        assertTrue(errors.isEmpty(), "Recipe registry resolution errors encountered:\n" + String.join("\n", errors));
    }

    private void validateItemId(String itemId, String filename, String context, List<String> errors) {
        if (itemId.startsWith("terraforge_rpg:")) {
            if (!registeredTerraItems.contains(itemId)) {
                errors.add("In recipe '" + filename + "' (" + context + "): Unregistered TerraForge item '" + itemId + "'");
            }
        } else {
            ResourceLocation rl = ResourceLocation.parse(itemId);
            if (!BuiltInRegistries.ITEM.containsKey(rl)) {
                errors.add("In recipe '" + filename + "' (" + context + "): Unknown Minecraft item '" + itemId + "'");
            }
        }
    }
}
