package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CharacterSetting {
    private static final String COMMENTS = "#";
    private static final String META = "meta";
    private static final String SETTING = "setting";
    private static final String AUTHOR = "author";
    private static final String MODEL_ID = "model_id";
    private static final Yaml YAML = new Yaml();

    private final String author;
    private final List<String> modelId;
    private final String rawSetting;

    @SuppressWarnings("unchecked")
    public CharacterSetting(File settingFile) throws IOException {
        try (FileReader reader = new FileReader(settingFile, StandardCharsets.UTF_8)) {
            Map<String, Object> result = YAML.load(reader);
            if (result == null) {
                throw new IOException("Failed to load setting file");
            }
            if (!result.containsKey(META) || !result.containsKey(SETTING)) {
                throw new IOException("Setting file must contain meta and setting key");
            }
            Map<String, Object> meta = (Map<String, Object>) result.get(META);
            if (!meta.containsKey(AUTHOR) || !meta.containsKey(MODEL_ID)) {
                throw new IOException("Meta must contain author and model_id key");
            }
            StringBuilder builder = new StringBuilder();
            processText((String) result.get(SETTING)).forEach(value -> builder.append(value).append("\n"));

            this.author = (String) meta.get(AUTHOR);
            this.modelId = (List<String>) meta.get(MODEL_ID);
            this.rawSetting = builder.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public CharacterSetting(InputStream stream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Map<String, Object> result = YAML.load(reader);
            if (result == null) {
                throw new IOException("Failed to load setting file");
            }
            if (!result.containsKey(META) || !result.containsKey(SETTING)) {
                throw new IOException("Setting file must contain meta and setting key");
            }
            Map<String, Object> meta = (Map<String, Object>) result.get(META);
            if (!meta.containsKey(AUTHOR) || !meta.containsKey(MODEL_ID)) {
                throw new IOException("Meta must contain author and model_id key");
            }
            StringBuilder builder = new StringBuilder();
            processText((String) result.get(SETTING)).forEach(value -> builder.append(value).append("\n"));

            this.author = (String) meta.get(AUTHOR);
            this.modelId = (List<String>) meta.get(MODEL_ID);
            this.rawSetting = builder.toString();
        }
    }

    private static List<String> processText(String text) {
        return Arrays.stream(StringUtils.split(text, "\n"))
                .map(StringUtils::trim)
                .filter(s -> !s.startsWith(COMMENTS))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    public String getSetting(EntityMaid maid, String language) {
        return PapiReplacer.replace(rawSetting, maid, language);
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getModelId() {
        return modelId;
    }
}