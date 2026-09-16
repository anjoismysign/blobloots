package io.github.anjoismysign.blobloots.director;

import io.github.anjoismysign.bloblib.manager.GenericManagerDirector;
import io.github.anjoismysign.blobloots.BlobLoots;
import io.github.anjoismysign.blobloots.director.manager.ChestLootManager;
import io.github.anjoismysign.blobloots.director.manager.LootsConfigurationManager;

public class LootsManagerDirector extends GenericManagerDirector<BlobLoots> {
    public LootsManagerDirector(BlobLoots plugin) {
        super(plugin);
        addManager("config", new LootsConfigurationManager(this));
        addManager("chestloot", new ChestLootManager(this));
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        getConfigurationManager().reload();
        getChestLootManager().reload();
    }

    @Override
    public void unload() {
    }

    public LootsConfigurationManager getConfigurationManager() {
        return getManager("config", LootsConfigurationManager.class);
    }

    public ChestLootManager getChestLootManager(){
        return getManager("chestloot", ChestLootManager.class);
    }
}