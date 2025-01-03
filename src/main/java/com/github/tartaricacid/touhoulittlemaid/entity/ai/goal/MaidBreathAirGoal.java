package com.github.tartaricacid.touhoulittlemaid.entity.ai.goal;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.SpawnParticleMessage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class MaidBreathAirGoal extends Goal {
    private static final int MAX_PROBABILITY = 5;
    private final EntityMaid maid;
    public MaidBreathAirGoal(EntityMaid pMob) {
        this.maid = pMob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (this.maid.isEatBreatheItem()) return false;

        if (this.maid.getAirSupply() < 140) {
            if (MobEffectUtil.hasWaterBreathing(this.maid)) {
                return false;
            }

            if (!this.maid.isUsingItem() && this.eatBreatheItem()) {
                return false;
            }

            if (this.hasDrownBauble()) {
                return false;
            }

            return true;
        }

        if (this.maid.isGoToBreathArea() && this.maid.getAirSupply() < this.maid.getMaxAirSupply()) {
            return true;
        }

        return false;
    }

    public boolean canContinueToUse() {
        return this.canUse();
    }

    public boolean isInterruptable() {
        return true;
    }

    public void start() {
    }

    private boolean hasDrownBauble() {
        BaubleItemHandler maidBauble = this.maid.getMaidBauble();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            if (maidBauble.getStackInSlot(i).is(InitItems.DROWN_PROTECT_BAUBLE.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean eatBreatheItem() {
        // 先查询手部的物品能否吃：能就直接开吃，否就进行后续工作
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = maid.getItemInHand(hand);

            if (itemInHand.isEmpty()) {
                continue;
            }

            if (this.isBreatheFood(itemInHand)) {
                this.startEatBreatheItem(maid, itemInHand, hand);
                return true;
            }

        }

        // 对手部进行处理：如果没有空的手部，那就取副手
        InteractionHand eanHand = InteractionHand.OFF_HAND;
        for (InteractionHand hand : InteractionHand.values()) {
            if (maid.getItemInHand(hand).isEmpty()) {
                eanHand = hand;
                break;
            }
        }
        ItemStack itemInHand = maid.getItemInHand(eanHand);

        // 尝试在背包中寻找食物放入
        boolean hasFood = false;
        RangedWrapper backpackInv = maid.getAvailableBackpackInv();
        for (int i = 0; i < backpackInv.getSlots(); i++) {
            ItemStack stack = backpackInv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (this.isBreatheFood(stack)) {
                ItemStack foodStack = stack.copy();
                ItemStack handStack = itemInHand.copy();
                maid.setItemInHand(eanHand, foodStack);
                backpackInv.setStackInSlot(i, ItemStack.EMPTY);
                ItemHandlerHelper.insertItemStacked(backpackInv, handStack, false);
                itemInHand = maid.getItemInHand(eanHand);
                hasFood = true;
                break;
            }

        }

        // 开吃
        if (hasFood) {
            this.startEatBreatheItem(maid, itemInHand, eanHand);
            return true;
        }

        return false;
    }

    private void startEatBreatheItem(EntityMaid maid, ItemStack stack, InteractionHand hand) {
        this.maid.setEatBreatheItem(true);

        FoodProperties foodProperties = stack.getFoodProperties(maid);
        int nutrition = 0;
        float saturationModifier = 0;
        float total = 0;
        if (foodProperties != null) {
            nutrition = foodProperties.getNutrition();
            saturationModifier = foodProperties.getSaturationModifier();
            total = nutrition + nutrition * saturationModifier * 2;
        }

        maid.startUsingItem(hand);
        // 原版的熟牛肉之类的一般在 20 左右（除了迷之炖菜为 34.2）
        if (maid.getRandom().nextInt(MAX_PROBABILITY) < total) {
            float healCount = Math.max(total / MAX_PROBABILITY, 1);
            maid.heal(healCount);
            NetworkHandler.sendToNearby(maid, new SpawnParticleMessage(maid.getId(), SpawnParticleMessage.Type.HEAL, stack.getUseDuration()));
        }
    }

    private boolean isBreatheFood(ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(stack);
            if (mobEffects.isEmpty()) return false;
            for (MobEffectInstance effect : mobEffects) {
                if (effect.getEffect() == MobEffects.WATER_BREATHING) {
                    return true;
                }
            }
            return false;
        }

        FoodProperties foodProperties = stack.getFoodProperties(this.maid);
        if (foodProperties == null) return false;
        List<Pair<MobEffectInstance, Float>> effects = foodProperties.getEffects();
        if (effects.isEmpty()) return false;
        for (Pair<MobEffectInstance, Float> effect : effects) {
            if (effect.getFirst().getEffect() == MobEffects.WATER_BREATHING) {
                return true;
            }
        }
        return false;
    }

    private void findAirPosition() {
        if (!this.maid.canBrainMoving()) return;

        final int offset = 16;
        Optional<BlockPos> match = BlockPos.findClosestMatch(this.maid.blockPosition(), offset, offset, this::givesAir);
        if (match.isPresent() && maid.canPathReach(match.get())) {
            this.maid.setGoToBreathArea(true);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, match.get(), 0.5f, 1);
            return;
        }

        BlockPos.MutableBlockPos seaLevelPos = this.maid.blockPosition().mutable().setY(this.maid.level.getSeaLevel() + 1);
        if (this.givesAir(seaLevelPos)) {
            this.maid.setGoToBreathArea(true);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, seaLevelPos, 0.5f, 1);
            return;
        }

        final int seaLevelOffset = 1;
        Iterable<BlockPos> canBreathPos = BlockPos.betweenClosed(seaLevelPos.getX() - seaLevelOffset, seaLevelPos.getY(), seaLevelPos.getZ() - seaLevelOffset,
                seaLevelPos.getX() + seaLevelOffset, seaLevelPos.getY(), seaLevelPos.getZ() + seaLevelOffset);
        for (BlockPos canBreathPo : canBreathPos) {
            if (this.givesAir(canBreathPo)) {
                this.maid.setGoToBreathArea(true);
                BehaviorUtils.setWalkAndLookTargetMemories(maid, canBreathPo, 0.5f, 1);
                return;
            }
        }
    }

    public void tick() {
        this.findAirPosition();
    }

    private boolean givesAir(BlockPos pPos) {
        Level level = this.maid.level;
        BlockState blockstate = level.getBlockState(pPos);
        return (level.getFluidState(pPos).isEmpty() || blockstate.is(Blocks.BUBBLE_COLUMN)) && blockstate.isPathfindable(level, pPos, PathComputationType.LAND);
    }

    @Override
    public void stop() {
        super.stop();
        if (this.maid.isGoToBreathArea()) {
            this.maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            this.maid.setGoToBreathArea(false);
        }
    }
}
