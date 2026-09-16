package io.github.anjoismysign.blobloots.placeholderapi;

import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.placeholderapi.BlobPHExpansion;
import io.github.anjoismysign.bloblib.translatable.TranslatableSnippet;
import io.github.anjoismysign.bloblib.utility.TimeUtil;
import io.github.anjoismysign.blobloots.BlobLoots;
import io.github.anjoismysign.blobloots.asset.ChestLoot;
import io.github.anjoismysign.blobloots.listener.ChestLootListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlobLootsPH {
    private BlobPHExpansion expansion;
    private static final BlobLibTranslatableAPI API = BlobLibTranslatableAPI.getInstance();

    private static final String LOOTED_SNIPPET = "ChestLoot.Loot-Looted";
    private static final String RESTOCKED_SNIPPET = "ChestLoot.Loot-Restocked";

    public BlobLootsPH(@NotNull BlobLoots plugin) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().info("PlaceholderAPI not found, not registering PhatLoots PlaceholderAPI expansion");
            return;
        }
        expansion = new BlobPHExpansion(plugin);
        expansion.putStartsWith("timeRemaining_", (offlinePlayer, identifier) -> {
            String[] split = identifier.split("_");
            if (split.length != 5) {
                return identifier;
            }
            Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (player == null) {
                return "Not online";
            }
            String chestLootName = split[0];
            @Nullable ChestLoot chestLoot = BlobLoots.getInstance().getManagerDirector().getChestLootManager().getChestLoots().get(chestLootName);
            if (chestLoot == null){
                return "Not a ChestLoot: "+chestLootName;
            }
            String worldKey = split[1];
            String[] worldKeySplit = worldKey.split(":");
            if (worldKeySplit.length != 2){
                return "Invalid World key: " + worldKey;
            }
            String namespace = worldKeySplit[0];
            String key = worldKeySplit[1];
            NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
            @Nullable World world = Bukkit.getWorld(namespacedKey);
            if (world == null){
                return "Not a world: "+worldKey;
            }
            int x = Integer.parseInt(split[2]);
            int y = Integer.parseInt(split[3]);
            int z = Integer.parseInt(split[4]);
            Location location = new Location(world, x, y, z);
            Block block = location.getBlock();
            long remainingMillis = getTimeRemaining(player, block);
            String playerLocale = player.getLocale();
            String timeRemaining = TimeUtil.INSTANCE.parseTime(playerLocale, remainingMillis);
            final String snippetKey = remainingMillis > 0 ? LOOTED_SNIPPET : RESTOCKED_SNIPPET;
            @Nullable TranslatableSnippet snippet = API.getTranslatableSnippet(snippetKey, playerLocale);
            if (snippet == null){
                return "TranslatableSnippet '"+snippetKey+"' not found. Is BlobLib reloading?";
            }
            return snippet.modder()
                    .replace("%time%", timeRemaining)
                    .get()
                    .get();
        });
    }

    private long getTimeRemaining(@NotNull Player player,
                                  @NotNull Block block){
        final long globalCooldown = ChestLootListener.getGlobalCooldown(block);
        return globalCooldown == -1 ? ChestLootListener.getCooldown(player, block) : globalCooldown;
    }
}
