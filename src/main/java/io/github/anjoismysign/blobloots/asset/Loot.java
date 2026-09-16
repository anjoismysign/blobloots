package io.github.anjoismysign.blobloots.asset;

import io.github.anjoismysign.blobloots.bean.ChestLootConfiguration;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.jetbrains.annotations.NotNull;

public record Loot(@NotNull String identifier,
                   @NotNull ChestLootConfiguration chestLootConfiguration) implements DataAsset {

    public static final class Info implements IdentityGenerator<Loot> {
        private ChestLootConfiguration chestLoot;

        @Override
        public @NotNull Loot generate(@NotNull String identifier) {
            return new Loot(identifier, chestLoot);
        }

        public ChestLootConfiguration getChestLoot() {
            return chestLoot;
        }

        public void setChestLoot(ChestLootConfiguration chestLoot) {
            this.chestLoot = chestLoot;
        }
    }
}
