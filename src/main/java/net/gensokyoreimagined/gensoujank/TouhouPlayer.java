package net.gensokyoreimagined.gensoujank;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;


public class TouhouPlayer extends ServerPlayer {
    public boolean bossMode = false;
    public double adjustY = 1.2;
    public BukkitTask debugTask = null;

    public TouhouPlayer(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions) {
        super(server, world, profile, clientOptions);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return bossMode ? getDimensions(Pose.SWIMMING).makeBoundingBox(this.position().add(0, adjustY, 0)) : super.makeBoundingBox();
    }
}
