package net.gensokyoreimagined.gensoujankmod.mixin.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vex.class)
public class MixinVex {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;tick()V"))
    private void tick(CallbackInfo ci) {
        ((Entity) (Object) this).noPhysics = false;
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
    private void populateDefaultEquipmentSlots(CallbackInfo ci) {
        ci.cancel();
    }
}
