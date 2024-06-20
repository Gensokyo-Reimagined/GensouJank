package net.gensokyoreimagined.gensoujankmod.mixin.core;

import net.gensokyoreimagined.gensoujankmod.TouhouPlayer;
import net.gensokyoreimagined.gensoujankmod.TouhouPlayers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinServerPlayer {
    @Unique
    private TouhouPlayer gensouJank$instance;

    @Inject(method = "makeBoundingBox", at = @At("HEAD"), cancellable = true)
    private void makeBoundingBox(CallbackInfoReturnable<AABB> cir) {
        var entity = (Entity) (Object) this;

        if (entity instanceof ServerPlayer player) {
            var uuid = player.getUUID();

            if (gensouJank$instance == null) {
                Bukkit.getLogger().info("[GensouJank] Creating new TouhouPlayer instance for " + uuid + "...");
                gensouJank$instance = TouhouPlayers.players.computeIfAbsent(uuid, o -> new TouhouPlayer(uuid));
                TouhouPlayers.players.put(uuid, gensouJank$instance);
            } else if (!gensouJank$instance.uuid.equals(uuid)) {
                Bukkit.getLogger().info("[GensouJank] Updating TouhouPlayer instance " + gensouJank$instance.uuid + " -> " + uuid + "...");
                TouhouPlayers.players.remove(gensouJank$instance.uuid);
                TouhouPlayers.players.put(uuid, gensouJank$instance);
                gensouJank$instance.uuid = uuid;
            }

            if (gensouJank$instance.bossMode) {
                var boundingBox = entity.getDimensions(Pose.SWIMMING).makeBoundingBox(entity.position().add(0, gensouJank$instance.adjustY, 0));
                cir.setReturnValue(boundingBox);
                cir.cancel();
            }
        }
    }
}
