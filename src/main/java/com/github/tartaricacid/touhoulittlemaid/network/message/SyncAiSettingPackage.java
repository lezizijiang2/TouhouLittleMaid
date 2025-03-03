package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.client.event.PressAIChatKeyEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai.AIChatScreen;
import com.github.tartaricacid.touhoulittlemaid.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public record SyncAiSettingPackage(Set<String> settings, Map<String, List<String>> chatSites,
                                   Map<String, List<String>> ttsSites) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncAiSettingPackage> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("send_ai_setting"));
    public static final StreamCodec<ByteBuf, SyncAiSettingPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufUtils.STRING_SET_CODEC,
            SyncAiSettingPackage::settings,
            ByteBufUtils.SITES_CODEC,
            SyncAiSettingPackage::chatSites,
            ByteBufUtils.SITES_CODEC,
            SyncAiSettingPackage::ttsSites,
            SyncAiSettingPackage::new
    );

    public static SyncAiSettingPackage getInstance() {
        return new SyncAiSettingPackage(SettingReader.getAllSettingKeys(),
                AvailableSites.getClientChatSites(),
                AvailableSites.getClientTtsSites());
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAiSettingPackage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> handle(message));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SyncAiSettingPackage message) {
        PressAIChatKeyEvent.CAN_CHAT_MAID_IDS.clear();
        PressAIChatKeyEvent.CAN_CHAT_MAID_IDS.addAll(message.settings);
        AIChatScreen.CLIENT_CHAT_SITES.clear();
        AIChatScreen.CLIENT_CHAT_SITES.putAll(message.chatSites);
        AIChatScreen.CLIENT_TTS_SITES.clear();
        AIChatScreen.CLIENT_TTS_SITES.putAll(message.ttsSites);
    }
}
