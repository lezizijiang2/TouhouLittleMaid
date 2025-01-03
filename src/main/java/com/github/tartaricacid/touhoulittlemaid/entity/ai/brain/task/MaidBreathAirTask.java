package com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.SpawnParticleMessage;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
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

import java.util.List;
import java.util.Optional;

public class MaidBreathAirTask extends Behavior<EntityMaid> {
    private static final int MAX_PROBABILITY = 5;
    public MaidBreathAirTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        this.findAirPosition(maid);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        if (maid.isGoToBreathArea()) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            maid.setGoToBreathArea(false);
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        // 正在食用可呼吸的食物
        if (maid.isEatBreatheItem()) return false;

        // 氧气值
        if (maid.getAirSupply() < 140) {
            // 拥有水下呼吸等效果
            if (MobEffectUtil.hasWaterBreathing(maid)) {
                return false;
            }

            // 没有在食用物品，并且在食用可提供呼吸效果的食物
            if (!maid.isUsingItem() && this.eatBreatheItem(maid)) {
                return false;
            }

            // 溺水保护饰品
            if (this.hasDrownBauble(maid)) {
                return false;
            }

            return true;
        }

        // 正在去往可呼吸新鲜空气的地方并且氧气值还没满
        if (maid.isGoToBreathArea() && maid.getAirSupply() < maid.getMaxAirSupply()) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return this.checkExtraStartConditions(level, maid);
    }

    private boolean hasDrownBauble(EntityMaid maid) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            if (maidBauble.getStackInSlot(i).is(InitItems.DROWN_PROTECT_BAUBLE.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean eatBreatheItem(EntityMaid maid) {
        // 先查询手部的物品能否吃：能就直接开吃，否就进行后续工作
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = maid.getItemInHand(hand);

            if (itemInHand.isEmpty()) {
                continue;
            }

            if (this.isBreatheFood(maid, itemInHand)) {
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
            if (this.isBreatheFood(maid, stack)) {
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
        maid.setEatBreatheItem(true);

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

    private boolean isBreatheFood(EntityMaid maid, ItemStack stack) {
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

        FoodProperties foodProperties = stack.getFoodProperties(maid);
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

    // 寻找可呼吸新鲜空气的地方
    private void findAirPosition(EntityMaid maid) {
        if (!maid.canBrainMoving()) return;

        // 周围16格以内
        final int offset = 16;
        Optional<BlockPos> match = BlockPos.findClosestMatch(maid.blockPosition(), offset, offset, blockPos -> this.givesAir(maid, blockPos));
        if (match.isPresent() && maid.canPathReach(match.get())) {
            maid.setGoToBreathArea(true);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, match.get(), 0.5f, 1);
            return;
        }

        // 当前女仆坐标的海平面位置
        BlockPos.MutableBlockPos seaLevelPos = maid.blockPosition().mutable().setY(maid.level.getSeaLevel() + 1);
        if (this.givesAir(maid, seaLevelPos) && maid.canPathReach(seaLevelPos)) {
            maid.setGoToBreathArea(true);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, seaLevelPos, 0.5f, 1);
            return;
        }

        // 当前女仆坐标的海平面3*3*1区域
        final int seaLevelOffset = 1;
        Iterable<BlockPos> canBreathPos = BlockPos.betweenClosed(seaLevelPos.getX() - seaLevelOffset, seaLevelPos.getY(), seaLevelPos.getZ() - seaLevelOffset,
                seaLevelPos.getX() + seaLevelOffset, seaLevelPos.getY(), seaLevelPos.getZ() + seaLevelOffset);
        for (BlockPos canBreathPo : canBreathPos) {
            if (this.givesAir(maid, canBreathPo) && maid.canPathReach(canBreathPo)) {
                maid.setGoToBreathArea(true);
                BehaviorUtils.setWalkAndLookTargetMemories(maid, canBreathPo, 0.5f, 1);
                return;
            }
        }

        // 这样三层检测是不是有点性能消耗恐怖了....#canPathReach消耗有点恐怖...
    }

    // 提供空气的判断
    private boolean givesAir(EntityMaid maid, BlockPos pos) {
        Level level = maid.level;
        BlockState blockstate = level.getBlockState(pos);
        return (level.getFluidState(pos).isEmpty() || blockstate.is(Blocks.BUBBLE_COLUMN)) && blockstate.isPathfindable(level, pos, PathComputationType.LAND);
    }
}
