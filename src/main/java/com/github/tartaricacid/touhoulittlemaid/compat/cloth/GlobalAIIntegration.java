package com.github.tartaricacid.touhoulittlemaid.compat.cloth;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.google.common.collect.Maps;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Path;
import java.util.SortedMap;

public class GlobalAIIntegration {
    public static void aiChat(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory aiChat = root.getOrCreateCategory(Component.translatable("config.touhou_little_maid.global_ai"));

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.chat_enable"), AIConfig.CHAT_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.chat_enable.tooltip"))
                .setSaveConsumer(AIConfig.CHAT_ENABLED::set).build());

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.tts_enable"), AIConfig.TTS_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.tts_enable.tooltip"))
                .setSaveConsumer(AIConfig.TTS_ENABLED::set).build());

        aiChat.addEntry(entryBuilder.startDoubleField(Component.translatable("config.touhou_little_maid.global_ai.chat_temperature"), AIConfig.CHAT_TEMPERATURE.get())
                .setDefaultValue(AIConfig.CHAT_TEMPERATURE.getDefault()).setMin(0.0).setMax(2.0)
                .setTooltip(Component.translatable("config.touhou_little_maid.global_ai.chat_temperature.tooltip"))
                .setSaveConsumer(AIConfig.CHAT_TEMPERATURE::set).build());

        aiChat.addEntry(entryBuilder.startIntSlider(Component.translatable("config.touhou_little_maid.global_ai.maid_max_history_chat_size"),
                        AIConfig.MAID_MAX_HISTORY_CHAT_SIZE.get(), 1, 128).setDefaultValue(16)
                .setTooltip(Component.translatable("config.touhou_little_maid.global_ai.maid_max_history_chat_size.tooltip"))
                .setSaveConsumer(AIConfig.MAID_MAX_HISTORY_CHAT_SIZE::set).build());

        SortedMap<String, LanguageInfo> languages = transformLanguageInfo();
        aiChat.addEntry(entryBuilder.startStringDropdownMenu(Component.translatable("config.touhou_little_maid.global_ai.tts_language"),
                        AIConfig.TTS_LANGUAGE.get(), Component::literal, cell(languages)).setSelections(languages.keySet())
                .setDefaultValue(LanguageManager.DEFAULT_LANGUAGE_CODE).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.tts_language.tooltip"))
                .setSaveConsumer(info -> AIConfig.TTS_LANGUAGE.set(info)).build());

        Path availableSiteFile = Utils.getConfigFolder().resolve(TouhouLittleMaid.MOD_ID).resolve(AvailableSites.FILE_NAME);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_FILE, availableSiteFile.toString());
        MutableComponent text = Component.translatable("config.touhou_little_maid.global_ai.chat_site.click");
        text.withStyle(s -> s.withUnderlined(true).withColor(ChatFormatting.BLUE).withClickEvent(clickEvent));
        aiChat.addEntry(entryBuilder.startTextDescription(text).build());
    }

    private static DropdownBoxEntry.SelectionCellCreator<String> cell(SortedMap<String, LanguageInfo> languages) {
        LanguageInfo defaultLanguage = languages.get(LanguageManager.DEFAULT_LANGUAGE_CODE);
        return new DropdownBoxEntry.DefaultSelectionCellCreator<>(i -> Component.literal(languages.getOrDefault(i, defaultLanguage).toString()));
    }

    private static SortedMap<String, LanguageInfo> transformLanguageInfo() {
        SortedMap<String, LanguageInfo> output = Maps.newTreeMap();
        Minecraft.getInstance().getLanguageManager().getLanguages().forEach(languageInfo -> output.put(languageInfo.getCode(), languageInfo));
        return output;
    }
}
