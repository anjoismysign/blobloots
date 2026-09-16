package io.github.anjoismysign.blobloots.bean;

import io.github.anjoismysign.bloblib.domain.VectorBean;

public class HologramConfiguration {
    private boolean enabled;
    private VectorBean pivot;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public VectorBean getPivot() {
        return pivot;
    }

    public void setPivot(VectorBean pivot) {
        this.pivot = pivot;
    }
}
