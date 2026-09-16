package io.github.anjoismysign.blobloots.director.manager;

import com.google.gson.JsonSyntaxException;
import io.github.anjoismysign.bloblib.api.BlobLibHologramAPI;
import io.github.anjoismysign.bloblib.translatable.TranslatableBlock;
import io.github.anjoismysign.blobloots.asset.ChestLoot;
import io.github.anjoismysign.blobloots.asset.Loot;
import io.github.anjoismysign.blobloots.bean.HologramConfiguration;
import io.github.anjoismysign.blobloots.director.LootsManager;
import io.github.anjoismysign.blobloots.director.LootsManagerDirector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class ChestLootManager extends LootsManager {
    private static final BlobLibHologramAPI HOLOGRAM_API = BlobLibHologramAPI.getInstance();
    private final Set<String> holograms = new HashSet<>();
    private final Map<String, ChestLoot> chestLoots = new HashMap<>();

    private static final String LOOT_BLOCK = "ChestLoot.Loot";

    public ChestLootManager(LootsManagerDirector managerDirector) {
        super(managerDirector);
        reload();
    }

    @Override
    public void reload() {
        holograms.forEach(key->HOLOGRAM_API.getHologramDriver().remove(key));
        holograms.clear();

        var managerDirector = getManagerDirector();
        var operator = managerDirector.getPluginOperator();
        boolean debug = managerDirector.getConfigurationManager().getConfiguration().isTinyDebug();
        var plugin = getPlugin();
        File pluginDataFolder = plugin.getDataFolder();
        var logger = plugin.getLogger();
        chestLoots.clear();
        Path chestLootDirectory = pluginDataFolder.toPath().resolve("chestLoot");
        try {
            Files.createDirectories(chestLootDirectory);
        } catch (IOException exception) {
            logger.severe("Failed to create directory: " + exception.getMessage());
            return;
        }
        List<Path> jsonFiles;
        try (Stream<Path> stream = Files.walk(chestLootDirectory)) {
            jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .toList();
        } catch (IOException exception) {
            logger.severe("Failed to walk directory: " + exception.getMessage());
            return;
        }
        if (debug) {
            logger.info("ChestLoot JSON files found: " + jsonFiles.size());
        }
        for (Path path : jsonFiles) {
            File jsonFile = path.toFile();
            String jsonFilePath = jsonFile.getPath();
            ChestLoot chestLoot;
            try {
                chestLoot = ChestLoot.of(jsonFile);
            } catch (JsonSyntaxException exception) {
                logger.severe("Malformed JSON!\nAt: " + jsonFilePath + "\n" + exception.getMessage());
                continue;
            } catch (IOException exception) {
                logger.severe("Couldn't read file!\nAt: " + jsonFilePath + "\n" + exception.getMessage());
                continue;
            }
            if (debug) {
                logger.info("Loaded ChestLoot: " + jsonFilePath);
            }
            @Nullable ChestLoot previous = chestLoots.put(chestLoot.identifier(), chestLoot);
            if (previous != null) {
                String previousPath = previous.path().toString();
                logger.warning("Duplicate ChestLoot identifier '" + chestLoot.identifier() + "' at '" + previousPath + "', overriding with '" + jsonFilePath + "'");
            }
        }
        operator.runTask(()->{
           chestLoots.values().forEach(chestLoot -> {
               String identifier = chestLoot.identifier();
               @Nullable Loot loot = plugin.getLootManager().parse(identifier);
               if (loot == null){
                   logger.info("ChestLoot '"+identifier+"' is not linked to a Loot");
                   return;
               }
               if (!loot.chestLootConfiguration().getHologram().isEnabled()){
                   return;
               }
               chestLoot.blocks().forEach((worldKey,vectors)->{
                   @Nullable World world = Bukkit.getWorld(worldKey);
                   if (world == null){
                       return;
                   }
                   vectors.forEach(blockVector -> {
                       Location location = new Location(world, blockVector.getX(), blockVector.getY(), blockVector.getZ());
                       Block block = location.getBlock();
                       addHologram(block, loot);
                   });
               });
           });
        });
    }

    public void addHologram(@NotNull Block block,
                            @NotNull Loot loot) {
        HologramConfiguration configuration = loot.chestLootConfiguration().getHologram();
        Vector pivot = configuration.getPivot().toVector();
        String worldKey = block.getWorld().getKey().toString();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        TranslatableBlock translatableBlock = Objects.requireNonNull(TranslatableBlock.by(LOOT_BLOCK), "Not a TranslatableBlock '" + LOOT_BLOCK + "'");
        List<String> lines = translatableBlock.modder().replace("$chestLoot$", loot.identifier() + "_" + worldKey + "_" + x + "_" + y + "_" + z).get().get();
        UUID random = UUID.randomUUID();
        String key = random.toString();
        HOLOGRAM_API.getHologramDriver().create(key, block.getLocation().clone().add(pivot), lines, false);
        holograms.add(key);
    }

    @Nullable
    public ChestLoot getLinked(@NotNull Block block) {
        return chestLoots.values()
                .stream()
                .filter(chestLoot -> chestLoot.belongsTo(block))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public Map<String, ChestLoot> getChestLoots() {
        return Map.copyOf(chestLoots);
    }
}
