package com.momort02.thelegendofmineda.item;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class LegendarySwordItem extends Item {
    public static final int HITS_BEFORE_COOLDOWN = 100;
    public static final int COOLDOWN_TICKS = 5 * 60 * 20;
    private static final String HITS_DATA_KEY = "legendary_sword_hits";
    private static final int SHARPNESS_V_DAMAGE_BONUS = 3;

    public LegendarySwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(itemStack, target, attacker);

        int hits = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt(HITS_DATA_KEY)
                .orElse(0) + 1;

        if (hits >= HITS_BEFORE_COOLDOWN) {
            CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> tag.remove(HITS_DATA_KEY));
            if (attacker instanceof Player player) {
                player.getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS);
            }
        } else {
            int updatedHits = hits;
            CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> tag.putInt(HITS_DATA_KEY, updatedHits));
        }
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource damageSource) {
        if (damageSource.getEntity() instanceof Player player
                && player.getHealth() >= player.getMaxHealth()) {
            return SHARPNESS_V_DAMAGE_BONUS;
        }

        return super.getAttackDamageBonus(victim, damage, damageSource);
    }
}
