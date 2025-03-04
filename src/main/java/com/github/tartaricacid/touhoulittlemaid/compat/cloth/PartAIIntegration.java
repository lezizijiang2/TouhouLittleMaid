package com.github.tartaricacid.touhoulittlemaid.compat.cloth;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai.AIChatScreen;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.SaveMaidAIDataMessage;
import com.github.tartaricacid.touhoulittlemaid.util.version.TComponent;
import com.google.common.collect.Maps;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.SortedMap;

public class PartAIIntegration {
    public static ConfigBuilder getConfigBuilder(EntityMaid maid) {
        ConfigBuilder root = ConfigBuilder.create().setTitle(TComponent.translatable("config.touhou_little_maid.part_ai"));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = root.entryBuilder();
        init(root, entryBuilder, maid);
        root.setSavingRunnable(() -> NetworkHandler.CHANNEL.sendToServer(new SaveMaidAIDataMessage(maid.getId(), maid.getAiChatManager())));
        return root;
    }

    @SuppressWarnings("all")
    public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder, EntityMaid maid) {
        MaidAIChatManager manager = maid.getAiChatManager();
        ConfigCategory ai = root.getOrCreateCategory(TComponent.translatable("config.touhou_little_maid.part_ai"));

        String chatSiteName = manager.getChatSiteName();
        String defaultChatSiteName = AIChatScreen.CLIENT_CHAT_SITES.containsKey(chatSiteName) ? chatSiteName : StringUtils.EMPTY;
        DropdownBoxEntry<String> chatSite = entryBuilder.startStringDropdownMenu(TComponent.translatable("config.touhou_little_maid.part_ai.chat_site_name"), defaultChatSiteName)
                .setDefaultValue(StringUtils.EMPTY).setSelections(AIChatScreen.CLIENT_CHAT_SITES.keySet()).setSaveConsumer(manager::setChatSiteName)
                .setTooltip(TComponent.translatable("config.touhou_little_maid.part_ai.chat_site_name.tooltip")).build();
        ai.addEntry(chatSite);

        for (String key : AIChatScreen.CLIENT_CHAT_SITES.keySet()) {
            List<String> models = AIChatScreen.CLIENT_CHAT_SITES.get(key);
            String model = manager.getChatModel();
            String defaultModel = models.contains(model) ? model : StringUtils.EMPTY;
            ai.addEntry(entryBuilder.startStringDropdownMenu(TComponent.translatable("config.touhou_little_maid.part_ai.chat_model"), defaultModel)
                    .setDefaultValue(StringUtils.EMPTY).setSelections(models).setTooltip(TComponent.translatable("config.touhou_little_maid.part_ai.chat_model.tooltip"))
                    .setSaveConsumer(result -> {
                        if (StringUtils.isNotBlank(chatSite.getValue()) && chatSite.getValue().equals(key)) {
                            manager.setChatModel(result);
                        }
                    })
                    .setDisplayRequirement(() -> StringUtils.isNotBlank(chatSite.getValue()) && chatSite.getValue().equals(key)).build());
        }

        double defaultTemperature = Math.max(manager.getChatTemperature(), 0);
        ai.addEntry(entryBuilder.startDoubleField(TComponent.translatable("config.touhou_little_maid.part_ai.chat_temperature"), defaultTemperature)
                .setDefaultValue(0.5).setMin(0.0).setMax(2.0)
                .setTooltip(TComponent.translatable("config.touhou_little_maid.part_ai.chat_temperature.tooltip"))
                .setSaveConsumer(manager::setChatTemperature).build());

        String ttsSiteName = manager.getTtsSiteName();
        String defaultTtsSiteName = AIChatScreen.CLIENT_TTS_SITES.containsKey(ttsSiteName) ? ttsSiteName : StringUtils.EMPTY;
        DropdownBoxEntry<String> ttsSite = entryBuilder.startStringDropdownMenu(TComponent.translatable("config.touhou_little_maid.part_ai.tts_site_name"), defaultTtsSiteName)
                .setDefaultValue(StringUtils.EMPTY).setSelections(AIChatScreen.CLIENT_TTS_SITES.keySet())
                .setSaveConsumer(manager::setTtsSiteName)
                .setTooltip(TComponent.translatable("config.touhou_little_maid.part_ai.tts_site_name.tooltip")).build();
        ai.addEntry(ttsSite);

        for (String key : AIChatScreen.CLIENT_TTS_SITES.keySet()) {
            List<String> models = AIChatScreen.CLIENT_TTS_SITES.get(key);
            String model = manager.getTtsModel();
            String defaultModel = models.contains(model) ? model : StringUtils.EMPTY;
            ai.addEntry(entryBuilder.startStringDropdownMenu(TComponent.translatable("config.touhou_little_maid.part_ai.tts_model"), defaultModel)
                    .setDefaultValue(StringUtils.EMPTY).setSelections(models).setTooltip(TComponent.translatable("config.touhou_little_maid.part_ai.tts_model.tooltip"))
                    .setSaveConsumer(result -> {
                        if (StringUtils.isNotBlank(ttsSite.getValue()) && ttsSite.getValue().equals(key)) {
                            manager.setTtsModel(result);
                        }
                    })
                    .setDisplayRequirement(() -> StringUtils.isNotBlank(ttsSite.getValue()) && ttsSite.getValue().equals(key)).build());
        }

        SortedMap<String, LanguageInfo> languages = transformLanguageInfo();
        String defaultLanguage = StringUtils.isBlank(manager.getTtsLanguage()) ? LanguageManager.DEFAULT_LANGUAGE_CODE : manager.getTtsLanguage();
        ai.addEntry(entryBuilder.startStringDropdownMenu(TComponent.translatable("config.touhou_little_maid.part_ai.tts_language"),
                        defaultLanguage, TComponent::literal, cell(languages)).setSelections(languages.keySet())
                .setDefaultValue(LanguageManager.DEFAULT_LANGUAGE_CODE).setTooltip(TComponent.translatable("config.touhou_little_maid.part_ai.tts_language.tooltip"))
                .setSaveConsumer(manager::setTtsLanguage).build());
    }

    private static DropdownBoxEntry.SelectionCellCreator<String> cell(SortedMap<String, LanguageInfo> languages) {
        LanguageInfo defaultLanguage = languages.get(LanguageManager.DEFAULT_LANGUAGE_CODE);
        return new DropdownBoxEntry.DefaultSelectionCellCreator<>(i -> TComponent.literal(languages.getOrDefault(i, defaultLanguage).toString()));
    }

    private static SortedMap<String, LanguageInfo> transformLanguageInfo() {
        SortedMap<String, LanguageInfo> output = Maps.newTreeMap();
        Minecraft.getInstance().getLanguageManager().getLanguages().forEach(languageInfo -> output.put(languageInfo.getCode(), languageInfo));
        return output;
    }
}
