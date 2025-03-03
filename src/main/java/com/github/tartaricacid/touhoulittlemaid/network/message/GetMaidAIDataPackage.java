package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public record GetMaidAIDataPackage(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GetMaidAIDataPackage> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("get_maid_ai_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GetMaidAIDataPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GetMaidAIDataPackage::entityId,
            GetMaidAIDataPackage::new
    );

    public static void handle(GetMaidAIDataPackage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> handleInner(message, context));
        }
    }

    private static void handleInner(GetMaidAIDataPackage message, IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();
        Entity entity = sender.level.getEntity(message.entityId);
        if (entity instanceof EntityMaid maid && maid.isOwnedBy(sender)) {
            PacketDistributor.sendToPlayer(sender, new OpenMaidAIDataScreenPackage(message.entityId, maid.getAiChatManager()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
