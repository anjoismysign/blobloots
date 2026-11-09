package io.github.anjoismysign.blobloots.configuration;

import io.github.anjoismysign.blobloots.BlobLoots;

public class LootsConfiguration {
    private boolean tinyDebug;

    public static LootsConfiguration getInstance(){
        return BlobLoots.getInstance().getManagerDirector().getConfigurationManager().getConfiguration();
    }

    public boolean isTinyDebug() {
        return tinyDebug;
    }

    public void setTinyDebug(boolean tinyDebug) {
        this.tinyDebug = tinyDebug;
    }
}
