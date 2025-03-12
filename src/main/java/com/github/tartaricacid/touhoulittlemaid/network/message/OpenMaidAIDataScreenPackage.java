package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIDataSerializable;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai.AIChatScreen;
import com.github.tartaricacid.touhoulittlemaid.compat.cloth.ClothConfigCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.registry.CompatRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public record OpenMaidAIDataScreenPackage(int entityId, MaidAIDataSerializable data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenMaidAIDataScreenPackage> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("open_maid_ai_data_screen"));
    public static final StreamCodec<ByteBuf, OpenMaidAIDataScreenPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OpenMaidAIDataScreenPackage::entityId,
            MaidAIDataSerializable.STREAM_CODEC,
            OpenMaidAIDataScreenPackage::data,
            OpenMaidAIDataScreenPackage::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenMaidAIDataScreenPackage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> handle(message));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(OpenMaidAIDataScreenPackage message) {
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
            } else if (mc.player != null) {
                mc.player.sendSystemMessage(Component.translatable("gui.touhou_little_maid.cloth_config_warning.tips").withStyle(ChatFormatting.RED));
            }
        }
    }
}
