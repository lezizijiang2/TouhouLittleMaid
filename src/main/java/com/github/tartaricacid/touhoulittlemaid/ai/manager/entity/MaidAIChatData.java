package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.CharacterSetting;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.Site;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class MaidAIChatData extends MaidAIDataSerializable {
    protected final EntityMaid maid;
    protected final CappedQueue<HistoryChat> history;

    public MaidAIChatData(EntityMaid maid) {
        this.maid = maid;
        this.history = new CappedQueue<>(AIConfig.MAID_MAX_HISTORY_CHAT_SIZE.get());
    }

    @Nullable
    public Site getChatSite() {
        Site site;
        if (StringUtils.isBlank(chatSiteName)) {
            site = AvailableSites.getFirstAvailableChatSite();
        } else {
            site = AvailableSites.getChatSite(chatSiteName);
            if (site == null) {
                site = AvailableSites.getFirstAvailableChatSite();
            }
        }
        return site;
    }

    @Nullable
    public Site getTtsSite() {
        Site site;
        if (StringUtils.isBlank(ttsSiteName)) {
            site = AvailableSites.getFirstAvailableTtsSite();
        } else {
            site = AvailableSites.getTtsSite(ttsSiteName);
            if (site == null) {
                site = AvailableSites.getFirstAvailableTtsSite();
            }
        }
        return site;
    }

    @Override
    public String getChatModel() {
        Site site = getChatSite();
        String model = StringUtils.EMPTY;
        if (site != null && !site.getModels().isEmpty()) {
            if (StringUtils.isBlank(chatModel)) {
                model = site.getModels().get(0);
            } else {
                model = chatModel;
            }
        }
        return model;
    }

    @Override
    public String getTtsModel() {
        Site site = getTtsSite();
        String model = StringUtils.EMPTY;
        if (site != null && !site.getModels().isEmpty()) {
            if (StringUtils.isBlank(ttsModel)) {
                model = site.getModels().get(0);
            } else {
                model = ttsModel;
            }
        }
        return model;
    }

    @Override
    public double getChatTemperature() {
        if (chatTemperature >= 0) {
            return chatTemperature;
        }
        return AIConfig.CHAT_TEMPERATURE.get();
    }

    @Override
    public String getTtsLanguage() {
        if (StringUtils.isNotBlank(ttsLanguage)) {
            return ttsLanguage;
        }
        return AIConfig.TTS_LANGUAGE.get();
    }

    public CappedQueue<HistoryChat> getHistory() {
        return history;
    }

    public void addUserHistory(String message) {
        this.history.add(HistoryChat.userChat(maid, message));
    }

    public void addAssistantHistory(String message) {
        this.history.add(HistoryChat.assistantChat(maid, message));
    }

    public EntityMaid getMaid() {
        return maid;
    }

    public Optional<CharacterSetting> getSetting() {
        String modelId = this.maid.getModelId();
        return SettingReader.getSetting(modelId);
    }
}
