package com.github.tartaricacid.touhoulittlemaid.compat.cloth;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Path;
import java.util.SortedMap;

public class GlobalAIIntegration {
    private static final String DEFAULT_LANGUAGE = "en_us";

    public static void aiChat(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory aiChat = root.getOrCreateCategory(Component.translatable("config.touhou_little_maid.global_ai"));

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.chat_enable"), AIConfig.CHAT_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.chat_enable.tooltip"))
                .setSaveConsumer(value -> {
                    AIConfig.CHAT_ENABLED.set(value);
                    AIConfig.CHAT_ENABLED.save();
                }).build());

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.tts_enable"), AIConfig.TTS_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.tts_enable.tooltip"))
                .setSaveConsumer(value -> {
                    AIConfig.TTS_ENABLED.set(value);
                    AIConfig.TTS_ENABLED.save();
                }).build());

        aiChat.addEntry(entryBuilder.startDoubleField(Component.translatable("config.touhou_little_maid.global_ai.chat_temperature"), AIConfig.CHAT_TEMPERATURE.get())
                .setDefaultValue(AIConfig.CHAT_TEMPERATURE.getDefault()).setMin(0.0).setMax(2.0)
                .setTooltip(Component.translatable("config.touhou_little_maid.global_ai.chat_temperature.tooltip"))
                .setSaveConsumer(value -> {
                    AIConfig.CHAT_TEMPERATURE.set(value);
                    AIConfig.CHAT_TEMPERATURE.save();
                }).build());

        aiChat.addEntry(entryBuilder.startIntSlider(Component.translatable("config.touhou_little_maid.global_ai.maid_max_history_chat_size"),
                        AIConfig.MAID_MAX_HISTORY_CHAT_SIZE.get(), 1, 128).setDefaultValue(16)
                .setTooltip(Component.translatable("config.touhou_little_maid.global_ai.maid_max_history_chat_size.tooltip"))
                .setSaveConsumer(value -> {
                    AIConfig.MAID_MAX_HISTORY_CHAT_SIZE.set(value);
                    AIConfig.MAID_MAX_HISTORY_CHAT_SIZE.save();
                }).build());

        SortedMap<String, LanguageInfo> languages = Minecraft.getInstance().getLanguageManager().getLanguages();
        aiChat.addEntry(entryBuilder.startStringDropdownMenu(Component.translatable("config.touhou_little_maid.global_ai.tts_language"),
                        AIConfig.TTS_LANGUAGE.get(), Component::literal, cell(languages)).setSelections(languages.keySet())
                .setDefaultValue(DEFAULT_LANGUAGE).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.tts_language.tooltip"))
                .setSaveConsumer(info -> {
                    AIConfig.TTS_LANGUAGE.set(info);
                    AIConfig.TTS_LANGUAGE.save();
                }).build());

        Path availableSiteFile = Utils.getConfigFolder().resolve(TouhouLittleMaid.MOD_ID).resolve(AvailableSites.FILE_NAME);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_FILE, availableSiteFile.toString());
        MutableComponent text = Component.translatable("config.touhou_little_maid.global_ai.chat_site.click");
        text.withStyle(s -> s.withUnderlined(true).withColor(ChatFormatting.BLUE).withClickEvent(clickEvent));
        aiChat.addEntry(entryBuilder.startTextDescription(text).build());
    }

    private static DropdownBoxEntry.SelectionCellCreator<String> cell(SortedMap<String, LanguageInfo> languages) {
        LanguageInfo defaultLanguage = languages.get(DEFAULT_LANGUAGE);
        return new DropdownBoxEntry.DefaultSelectionCellCreator<>(i -> languages.getOrDefault(i, defaultLanguage).toComponent());
    }
}
