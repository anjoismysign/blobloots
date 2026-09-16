package io.github.anjoismysign.blobloots;

import io.github.anjoismysign.bloblib.manager.BlobPlugin;
import io.github.anjoismysign.bloblib.manager.PluginManager;
import io.github.anjoismysign.bloblib.manager.asset.BukkitIdentityManager;
import io.github.anjoismysign.bloblib.utility.ListenerScanner;
import io.github.anjoismysign.blobloots.asset.Loot;
import io.github.anjoismysign.blobloots.command.BlobLootsCommand;
import io.github.anjoismysign.blobloots.configuration.LootsConfiguration;
import io.github.anjoismysign.blobloots.director.LootsManagerDirector;
import io.github.anjoismysign.blobloots.placeholderapi.BlobLootsPH;
import org.jetbrains.annotations.NotNull;

public class BlobLoots extends BlobPlugin{
    private static BlobLoots INSTANCE;

    public static BlobLoots getInstance() {
        return INSTANCE;
    }

    private LootsManagerDirector director;
    private BukkitIdentityManager<Loot> lootManager;

    @Override
    public void onEnable(){
        INSTANCE = this;
        director = new LootsManagerDirector(this);
        new BlobLootsPH(this);
        PluginManager pluginManager = PluginManager.getInstance();
        lootManager = pluginManager.addIdentityManager(Loot.Info.class, this, "loot", true);
        BlobLootsCommand.INSTANCE.load();
        ListenerScanner.scanAndRegister(this, "io.github.anjoismysign.blobloots.listener", () -> LootsConfiguration.getInstance().isTinyDebug());
    }

    @Override
    public @NotNull LootsManagerDirector getManagerDirector() {
        return director;
    }

    @NotNull
    public BukkitIdentityManager<Loot> getLootManager(){
        return lootManager;
    }
}
