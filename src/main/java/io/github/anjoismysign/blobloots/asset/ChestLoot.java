package io.github.anjoismysign.blobloots.asset;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;
import io.github.anjoismysign.blobloots.BlobLoots;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record ChestLoot(@NotNull String identifier,
                        @NotNull Map<NamespacedKey, Set<BlockVector>> blocks,
                        @NotNull Path path) implements DataAsset {

    @NotNull
    public static ChestLoot of(@NotNull File jsonFile) throws IOException, JsonSyntaxException {
        String fileName = jsonFile.getName();
        if (!fileName.endsWith(".json")) {
            throw new ConfigurationFieldException("Not a JSON file!");
        }
        String identifier = fileName.replace(".json", "");
        JsonArray array;
        try (Reader reader = Files.newBufferedReader(jsonFile.toPath())) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                throw new ConfigurationFieldException("Root element is not a JSON array!");
            }
            array = root.getAsJsonArray();
        }
        Map<NamespacedKey, Set<BlockVector>> blocks = new HashMap<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new ConfigurationFieldException("'" + element + "' is not a String!");
            }
            String serialized = element.getAsString();
            String[] split = serialized.split("'");
            //noinspection ExtractMethodRecommender
            if (split.length != 4) {
                throw new ConfigurationFieldException("'" + serialized + "' needs to be split by \"'\" in 4 parts!");
            }
            String worldKey = split[0];
            if (!worldKey.contains(":")) {
                worldKey = "minecraft:" + worldKey;
            }
            String[] worldKeySplit = worldKey.split(":");
            if (worldKeySplit.length != 2) {
                throw new ConfigurationFieldException("'" + worldKey + "' is not a valid world key!");
            }
            String namespace = worldKeySplit[0];
            String key = worldKeySplit[1];
            NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
            int x = parseInt(split[1], serialized, "X");
            int y = parseInt(split[2], serialized, "Y");
            int z = parseInt(split[3], serialized, "Z");
            blocks.computeIfAbsent(namespacedKey, ignored -> new HashSet<>())
                    .add(new BlockVector(x, y, z));
        }
        return new ChestLoot(identifier, blocks, jsonFile.toPath());
    }

    private static int parseInt(@NotNull String input,
                                @NotNull String serialized,
                                @NotNull String axis) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException exception) {
            throw new ConfigurationFieldException("'" + serialized + "' has an invalid " + axis + " ('" + input + "'), needs to be an Integer!");
        }
    }

    private void serialize() {
        JsonArray array = new JsonArray();
        blocks().forEach((worldKey, blocks) -> blocks.forEach(blockVector -> {
            array.add(worldKey.asString()
                    + "'" + blockVector.getBlockX()
                    + "'" + blockVector.getBlockY()
                    + "'" + blockVector.getBlockZ());
        }));
        @Nullable Path parent = path().getParent();
        var logger = BlobLoots.getInstance().getLogger();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException exception) {
            logger.severe("Couldn't create parent directories for " + path);
        }
        try (Writer writer = Files.newBufferedWriter(path())) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(array, writer);
        } catch (JsonSyntaxException exception) {
            logger.severe("Couldn't write JSON!\nAt: "+path+"\n"+exception.getMessage());
        } catch (IOException exception) {
            logger.severe("Couldn't write file!\nAt: "+path+"\n"+exception.getMessage());
        }
    }

    public boolean belongsTo(@NotNull Block block) {
        World world = block.getWorld();
        NamespacedKey worldKey = world.getKey();
        BlockVector vector = vectorOf(block);
        @Nullable Set<BlockVector> blocks = blocks().get(worldKey);
        if (blocks == null) {
            return false;
        }
        return blocks.contains(vector);
    }

    @NotNull
    private static BlockVector vectorOf(@NotNull Block block) {
        return block.getLocation().toVector().toBlockVector();
    }

    public boolean remove(@NotNull Block block) {
        World world = block.getWorld();
        NamespacedKey worldKey = world.getKey();
        var logger = BlobLoots.getInstance().getLogger();
        @Nullable Set<BlockVector> blocks = blocks().get(worldKey);
        if (blocks == null) {
            logger.info("[remove] '" + identifier() + "' has no entry for world '" + worldKey + "'");
            return false;
        }
        boolean changed = blocks.remove(vectorOf(block));
        logger.info("[remove] '" + identifier() + "' " + worldKey + " " + vectorOf(block) + " changed=" + changed);
        if (changed) {
            serialize();
        }
        return changed;
    }

    public boolean add(@NotNull Block block) {
        World world = block.getWorld();
        NamespacedKey worldKey = world.getKey();
        var logger = BlobLoots.getInstance().getLogger();
        Set<BlockVector> blocks = blocks().computeIfAbsent(worldKey, ignored -> new HashSet<>());
        BlockVector vector = vectorOf(block);
        boolean changed = blocks.add(vector);
        logger.info("[add] '" + identifier() + "' " + worldKey + " " + vector + " changed=" + changed);
        if (changed) {
            serialize();
        }
        return changed;
    }


}
