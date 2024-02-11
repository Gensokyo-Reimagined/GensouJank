package net.gensokyoreimagined.gensoujank;

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


public class TouhouPlayer extends ServerPlayer {
    /**
     * The Y coordinate of the player's hitbox is adjusted by this amount when boss mode is enabled.
     */
    public static double adjustY = 1.2;

    /**
     * Whether to enable the smaller hitbox.
     */
    public boolean bossMode = false;

    /**
     * The task upon debug mode to reveal the hitbox.
     */
    public BukkitTask debugTask = null;
    public BoundingBox lastBoundingBox = null;

    public CraftBlockDisplay hitbox = null;

    public TouhouPlayer(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions) {
        super(server, world, profile, clientOptions);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return bossMode ? getDimensions(Pose.SWIMMING).makeBoundingBox(this.position().add(0, adjustY, 0)) : super.makeBoundingBox();
    }
}
