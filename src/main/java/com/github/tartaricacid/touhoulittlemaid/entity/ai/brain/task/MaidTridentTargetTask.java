package com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

public class MaidTridentTargetTask extends Behavior<EntityMaid> {
    // 半秒的间隔使用时间，模拟忠诚后的三叉戟击中目标返回来所需要的时间，不然就太INBA了...
    private final int attackCooldown = 10;
    private int attackTime = -1;
    private int seeTime;

    public MaidTridentTargetTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        Optional<LivingEntity> memory = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (memory.isPresent()) {
            LivingEntity target = memory.get();
            return hasTrident(owner)
                   && BehaviorUtils.canSee(owner, target);
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        return entityIn.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        entityIn.setSwingingArms(true);
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityMaid owner, long gameTime) {
        owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((target) -> {
            // 强行看见并朝向
            owner.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
            boolean canSee = BehaviorUtils.canSee(owner, target);
            boolean seeTimeMoreThanZero = this.seeTime > 0;

            // 如果两者不一致，重置看见时间
            if (canSee != seeTimeMoreThanZero) {
                this.seeTime = 0;
            }
            // 如果看见了对方，增加看见时间，否则减少
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            // 如果实体手部处于激活状态
            if (owner.isUsingItem()) {
                // 如果看不见对方超时 60，重置激活状态
                if (!canSee && this.seeTime < -60) {
                    owner.stopUsingItem();
                } else if (canSee) {
                    // 否则开始进行远程攻击
                    int ticksUsingItem = owner.getTicksUsingItem();

                    // 物品最大使用计数大于 10 才可以
                    // 这里大致解释下计数的意思，也就是蓄力，蓄力越长自然射的越远
                    // 只有蓄力超过 0.5 秒才会进行发射
                    // 如果有引雷，必须2格之外（安全范围，以免波及自身）
                    if (ticksUsingItem >= 10 && !(!owner.isUnderWater() &&EnchantmentHelper.hasChanneling(owner.getMainHandItem())
                            && owner.level.isThundering() && owner.closerThan(target, Math.max(target.getBbWidth(), target.getBbHeight()) + 1.5))) {
                        owner.stopUsingItem();
                        owner.performRangedAttack(target, 0);
                        this.attackTime = this.attackCooldown;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                // 非激活状态，但是时长合适，开始激活手部
                owner.startUsingItem(InteractionHand.MAIN_HAND);
            }
        });
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        this.seeTime = 0;
        this.attackTime = -1;
        entityIn.stopUsingItem();
    }

    // 必须要有忠诚附魔，硬性条件(虽说代码上可不用强制要求...)
    // 不然女仆有时miss了，三叉戟飞的太远...
    // 捡不回来，玩家：我三叉戟呢:(
    private boolean hasTrident(EntityMaid maid) {
        return maid.getMainHandItem().getItem() instanceof TridentItem && EnchantmentHelper.getLoyalty(maid.getMainHandItem()) > 0;
    }
}
