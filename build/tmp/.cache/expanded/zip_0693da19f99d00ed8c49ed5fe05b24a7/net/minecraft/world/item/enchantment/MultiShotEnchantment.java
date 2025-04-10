package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MultiShotEnchantment extends Enchantment {
    public MultiShotEnchantment(Enchantment.Rarity p_45107_, EquipmentSlot... p_45108_) {
        super(p_45107_, EnchantmentCategory.CROSSBOW, p_45108_);
    }

    @Override
    public int getMinCost(int p_45111_) {
        return 20;
    }

    @Override
    public int getMaxCost(int p_45115_) {
        return 50;
    }

    @Override
    public boolean checkCompatibility(Enchantment p_45113_) {
        return super.checkCompatibility(p_45113_) && p_45113_ != Enchantments.PIERCING;
    }
}
