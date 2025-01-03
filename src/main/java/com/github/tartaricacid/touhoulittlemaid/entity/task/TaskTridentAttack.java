package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidAttackStrafingTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidAttackTridentTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidShootTargetTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidTridentTargetTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class TaskTridentAttack implements IRangedAttackTask {
    public static final ResourceLocation UID = new ResourceLocation(TouhouLittleMaid.MOD_ID, "trident_attack");
    private static final int MAX_STOP_ATTACK_DISTANCE = 30;

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.TRIDENT.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_RANGE_ATTACK.get(), 0.5f);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasTrident, IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasTrident(maid) || farAway(target, maid));
        BehaviorControl<Mob> moveToTargetTask = SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(0.6f);
        BehaviorControl<EntityMaid> maidAttackStrafingTask = new MaidAttackTridentTask();
        BehaviorControl<EntityMaid> shootTargetTask = new MaidTridentTargetTask();

        return Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, moveToTargetTask),
                Pair.of(5, maidAttackStrafingTask),
                Pair.of(5, shootTargetTask)
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasTrident, IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasTrident(maid) || farAway(target, maid));
        BehaviorControl<EntityMaid> shootTargetTask = new MaidTridentTargetTask();

        return Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, shootTargetTask)
        );
    }

    @Override
    public void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor) {
        ItemStack tridentItem = shooter.getMainHandItem().copy();
        CompoundTag tag = tridentItem.getTag();
        // 去除忠诚附魔，不然三叉戟返回来是发现主人不是玩家，就翻脸不认人了不给捡了..
        if (tag != null && tag.contains("Enchantments", 9)) {
            String value = String.valueOf(EnchantmentHelper.getEnchantmentId(Enchantments.LOYALTY));
            tag.getList("Enchantments", 10).removeIf(tag1 -> tag1 instanceof CompoundTag compoundTag && compoundTag.getString("id").equals(value));
        }
        // 原本是像应用上好感度伤害的，但是发现无效...不知是哪里没有debug到...
        ThrownTrident throwntrident = new ThrownTrident(shooter.level, shooter, tridentItem);
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333D) - throwntrident.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        throwntrident.shoot(d0, d1 + d3 * (double) 0.2F, d2, 2.5F + (float) 0 * 0.5F, 1.0F);
        throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        shooter.getMainHandItem().hurtAndBreak(1, shooter, (maid) -> maid.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        shooter.level.addFreshEntity(throwntrident);
        shooter.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 1.0F);
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_trident", this::hasTrident));
    }

    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > MAX_STOP_ATTACK_DISTANCE;
    }

    // 必须要有忠诚附魔，硬性条件(虽说代码上可不用强制要求...)
    // 不然女仆有时miss了，三叉戟飞的太远...
    // 捡不回来，玩家：我三叉戟呢:(
    private boolean hasTrident(EntityMaid maid) {
        return maid.getMainHandItem().getItem() instanceof TridentItem && EnchantmentHelper.getLoyalty(maid.getMainHandItem()) > 0;
    }
}