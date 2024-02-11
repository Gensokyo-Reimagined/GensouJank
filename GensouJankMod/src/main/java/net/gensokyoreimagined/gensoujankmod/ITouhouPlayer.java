package net.gensokyoreimagined.gensoujankmod;

import org.bukkit.craftbukkit.v1_20_R3.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

public interface ITouhouPlayer {

    public boolean isBossMode();
    public void setBossMode(boolean bossMode);

    public BukkitTask getDebugTask();
    public void setDebugTask(BukkitTask debugTask);

    public BoundingBox getLastBoundingBox();
    public void setLastBoundingBox(BoundingBox lastBoundingBox);

    public CraftBlockDisplay getHitboxDisplay();
    public void setHitboxDisplay(CraftBlockDisplay hitbox);

    public CraftPlayer getBukkitEntity();
}
