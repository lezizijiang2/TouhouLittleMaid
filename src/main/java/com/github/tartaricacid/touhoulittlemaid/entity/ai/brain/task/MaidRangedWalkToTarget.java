package com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.function.Function;

/**
 * 修改了原版的走向攻击目标的 AI，现在能够依据超远视距和 home 范围进行行走判断
 * <p>
 * 主要用于远程射击，近战的还是请走 SetWalkTargetFromAttackTargetIfTargetOutOfReach
 */
public class MaidRangedWalkToTarget extends Behavior<EntityMaid> {
    private final Function<LivingEntity, Float> speedModifier;

    public MaidRangedWalkToTarget(float speedModifier) {
        this(entity -> speedModifier);
    }

    public MaidRangedWalkToTarget(Function<LivingEntity, Float> speedModifier) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED));
        this.speedModifier = speedModifier;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long pGameTime) {
        LivingEntity livingentity = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (maid.canSee(livingentity) && isWithinRestriction(maid, livingentity)) {
            this.clearWalkTarget(maid);
        } else {
            this.setWalkAndLookTarget(maid, livingentity);
        }
    }

    private void setWalkAndLookTarget(EntityMaid maid, LivingEntity target) {
        Brain<?> brain = maid.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        WalkTarget walktarget = new WalkTarget(new EntityTracker(target, false), this.speedModifier.apply(maid), 0);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walktarget);
    }

    private void clearWalkTarget(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private static boolean isWithinRestriction(EntityMaid maid, LivingEntity target) {
        float restrictRadius = maid.getRestrictRadius() * 0.65f;
        return target.distanceTo(maid) < restrictRadius;
    }
}
