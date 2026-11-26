package com.fakeelytra.mixin;

import com.fakeelytra.FakeElytra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the moment the game finishes collecting "how should this entity be drawn" info.
 * We only touch it when the entity is your own player.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(
            method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private void fakeElytra$afterExtract(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity != Minecraft.getInstance().player) {
            return;
        }
        if (cir.getReturnValue() instanceof HumanoidRenderState state) {
            FakeElytra.applyToRenderState(state, partialTick);
        }
    }
}
