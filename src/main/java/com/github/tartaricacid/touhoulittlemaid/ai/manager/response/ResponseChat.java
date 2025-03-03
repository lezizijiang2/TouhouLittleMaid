package com.github.tartaricacid.touhoulittlemaid.ai.manager.response;

import com.google.gson.annotations.SerializedName;

public class ResponseChat {
    @SerializedName("chat_text")
    public String chatText = "";

    @SerializedName("tts_text")
    public String ttsText = "";

    public String getChatText() {
        return chatText;
    }

    public String getTtsText() {
        return ttsText;
    }
}
