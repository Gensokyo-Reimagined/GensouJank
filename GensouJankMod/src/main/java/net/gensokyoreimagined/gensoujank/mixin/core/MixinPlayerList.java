package net.gensokyoreimagined.gensoujank.mixin.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerList.class)
public abstract class MixinPlayerList {

    @Inject(method = "canPlayerLogin", at = @At("HEAD"))
    private void onPlayerLogin(ServerLoginPacketListenerImpl loginlistener, GameProfile gameprofile, CallbackInfoReturnable<ServerPlayer> cir) {
        System.out.println("Player " + gameprofile.getName() + " is trying to log in!");
    }
}