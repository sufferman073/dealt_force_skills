package com.rzy.dealt_force_skills.mixin.client;

import com.rzy.dealt_force_skills.client.character.ClientCharacterSkinState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerSkinMixin {
    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void dealt_force_skills$useSelectedCharacterSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientCharacterSkinState.skinFor(player).ifPresent(cir::setReturnValue);
    }
}
