package com.github.tartaricacid.touhoulittlemaid.datagen.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public class TagEnchantment extends EnchantmentTagsProvider {
    public static final TagKey<Enchantment> BAUBLE_INCOMPATIBLE =
            TagKey.create(Registries.ENCHANTMENT, getResourceLocation("bauble_incompatible"));

    public TagEnchantment(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, completableFuture, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BAUBLE_INCOMPATIBLE).add(Enchantments.MENDING);
    }
}
