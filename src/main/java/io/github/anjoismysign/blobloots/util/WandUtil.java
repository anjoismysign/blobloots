package io.github.anjoismysign.blobloots.util;

import io.github.anjoismysign.bloblib.translatable.TranslatableItem;
import io.github.anjoismysign.blobloots.BlobLoots;
import io.github.anjoismysign.blobloots.asset.ChestLoot;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum WandUtil {
    INSTANCE;

    private static final NamespacedKey WAND_NAMESPACED_KEY = new NamespacedKey(BlobLoots.getInstance(), "chestLootWand");

    @Nullable
    public ChestLoot getLinkedChestLoot(@NotNull ItemStack stack){
        ItemMeta itemMeta = stack.getItemMeta();
        var dataContainer = itemMeta.getPersistentDataContainer();
        @Nullable String identifier = dataContainer.get(WAND_NAMESPACED_KEY, PersistentDataType.STRING);
        if (identifier == null){
            return null;
        }
        @Nullable ChestLoot chestLoot = BlobLoots.getInstance().getManagerDirector().getChestLootManager().getChestLoots().get(identifier);
        return chestLoot;
    }

    public void giveChestLootWand(@NotNull Player player,
                                  @NotNull ChestLoot chestLoot){
        String identifier = chestLoot.identifier();
        ItemStack stack = Objects.requireNonNull(TranslatableItem.by("ChestLoot.Wand"), "Not a TranslatableItem 'ChestLoot.Wand'")
                .localize(player.getLocale())
                .modder()
                .replace("%identifier%", identifier)
                .get()
                .getClone();
        ItemMeta itemMeta = stack.getItemMeta();
        var dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(WAND_NAMESPACED_KEY, PersistentDataType.STRING, identifier);
        stack.setItemMeta(itemMeta);
        player.give(stack);
    }
}
