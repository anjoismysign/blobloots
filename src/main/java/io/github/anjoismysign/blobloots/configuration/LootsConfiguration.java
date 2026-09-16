package io.github.anjoismysign.blobloots.configuration;

import io.github.anjoismysign.blobloots.BlobLoots;

import java.io.File;

public class LootsConfiguration {
    private boolean tinyDebug;

    public static LootsConfiguration getInstance(){
        return BlobLoots.getInstance().getManagerDirector().getConfigurationManager().getConfiguration();
    }

    public static String getPath(){
        return new File(BlobLoots.getInstance().getDataFolder(), "config.yml").getPath();
    }

    public boolean isTinyDebug() {
        return tinyDebug;
    }

    public void setTinyDebug(boolean tinyDebug) {
        this.tinyDebug = tinyDebug;
    }
}
