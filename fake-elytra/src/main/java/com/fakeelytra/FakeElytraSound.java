package com.fakeelytra;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/** The elytra wind sound, played only on your client while the fake glide is active. */
public class FakeElytraSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private int time;

    public FakeElytraSound(LocalPlayer player) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        time++;
        if (player.isRemoved() || !FakeElytra.isGliding()) {
            this.stop();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();

        float speed = (float) player.getDeltaMovement().lengthSqr();
        float target = speed >= 1.0E-7F ? Mth.clamp(speed / 4.0F, 0.0F, 1.0F) : 0.0F;
        // Fade in over the first second like vanilla
        target *= Mth.clamp(time / 20.0F, 0.0F, 1.0F);
        this.volume = target;
        this.pitch = this.volume > 0.8F ? 1.0F + (this.volume - 0.8F) : 1.0F;
    }
}
