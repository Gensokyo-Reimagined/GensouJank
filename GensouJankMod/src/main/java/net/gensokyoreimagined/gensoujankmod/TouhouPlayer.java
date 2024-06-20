package net.gensokyoreimagined.gensoujankmod;

import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.util.UUID;


public class TouhouPlayer {
    public UUID uuid;

    public double adjustY = TouhouPlayers.defaultAdjustY;
    public boolean bossMode = false;
    public BukkitTask debugTask;
    public BoundingBox lastBoundingBox;
    public CraftBlockDisplay hitbox;

    public TouhouPlayer(UUID uuid) {
        this.uuid = uuid;
    }
}
