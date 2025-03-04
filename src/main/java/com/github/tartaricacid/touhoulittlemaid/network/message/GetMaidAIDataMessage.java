package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GetMaidAIDataMessage {
    private final int entityId;

    public GetMaidAIDataMessage(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(GetMaidAIDataMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
    }

    public static GetMaidAIDataMessage decode(FriendlyByteBuf buf) {
        return new GetMaidAIDataMessage(buf.readInt());
    }

    public static void handle(GetMaidAIDataMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> handle(message, context));
        }
        context.setPacketHandled(true);
    }

    private static void handle(GetMaidAIDataMessage message, NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) {
            return;
        }
        Entity entity = sender.level.getEntity(message.entityId);
        if (entity instanceof EntityMaid maid && maid.isOwnedBy(sender)) {
            NetworkHandler.sendToClientPlayer(new OpenMaidAIDataScreenMessage(message.entityId, maid.getAiChatManager()), sender);
        }
    }
}
