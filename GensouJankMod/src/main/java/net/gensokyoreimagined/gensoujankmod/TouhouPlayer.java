package net.gensokyoreimagined.gensoujankmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftBlockDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;


public class TouhouPlayer extends ServerPlayer implements ITouhouPlayer {
    /**
     * The Y coordinate of the player's hitbox is adjusted by this amount when boss mode is enabled.
     */
    private static double adjustY = 1.2;
    public static void setAdjustY(double adjustY) { TouhouPlayer.adjustY = adjustY; }
    public void setAdjustYY(double adjustY) { TouhouPlayer.adjustY = adjustY; }

    /**
     * Whether to enable the smaller hitbox.
     */
    private boolean bossMode = false;
    public boolean isBossMode() { return bossMode; }
    public void setBossMode(boolean bossMode) { this.bossMode = bossMode; }

    /**
     * The task upon debug mode to reveal the hitbox.
     */
    private BukkitTask debugTask = null;
    public BukkitTask getDebugTask() { return debugTask; }
    public void setDebugTask(BukkitTask debugTask) { this.debugTask = debugTask; }

    private BoundingBox lastBoundingBox = null;
    public BoundingBox getLastBoundingBox() { return lastBoundingBox; }
    public void setLastBoundingBox(BoundingBox lastBoundingBox) { this.lastBoundingBox = lastBoundingBox; }

    private CraftBlockDisplay hitbox = null;
    public CraftBlockDisplay getHitboxDisplay() { return hitbox; }
    public void setHitboxDisplay(CraftBlockDisplay hitbox) { this.hitbox = hitbox; }

    public TouhouPlayer(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions) {
        super(server, world, profile, clientOptions);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return bossMode ? getDimensions(Pose.SWIMMING).makeBoundingBox(this.position().add(0, adjustY, 0)) : super.makeBoundingBox();
    }
}
