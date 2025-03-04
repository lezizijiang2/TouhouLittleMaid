package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class Site {
    private static final String CHAT = "chat";
    private static final String TTS = "tts";
    private static final String TYPE = "type";
    private static final String URL = "url";
    private static final String API_KEY = "api_key";
    private static final String MODELS = "models";

    private String type;
    private String url;
    private String apiKey;
    private List<String> models;

    public Site(String type, String url, String apiKey, List<String> models) {
        this.type = type;
        this.url = url;
        this.apiKey = apiKey;
        this.models = models;
    }

    @SuppressWarnings("unchecked")
    public Site(LinkedHashMap<String, Object> map) {
        this.type = Objects.requireNonNullElse((String) map.get(TYPE), StringUtils.EMPTY);
        this.url = Objects.requireNonNullElse((String) map.get(URL), StringUtils.EMPTY);
        this.apiKey = Objects.requireNonNullElse((String) map.get(API_KEY), StringUtils.EMPTY);
        this.models = Objects.requireNonNullElse((List<String>) map.get(MODELS), Lists.newArrayList());
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<String> getModels() {
        return models;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public boolean isChat() {
        return CHAT.equals(type);
    }

    public boolean isTts() {
        return TTS.equals(type);
    }

    public LinkedHashMap<String, Object> siteToMap() {
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        map.put(TYPE, this.getType());
        map.put(URL, this.getUrl());
        map.put(API_KEY, this.getApiKey());
        map.put(MODELS, this.getModels());
        return map;
    }
}
