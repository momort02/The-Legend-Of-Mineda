package com.momort02.thelegendofmineda.item;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LegendarySwordCharge {
    public static final int CHARGE_TICKS = 3 * 20;
    public static final int COOLDOWN_TICKS = 10 * 20;
    private static final float WAVE_DAMAGE = 10.0F;
    private static final double WAVE_RANGE = 10.0;
    private static final double WAVE_HALF_WIDTH = 1.5;
    private static final double WAVE_HALF_HEIGHT = 1.5;

    private static final Map<UUID, Long> CHARGING_PLAYERS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private LegendarySwordCharge() {
    }

    public static void start(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        UUID playerId = player.getUUID();
        Long cooldownEnd = COOLDOWNS.get(playerId);
        if (cooldownEnd != null && cooldownEnd <= gameTime) {
            COOLDOWNS.remove(playerId);
            cooldownEnd = null;
        }

        if (cooldownEnd != null
                || !player.getMainHandItem().is(TheLegendOfMineda.LEGENDARY_SWORD.get())
                || player.getCooldowns().isOnCooldown(player.getMainHandItem())) {
            return;
        }

        CHARGING_PLAYERS.putIfAbsent(playerId, gameTime);
    }

    public static void cancel(Player player) {
        CHARGING_PLAYERS.remove(player.getUUID());
    }

    public static boolean isCharging(Player player) {
        return CHARGING_PLAYERS.containsKey(player.getUUID());
    }

    public static void tick(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Long chargeStart = CHARGING_PLAYERS.get(playerId);
        if (chargeStart == null) {
            return;
        }

        if (!player.getMainHandItem().is(TheLegendOfMineda.LEGENDARY_SWORD.get())
                || player.isDeadOrDying()
                || player.isSpectator()) {
            CHARGING_PLAYERS.remove(playerId);
            return;
        }

        long gameTime = player.level().getGameTime();
        if (gameTime - chargeStart < CHARGE_TICKS) {
            return;
        }

        CHARGING_PLAYERS.remove(playerId);
        COOLDOWNS.put(playerId, gameTime + COOLDOWN_TICKS);
        if (player.level() instanceof ServerLevel level) {
            launchWave(player, level);
        }
    }

    public static void removePlayer(Player player) {
        cancel(player);
    }

    private static void launchWave(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        Vec3 direction = new Vec3(look.x, 0.0, look.z);
        if (direction.lengthSqr() < 1.0E-6) {
            return;
        }
        direction = direction.normalize();

        Vec3 origin = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 end = origin.add(direction.scale(WAVE_RANGE));
        AABB searchBox = new AABB(
                Math.min(origin.x, end.x),
                Math.min(origin.y, end.y),
                Math.min(origin.z, end.z),
                Math.max(origin.x, end.x),
                Math.max(origin.y, end.y),
                Math.max(origin.z, end.z))
                .inflate(WAVE_HALF_WIDTH, WAVE_HALF_HEIGHT, WAVE_HALF_WIDTH);
        Vec3 waveDirection = direction;

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            Vec3 offset = target.getBoundingBox().getCenter().subtract(origin);
            double forwardDistance = offset.x * waveDirection.x + offset.z * waveDirection.z;
            double sideDistance = Math.abs(offset.x * waveDirection.z - offset.z * waveDirection.x);
            if (forwardDistance < 0.0
                    || forwardDistance > WAVE_RANGE
                    || sideDistance > WAVE_HALF_WIDTH
                    || Math.abs(offset.y) > WAVE_HALF_HEIGHT) {
                continue;
            }
            if (target instanceof Player otherPlayer && !player.canHarmPlayer(otherPlayer)) {
                continue;
            }

            target.hurtServer(level, level.damageSources().playerAttack(player), WAVE_DAMAGE);
        }
    }
}
