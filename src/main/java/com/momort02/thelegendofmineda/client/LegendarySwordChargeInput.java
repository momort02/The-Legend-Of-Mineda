package com.momort02.thelegendofmineda.client;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import com.momort02.thelegendofmineda.network.LegendarySwordChargePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class LegendarySwordChargeInput {
    private static boolean charging;
    private static boolean chargeCompleted;
    private static long chargeStartedAt;
    private static long cooldownEndsAt;

    private LegendarySwordChargeInput() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean shouldCharge = player != null
                && minecraft.level != null
                && minecraft.gui.screen() == null
                && minecraft.options.keyAttack.isDown()
                && player.getMainHandItem().is(TheLegendOfMineda.LEGENDARY_SWORD.get())
                && !player.getCooldowns().isOnCooldown(player.getMainHandItem());

        long gameTime = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        if (!shouldCharge || (!charging && gameTime < cooldownEndsAt)) {
            stopCharging();
            return;
        }

        if (!charging) {
            charging = true;
            chargeStartedAt = gameTime;
            ClientPacketDistributor.sendToServer(new LegendarySwordChargePayload(true));
        } else if (!chargeCompleted && gameTime - chargeStartedAt >= 60) {
            chargeCompleted = true;
            cooldownEndsAt = gameTime + 10 * 20;
        }
    }

    public static float getChargeProgress(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!charging || minecraft.level == null) {
            return 0.0F;
        }
        float elapsedTicks = (float) (minecraft.level.getGameTime() - chargeStartedAt) + partialTick;
        return Math.clamp(elapsedTicks / 60.0F, 0.0F, 1.0F);
    }

    private static void stopCharging() {
        if (charging) {
            if (Minecraft.getInstance().getConnection() != null) {
                ClientPacketDistributor.sendToServer(new LegendarySwordChargePayload(false));
            }
        }
        charging = false;
        chargeCompleted = false;
        chargeStartedAt = 0L;
    }
}
