package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.datagen.tag.TagEnchantment;
import net.minecraft.core.Holder;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public class ItemDamageableBauble extends Item {
    public ItemDamageableBauble(int durability) {
        super((new Properties()).durability(durability).setNoRepair());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        // 检查是否含有不兼容附魔, 注册表在 TagEnchantment 中。此处应当仅有mending
        return super.supportsEnchantment(stack, enchantment) && !enchantment.is(TagEnchantment.BAUBLE_INCOMPATIBLE);
    }

}
