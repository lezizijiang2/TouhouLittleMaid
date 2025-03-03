package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIDataSerializable;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public record SaveMaidAIDataPackage(int entityId, MaidAIDataSerializable data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SaveMaidAIDataPackage> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("save_maid_ai_data"));
    public static final StreamCodec<ByteBuf, SaveMaidAIDataPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SaveMaidAIDataPackage::entityId,
            MaidAIDataSerializable.STREAM_CODEC,
            SaveMaidAIDataPackage::data,
            SaveMaidAIDataPackage::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SaveMaidAIDataPackage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> handle(message, (ServerPlayer) context.player()));
        }
    }

    private static void handle(SaveMaidAIDataPackage message, @Nullable ServerPlayer player) {
        if (player == null) {
            return;
        }
        Entity entity = player.level.getEntity(message.entityId);
        if (entity instanceof EntityMaid maid && maid.isOwnedBy(player)) {
            maid.getAiChatManager().copyFrom(message.data);
        }
    }
}
