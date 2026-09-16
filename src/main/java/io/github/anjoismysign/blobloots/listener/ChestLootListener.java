package io.github.anjoismysign.blobloots.listener;

import io.github.anjoismysign.bloblib.api.BlobLibLootAPI;
import io.github.anjoismysign.bloblib.message.BlobMessage;
import io.github.anjoismysign.bloblib.message.BlobSound;
import io.github.anjoismysign.bloblib.scheduler.BukkitPluginOperator;
import io.github.anjoismysign.bloblib.utility.TimeUtil;
import io.github.anjoismysign.blobloots.BlobLoots;
import io.github.anjoismysign.blobloots.asset.ChestLoot;
import io.github.anjoismysign.blobloots.asset.Loot;
import io.github.anjoismysign.blobloots.bean.ChestLootConfiguration;
import io.github.anjoismysign.blobloots.util.WandUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class ChestLootListener implements Listener {
    private static final BlobLoots PLUGIN = BlobLoots.getInstance();
    private static final Logger LOGGER = PLUGIN.getLogger();
    private static final BukkitPluginOperator OPERATOR = PLUGIN.getManagerDirector().getPluginOperator();
    private static final long MILLISECONDS_PER_TICK = 50L;
    private static final Map<NamespacedKey, Map<BlockVector, Long>> GLOBAL_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Map<NamespacedKey, Map<BlockVector, Long>>> COOLDOWN = new HashMap<>();

    private static final String ADDED_IDENTIFIER = "ChestLoot.Added";
    private static final String REMOVED_IDENTIFIER = "ChestLoot.Removed";
    private static final String DROP_SOUND = "ChestLoot.Drop";
    private static final String ON_COOLDOWN_IDENTIFIER = "ChestLoot.On-Cooldown";

    public static long getGlobalCooldown(@NotNull Block block){
        NamespacedKey worldKey = block.getWorld().getKey();
        @Nullable Map<BlockVector, Long> blocks = GLOBAL_COOLDOWN.get(worldKey);
        if (blocks == null){
            return -1;
        }
        BlockVector blockVector = block.getLocation().toVector().toBlockVector();
        @Nullable Long readyAt = blocks.get(blockVector);
        if (readyAt == null){
            return -1;
        }
        long now = Instant.now().toEpochMilli();
        if (now < readyAt){
            return readyAt - now;
        }
        blocks.remove(blockVector);
        if (blocks.isEmpty()){
            GLOBAL_COOLDOWN.remove(worldKey);
        }
        return -1;
    }

    private static void globalCooldown(@NotNull Block block,
                                       long cooldownInTicks){
        GLOBAL_COOLDOWN.computeIfAbsent(block.getWorld().getKey(), ignored -> new HashMap<>())
                .put(block.getLocation().toVector().toBlockVector(), readyAt(cooldownInTicks));
    }

    private static void removeGlobalCooldown(@NotNull Block block){
        NamespacedKey worldKey = block.getWorld().getKey();
        @Nullable Map<BlockVector, Long> blocks = GLOBAL_COOLDOWN.get(worldKey);
        if (blocks == null){
            return;
        }
        blocks.remove(block.getLocation().toVector().toBlockVector());
        if (blocks.isEmpty()){
            GLOBAL_COOLDOWN.remove(worldKey);
        }
    }

    public static long getCooldown(@NotNull Player player,
                                   @NotNull Block block){
        UUID uniqueId = player.getUniqueId();
        @Nullable Map<NamespacedKey, Map<BlockVector, Long>> worlds = COOLDOWN.get(uniqueId);
        if (worlds == null){
            return -1;
        }
        NamespacedKey worldKey = block.getWorld().getKey();
        @Nullable Map<BlockVector, Long> blocks = worlds.get(worldKey);
        if (blocks == null){
            return -1;
        }
        BlockVector blockVector = block.getLocation().toVector().toBlockVector();
        @Nullable Long readyAt = blocks.get(blockVector);
        if (readyAt == null){
            return -1;
        }
        long now = Instant.now().toEpochMilli();
        if (now < readyAt){
            return readyAt - now;
        }
        blocks.remove(blockVector);
        if (blocks.isEmpty()){
            worlds.remove(worldKey);
        }
        if (worlds.isEmpty()){
            COOLDOWN.remove(uniqueId);
        }
        return -1;
    }

    private static void cooldown(@NotNull Player player,
                                 @NotNull Block block,
                                 long cooldownInTicks){
        COOLDOWN.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>())
                .computeIfAbsent(block.getWorld().getKey(), ignored -> new HashMap<>())
                .put(block.getLocation().toVector().toBlockVector(), readyAt(cooldownInTicks));
    }

    private static void removeCooldown(@NotNull Player player,
                                       @NotNull Block block){
        UUID uniqueId = player.getUniqueId();
        @Nullable Map<NamespacedKey, Map<BlockVector, Long>> worlds = COOLDOWN.get(uniqueId);
        if (worlds == null){
            return;
        }
        NamespacedKey worldKey = block.getWorld().getKey();
        @Nullable Map<BlockVector, Long> blocks = worlds.get(worldKey);
        if (blocks == null){
            return;
        }
        blocks.remove(block.getLocation().toVector().toBlockVector());
        if (blocks.isEmpty()){
            worlds.remove(worldKey);
        }
        if (worlds.isEmpty()){
            COOLDOWN.remove(uniqueId);
        }
    }

    private static long readyAt(long cooldownInTicks){
        return Instant.now()
                .plusMillis(cooldownInTicks * MILLISECONDS_PER_TICK)
                .toEpochMilli();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if (event.getHand() != EquipmentSlot.HAND){
            return;
        }
        Action action = event.getAction();
        @Nullable Block block = event.getClickedBlock();
        if (block == null){
            return;
        }
        if (action != Action.RIGHT_CLICK_BLOCK){
            if (action != Action.LEFT_CLICK_BLOCK){
                return;
            }
            handleWand(event, block, false);
            return;
        }
        if (handleWand(event, block, true)){
            return;
        }
        @Nullable ChestLoot chestLoot = PLUGIN.getManagerDirector().getChestLootManager().getLinked(block);
        if (chestLoot == null){
            return;
        }
        String identifier = chestLoot.identifier();
        @Nullable Loot loot = PLUGIN.getLootManager().parse(identifier);
        if (loot == null){
            throw new IllegalStateException("ChestLoot '"+identifier+"' is not linked to a Loot by the same identifier!");
        }
        ChestLootConfiguration configuration = loot.chestLootConfiguration();
        Player player = event.getPlayer();
        String playerLocale = player.getLocale();
        final long globalCooldown = getGlobalCooldown(block);
        final long cooldown = globalCooldown == -1 ? getCooldown(player, block) : globalCooldown;
        if (cooldown != -1){
            Objects.requireNonNull(BlobMessage.by(ON_COOLDOWN_IDENTIFIER), "Not a BlobMessage '"+ON_COOLDOWN_IDENTIFIER+"'")
                    .localize(playerLocale)
                    .modder()
                    .replace("%time%", TimeUtil.INSTANCE.parseTime(playerLocale, cooldown))
                    .get()
                    .handle(player);
            return;
        }
        boolean dropOnGround = configuration.isDropOnGround();
        loot(block, identifier, player, dropOnGround);
        boolean isGlobal = configuration.isGlobal();
        long cooldownInTicks = configuration.getCooldown();
        if (isGlobal){
            globalCooldown(block, cooldownInTicks);
            OPERATOR.runTaskLater(()-> removeGlobalCooldown(block), cooldownInTicks);
            return;
        }
        cooldown(player, block, cooldownInTicks);
        OPERATOR.runTaskLater(()-> removeCooldown(player, block), cooldownInTicks);
    }

    private void loot(@NotNull Block block,
                      @NotNull String identifier,
                      @NotNull Player player,
                      boolean dropOnGround){
        String playerLocale = player.getLocale();
        Collection<ItemStack> itemBundle = BlobLibLootAPI.getInstance().generateLoot(identifier, playerLocale);
        if (!dropOnGround){
            player.give(itemBundle);
            return;
        }
        Location spawnLocation = block.getLocation().clone().add(0.5, 0.5, 0.5);
        World world = spawnLocation.getWorld();
        int delay = 0;
        for (ItemStack itemStack : itemBundle) {
            if (itemStack == null) {
                continue;
            }
            delay++;
            OPERATOR.runTaskLater(()->{
                Item item = world.dropItemNaturally(spawnLocation, itemStack);
                item.setVelocity(randomPopVelocity());
                BlobSound sound = Objects.requireNonNull(BlobSound.by(DROP_SOUND), "Not a BlobSound '"+DROP_SOUND+"'");
                sound.playInWorld(spawnLocation);
            }, delay + 3);
        }
    }

    private Vector randomPopVelocity() {
        double x = (Math.random() - 0.5) * 0.4;
        double z = (Math.random() - 0.5) * 0.4;
        double y = 0.2 + Math.random() * 0.2;
        return new Vector(x, y, z);
    }

    private boolean handleWand(PlayerInteractEvent event,
                               Block block,
                               boolean isAdd){
        if (event.isCancelled()){
            return false;
        }
        @Nullable ItemStack itemStack = event.getItem();
        if (itemStack == null){
            return false;
        }
        @Nullable ChestLoot chestLoot = WandUtil.INSTANCE.getLinkedChestLoot(itemStack);
        if (chestLoot == null){
            return false;
        }
        event.setCancelled(true);
        if (!chestLoot.add(block)){
            return false;
        }
        Player player = event.getPlayer();
        String messageIdentifier = isAdd ? ADDED_IDENTIFIER : REMOVED_IDENTIFIER;
        Objects.requireNonNull(BlobMessage.by(messageIdentifier), "Not a BlobMessage '"+messageIdentifier+"'")
                .localize(player.getLocale())
                .handle(player);
        return true;
    }
}
