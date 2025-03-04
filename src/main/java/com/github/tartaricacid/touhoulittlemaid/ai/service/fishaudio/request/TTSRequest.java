package com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Mth;

import java.util.List;

public class TTSRequest {
    @SerializedName("text")
    private String text = "";

    @SerializedName("chunk_length")
    private int chunkLength = 200;

    @SerializedName("format")
    private String format = Format.MP3.getId();

    @SerializedName("mp3_bitrate")
    private int mp3Bitrate = Mp3BitRate.MEDIUM.getBitRate();

    @SerializedName("opus_bitrate")
    private int opusBitrate = OpusBitRate.LOW.getBitRate();

    @SerializedName("prosody")
    private Prosody prosody = null;

    @SerializedName("references")
    private List<References> references = null;

    @SerializedName("reference_id")
    private String referenceId = null;

    @SerializedName("normalize")
    private boolean normalize = true;

    @SerializedName("latency")
    private String latency = Latency.NORMAL.getId();

    @SerializedName("sample_rate")
    private Integer sampleRate = null;

    public static TTSRequest create() {
        return new TTSRequest();
    }

    public static String getUrl() {
        return "/tts";
    }

    private TTSRequest() {
    }

    public TTSRequest setText(String text) {
        this.text = text;
        return this;
    }

    public TTSRequest setChunkLength(int chunkLength) {
        this.chunkLength = Mth.clamp(chunkLength, 100, 300);
        return this;
    }

    public TTSRequest setFormat(Format format) {
        this.format = format.getId();
        return this;
    }

    public TTSRequest setMp3Bitrate(Mp3BitRate mp3Bitrate) {
        this.mp3Bitrate = mp3Bitrate.getBitRate();
        return this;
    }

    public TTSRequest setOpusBitrate(OpusBitRate opusBitrate) {
        this.opusBitrate = opusBitrate.getBitRate();
        return this;
    }

    public TTSRequest setReferences(List<References> references) {
        this.references = references;
        return this;
    }

    public TTSRequest setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public TTSRequest setNormalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    public TTSRequest setLatency(Latency latency) {
        this.latency = latency.getId();
        return this;
    }

    public TTSRequest setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }
}
