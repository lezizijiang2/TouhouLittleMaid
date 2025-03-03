package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public record SendUserChatPackage(int maidId, String message, String language) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SendUserChatPackage> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("send_user_chat"));
    public static final StreamCodec<ByteBuf, SendUserChatPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SendUserChatPackage::maidId,
            ByteBufCodecs.STRING_UTF8,
            SendUserChatPackage::message,
            ByteBufCodecs.STRING_UTF8,
            SendUserChatPackage::language,
            SendUserChatPackage::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendUserChatPackage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> onHandle(message, context));
        }
    }

    private static void onHandle(SendUserChatPackage message, IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();
        Entity entity = sender.level.getEntity(message.maidId);
        if (entity instanceof EntityMaid maid && maid.isOwnedBy(sender) && maid.isAlive()) {
            maid.getAiChatManager().chat(message.message, message.language);
        }
    }
}
