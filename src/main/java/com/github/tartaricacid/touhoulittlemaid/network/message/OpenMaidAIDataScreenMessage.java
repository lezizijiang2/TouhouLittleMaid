package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIDataSerializable;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai.AIChatScreen;
import com.github.tartaricacid.touhoulittlemaid.client.gui.mod.ClothConfigScreen;
import com.github.tartaricacid.touhoulittlemaid.compat.cloth.ClothConfigCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.registry.CompatRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenMaidAIDataScreenMessage {
    private final int entityId;
    private final MaidAIDataSerializable data;

    public OpenMaidAIDataScreenMessage(int entityId, MaidAIDataSerializable data) {
        this.entityId = entityId;
        this.data = data;
    }

    public static void encode(OpenMaidAIDataScreenMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        message.data.encode(buf);
    }

    public static OpenMaidAIDataScreenMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        MaidAIDataSerializable data = new MaidAIDataSerializable();
        data.decode(buf);
        return new OpenMaidAIDataScreenMessage(entityId, data);
    }

    public static void handle(OpenMaidAIDataScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(OpenMaidAIDataScreenMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity e = mc.level.getEntity(message.entityId);
        if (e instanceof EntityMaid maid && maid.isAlive()) {
            maid.getAiChatManager().copyFrom(message.data);
            openConfigScreen(maid, mc);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void openConfigScreen(EntityMaid maid, Minecraft mc) {
        if (mc.screen instanceof AIChatScreen) {
            if (ModList.get().isLoaded(CompatRegistry.CLOTH_CONFIG)) {
                ClothConfigCompat.openPartAiSettingScreen(maid);
            } else {
                ClothConfigScreen.open();
            }
        }
    }
}
