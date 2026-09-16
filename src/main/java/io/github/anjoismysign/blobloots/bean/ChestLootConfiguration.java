package io.github.anjoismysign.blobloots.bean;

public class ChestLootConfiguration {
    private long cooldown;
    private boolean dropOnGround;
    private boolean global;
    private HologramConfiguration hologram;

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isDropOnGround() {
        return dropOnGround;
    }

    public void setDropOnGround(boolean dropOnGround) {
        this.dropOnGround = dropOnGround;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public HologramConfiguration getHologram() {
        return hologram;
    }

    public void setHologram(HologramConfiguration hologram) {
        this.hologram = hologram;
    }
}
