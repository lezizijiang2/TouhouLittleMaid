package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendUserChatMessage {
    private final int maidId;
    private final String message;
    private final String language;

    public SendUserChatMessage(int maidId, String message, String language) {
        this.maidId = maidId;
        this.message = message;
        this.language = language;
    }

    public static void encode(SendUserChatMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.maidId);
        buf.writeUtf(message.message);
        buf.writeUtf(message.language);
    }

    public static SendUserChatMessage decode(FriendlyByteBuf buf) {
        return new SendUserChatMessage(buf.readVarInt(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(SendUserChatMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> onHandle(message, context));
        }
        context.setPacketHandled(true);
    }

    private static void onHandle(SendUserChatMessage message, NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) {
            return;
        }
        Entity entity = sender.level.getEntity(message.maidId);
        if (entity instanceof EntityMaid maid && maid.isOwnedBy(sender) && maid.isAlive()) {
            maid.getAiChatManager().chat(message.message, message.language);
        }
    }
}
