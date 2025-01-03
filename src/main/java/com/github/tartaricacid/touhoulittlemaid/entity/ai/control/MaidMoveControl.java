package com.github.tartaricacid.touhoulittlemaid.entity.ai.control;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

// link{Drowned#DrownedMoveControl}
public class MaidMoveControl extends MoveControl {
    private final EntityMaid maid;

    public MaidMoveControl(EntityMaid maid) {
        super(maid);
        this.maid = maid;
    }

    public void tick() {
        // 用underwater是因为如果是inwater的话，女仆在登陆的时候，就会一直卡住，上不来...
        if (this.maid.isUnderWater() && this.hasWanted()) {

            PathNavigation navigation1 = this.maid.getNavigation();
            Path path = navigation1.getPath();
            if (navigation1.isDone()) {
                this.maid.setWantToSwim(false);
            } else if (path != null || maid.getTarget() != null) {
                this.maid.setWantToSwim(true);
            }

            if (this.operation != MoveControl.Operation.MOVE_TO || this.maid.getNavigation().isDone()) {
                this.maid.setSpeed(0.0F);
                return;
            }

            double d0 = this.wantedX - this.maid.getX();
            double d1 = this.wantedY - this.maid.getY();
            double d2 = this.wantedZ - this.maid.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d1 /= d3;
            float f = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.maid.setYRot(this.rotlerp(this.maid.getYRot(), f, 90.0F));
            this.maid.yBodyRot = this.maid.getYRot();
            float f1 = (float) (this.speedModifier * this.maid.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f2 = Mth.lerp(1, this.maid.getSpeed(), f1);

            // 太慢了，3倍基础速度
            this.maid.setSpeed(f2 * 3);
            this.maid.setDeltaMovement(this.maid.getDeltaMovement().add((double) f2 * d0 * 0.005D, (double) f2 * d1 * 0.25D, (double) f2 * d2 * 0.005D));
        } else {
            super.tick();
        }
    }
}