package net.gensokyoreimagined.gensoujankmod.mixin.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftBlockDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    /**
     * The Y coordinate of the player's hitbox is adjusted by this amount when boss mode is enabled.
     */
    @Unique
    private static double gensouJank$adjustY = 1.2;

    /**
     * Whether to enable the smaller hitbox.
     */
    @Unique
    private boolean gensouJank$bossMode = false;

    /**
     * The task upon debug mode to reveal the hitbox.
     */
    @Unique
    private BukkitTask gensouJank$debugTask = null;

    /**
     * The last bounding box for the hitbox, to interpolate from.
     */
    @Unique
    private BoundingBox gensouJank$lastBoundingBox = null;

    /**
     * The hitbox display for the player.
     */
    @Unique
    private CraftBlockDisplay gensouJank$hitbox = null;

    @Shadow
    public EntityDimensions getDimensions(Pose pose) {
        throw new RuntimeException("Mixin failed to shadow getDimensions");
    }

    @Shadow
    public Vec3 position() {
        throw new RuntimeException("Mixin failed to shadow position");
    }

    @Inject(method = "makeBoundingBox", at = @At("HEAD"), cancellable = true)
    protected void makeBoundingBox(CallbackInfoReturnable<AABB> cir) {
        if (gensouJank$bossMode) {
            cir.setReturnValue(getDimensions(Pose.SWIMMING).makeBoundingBox(position().add(0, gensouJank$adjustY, 0)));
            cir.cancel();
        }
    }
}