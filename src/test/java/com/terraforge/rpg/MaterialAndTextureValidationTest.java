package com.terraforge.rpg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MaterialAndTextureValidationTest {

    private static final Path ASSETS_DIR = Path.of("src/main/resources/assets/terraforge_rpg");

    @Test
    @DisplayName("Verify zero absolute paths exist across any asset files (.mtl, .obj, .json, .skin.json)")
    void testNoAbsolutePathsInAssets() throws Exception {
        assertTrue(Files.exists(ASSETS_DIR), "Assets directory must exist");

        List<String> violations = new ArrayList<>();
        String[] forbiddenTokens = new String[]{"C:\\", "C:/", "c:\\", "c:/", "Users\\", "Users/", "Desktop\\", "Desktop/"};

        try (Stream<Path> stream = Files.walk(ASSETS_DIR)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".mtl") || name.endsWith(".obj") || name.endsWith(".json") || name.endsWith(".txt");
                    })
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            String line;
                            int lineNum = 1;
                            while ((line = reader.readLine()) != null) {
                                for (String token : forbiddenTokens) {
                                    if (line.contains(token)) {
                                        violations.add(path.getFileName() + ":" + lineNum + " contains '" + token + "'");
                                        break;
                                    }
                                }
                                lineNum++;
                            }
                        } catch (Exception e) {
                            violations.add("Failed to read " + path + ": " + e.getMessage());
                        }
                    });
        }

        assertTrue(violations.isEmpty(), "Found absolute paths in asset files:\n" + String.join("\n", violations));
    }

    @Test
    @DisplayName("Verify 100% of MTL map_Kd texture references resolve to existing PNGs on disk")
    void testAllMtlTexturesResolveOnDisk() throws Exception {
        List<String> missingTextures = new ArrayList<>();
        int checkedCount = 0;

        try (Stream<Path> stream = Files.walk(ASSETS_DIR)) {
            List<Path> mtlFiles = stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".mtl"))
                    .toList();

            assertFalse(mtlFiles.isEmpty(), "Expected to find MTL files");

            for (Path mtl : mtlFiles) {
                Path mtlDir = mtl.getParent();
                try (BufferedReader reader = Files.newBufferedReader(mtl)) {
                    String line;
                    int lineNum = 1;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("map_Kd") || line.startsWith("map_Ka") || line.startsWith("map_Ks")) {
                            String[] parts = line.split("\\s+", 2);
                            if (parts.length > 1) {
                                String texRef = parts[1].trim();
                                Path resolvedPath = mtlDir.resolve(texRef).normalize();
                                checkedCount++;

                                if (!Files.isRegularFile(resolvedPath)) {
                                    missingTextures.add(mtl.getFileName() + ":" + lineNum + " -> " + texRef + " (resolved to: " + resolvedPath + ")");
                                } else {
                                    // Case-sensitivity validation: real file name on disk must match exactly
                                    File realFile = resolvedPath.toFile();
                                    File canonicalFile = realFile.getCanonicalFile();
                                    if (!canonicalFile.getName().equals(realFile.getName())) {
                                        missingTextures.add("Case mismatch in " + mtl.getFileName() + " -> declared: " + realFile.getName() + ", on disk: " + canonicalFile.getName());
                                    }
                                }
                            }
                        }
                        lineNum++;
                    }
                }
            }
        }

        System.out.println("[MaterialAndTextureValidationTest] Validated " + checkedCount + " MTL texture references.");
        assertTrue(checkedCount > 15, "Expected at least 15 MTL texture references, found " + checkedCount);
        assertTrue(missingTextures.isEmpty(), "Missing textures referenced by MTL files:\n" + String.join("\n", missingTextures));
    }

    @Test
    @DisplayName("Verify all OBJ mtllib references exist on disk")
    void testAllObjMtllibReferencesExist() throws Exception {
        List<String> missingMtls = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(ASSETS_DIR)) {
            List<Path> objFiles = stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".obj"))
                    .toList();

            for (Path obj : objFiles) {
                Path objDir = obj.getParent();
                try (BufferedReader reader = Files.newBufferedReader(obj)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("mtllib ")) {
                            String mtlName = line.substring("mtllib ".length()).trim();
                            Path resolvedMtl = objDir.resolve(mtlName).normalize();
                            if (!Files.isRegularFile(resolvedMtl)) {
                                missingMtls.add(obj.getFileName() + " references missing mtllib: " + mtlName);
                            }
                        }
                    }
                }
            }
        }

        assertTrue(missingMtls.isEmpty(), "Missing mtllib references:\n" + String.join("\n", missingMtls));
    }
}
