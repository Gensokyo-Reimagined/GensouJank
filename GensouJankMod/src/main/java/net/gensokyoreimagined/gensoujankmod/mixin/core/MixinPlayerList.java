package net.gensokyoreimagined.gensoujankmod.mixin.core;

import com.mojang.authlib.GameProfile;
import net.gensokyoreimagined.gensoujankmod.TouhouPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    @Redirect(method = "canPlayerLogin", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer onCanPlayerLogin(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions) {
        System.out.println("Substituted TouhouPlayer successfully!");
        return new TouhouPlayer(server, world, profile, clientOptions);
    }
}