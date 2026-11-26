package com.fakeelytra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * All the logic for the fake elytra. Everything here only touches what YOUR client draws:
 * the server never hears about it, your movement is not changed, and no item is created.
 */
public final class FakeElytra {
    /** How many ticks you have between the two jump presses (vanilla creative-flight uses 7). */
    private static final int DOUBLE_TAP_WINDOW = 7;

    // Wing angles, same targets vanilla uses (radians)
    private static final float IDLE_X = 0.2617994F;     // 15 degrees
    private static final float IDLE_Z = -0.2617994F;
    private static final float CROUCH_X = 0.6981317F;
    private static final float CROUCH_Y = 0.08726646F;
    private static final float CROUCH_Z = -0.7853982F;
    private static final float GLIDE_X = 0.34906584F;   // 20 degrees
    private static final float GLIDE_Z = -1.5707964F;   // wings fully spread

    private static ItemStack elytraStack;

    private static boolean gliding = false;
    private static int glideTicks = 0;
    private static int tapTimer = 0;

    private static float rotX = IDLE_X, rotY = 0F, rotZ = IDLE_Z;
    private static float prevRotX = IDLE_X, prevRotY = 0F, prevRotZ = IDLE_Z;

    private FakeElytra() {}

    private static ItemStack elytra() {
        if (elytraStack == null) {
            elytraStack = new ItemStack(Items.ELYTRA);
        }
        return elytraStack;
    }

    public static boolean isWearingCopperChestplate(LocalPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(Items.COPPER_CHESTPLATE);
    }

    private static boolean canStartGliding(LocalPlayer player) {
        return isWearingCopperChestplate(player)
                && !player.onGround()
                && !player.isInWater()
                && !player.isInLava()
                && !player.isPassenger()
                && !player.onClimbable()
                && !player.getAbilities().flying
                && !player.isFallFlying(); // already on a real elytra
    }

    private static boolean shouldStopGliding(LocalPlayer player) {
        return !isWearingCopperChestplate(player)
                || player.onGround()
                || player.isInWater()
                || player.isInLava()
                || player.isPassenger()
                || player.onClimbable()
                || player.getAbilities().flying
                || player.isRemoved();
    }

    private static void startGliding(Minecraft mc, LocalPlayer player) {
        gliding = true;
        glideTicks = 0;
        mc.getSoundManager().play(new FakeElytraSound(player));
    }

    private static void stopGliding() {
        gliding = false;
        glideTicks = 0;
    }

    public static boolean isGliding() {
        return gliding;
    }

    /** Called once per client tick (20 times a second). */
    public static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            stopGliding();
            tapTimer = 0;
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        // Count jump presses since last tick. Vanilla never uses these clicks for jumping
        // (it reads whether the key is held), so draining them doesn't affect normal jumps.
        int presses = 0;
        while (mc.options.keyJump.consumeClick()) {
            presses++;
        }
        if (mc.screen != null) {
            presses = 0;
        }

        if (tapTimer > 0) {
            tapTimer--;
        }
        for (int i = 0; i < presses; i++) {
            if (tapTimer > 0) {
                tapTimer = 0;
                onDoubleTap(mc, player);
            } else {
                tapTimer = DOUBLE_TAP_WINDOW;
            }
        }

        if (gliding) {
            if (shouldStopGliding(player)) {
                stopGliding();
            } else {
                glideTicks++;
            }
        }

        tickWingAnimation(player);
    }

    private static void onDoubleTap(Minecraft mc, LocalPlayer player) {
        if (gliding) {
            stopGliding();
        } else if (canStartGliding(player)) {
            startGliding(mc, player);
        }
    }

    private static void tickWingAnimation(LocalPlayer player) {
        prevRotX = rotX;
        prevRotY = rotY;
        prevRotZ = rotZ;

        float targetX = IDLE_X, targetY = 0F, targetZ = IDLE_Z;
        if (gliding) {
            targetX = GLIDE_X;
            targetZ = GLIDE_Z;
        } else if (player.isCrouching()) {
            targetX = CROUCH_X;
            targetY = CROUCH_Y;
            targetZ = CROUCH_Z;
        }

        rotX += (targetX - rotX) * 0.3F;
        rotY += (targetY - rotY) * 0.3F;
        rotZ += (targetZ - rotZ) * 0.3F;
    }

    /**
     * Edits the render state of your own player right before it is drawn.
     * This is the only place the "elytra" exists: it's purely a drawing trick.
     */
    public static void applyToRenderState(HumanoidRenderState state, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !isWearingCopperChestplate(player)) {
            return;
        }

        // Swap the chestplate for an elytra in what gets drawn only.
        // (The armor layer draws nothing for an elytra, the wings layer draws the wings.)
        state.chestEquipment = elytra();

        state.elytraRotX = Mth.lerp(partialTick, prevRotX, rotX);
        state.elytraRotY = Mth.lerp(partialTick, prevRotY, rotY);
        state.elytraRotZ = Mth.lerp(partialTick, prevRotZ, rotZ);

        if (gliding) {
            state.isFallFlying = true;
            state.isCrouching = false;
            if (state instanceof AvatarRenderState avatar) {
                // Drives how far the body tilts into the glide (ramps up over ~10 ticks)
                avatar.fallFlyingTimeInTicks = glideTicks + partialTick;
                avatar.shouldApplyFlyingYRot = false;
            }
        }
    }
}
