package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MaidAIDataSerializable {
    public static final StreamCodec<ByteBuf, MaidAIDataSerializable> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MaidAIDataSerializable::getChatSiteName,
            ByteBufCodecs.STRING_UTF8, MaidAIDataSerializable::getChatModel,
            ByteBufCodecs.DOUBLE, MaidAIDataSerializable::getChatTemperature,
            ByteBufCodecs.STRING_UTF8, MaidAIDataSerializable::getTtsSiteName,
            ByteBufCodecs.STRING_UTF8, MaidAIDataSerializable::getTtsModel,
            ByteBufCodecs.STRING_UTF8, MaidAIDataSerializable::getTtsLanguage,
            MaidAIDataSerializable::new
    );

    protected String chatSiteName = "";
    protected String chatModel = "";
    protected double chatTemperature = -1;
    protected String ttsSiteName = "";
    protected String ttsModel = "";
    protected String ttsLanguage = "";

    public MaidAIDataSerializable() {
    }

    public MaidAIDataSerializable(String chatSiteName, String chatModel, double chatTemperature, String ttsSiteName, String ttsModel, String ttsLanguage) {
        this.chatSiteName = chatSiteName;
        this.chatModel = chatModel;
        this.chatTemperature = chatTemperature;
        this.ttsSiteName = ttsSiteName;
        this.ttsModel = ttsModel;
        this.ttsLanguage = ttsLanguage;
    }

    public void copyFrom(MaidAIDataSerializable data) {
        chatSiteName = data.chatSiteName;
        chatModel = data.chatModel;
        chatTemperature = data.chatTemperature;
        ttsSiteName = data.ttsSiteName;
        ttsModel = data.ttsModel;
        ttsLanguage = data.ttsLanguage;
    }

    public void readFromTag(CompoundTag tag) {
        if (tag.contains("MaidAIChatData")) {
            CompoundTag data = tag.getCompound("MaidAIChatData");
            chatSiteName = data.getString("ChatSiteName");
            chatModel = data.getString("ChatModel");
            chatTemperature = data.getDouble("ChatTemperature");
            ttsSiteName = data.getString("TtsSiteName");
            ttsModel = data.getString("TtsModel");
            ttsLanguage = data.getString("TtsLanguage");
        }
    }

    public void writeToTag(CompoundTag tag) {
        CompoundTag data = new CompoundTag();
        {
            data.putString("ChatSiteName", chatSiteName);
            data.putString("ChatModel", chatModel);
            data.putDouble("ChatTemperature", chatTemperature);
            data.putString("TtsSiteName", ttsSiteName);
            data.putString("TtsModel", ttsModel);
            data.putString("TtsLanguage", ttsLanguage);
        }
        tag.put("MaidAIChatData", data);
    }

    public String getChatSiteName() {
        return chatSiteName;
    }

    public void setChatSiteName(String chatSiteName) {
        this.chatSiteName = chatSiteName;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public double getChatTemperature() {
        return chatTemperature;
    }

    public void setChatTemperature(double chatTemperature) {
        this.chatTemperature = chatTemperature;
    }

    public String getTtsSiteName() {
        return ttsSiteName;
    }

    public void setTtsSiteName(String ttsSiteName) {
        this.ttsSiteName = ttsSiteName;
    }

    public String getTtsModel() {
        return ttsModel;
    }

    public void setTtsModel(String ttsModel) {
        this.ttsModel = ttsModel;
    }

    public String getTtsLanguage() {
        return ttsLanguage;
    }

    public void setTtsLanguage(String ttsLanguage) {
        this.ttsLanguage = ttsLanguage;
    }
}
