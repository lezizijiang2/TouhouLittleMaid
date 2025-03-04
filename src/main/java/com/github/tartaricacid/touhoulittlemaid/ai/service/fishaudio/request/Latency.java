package com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request;

public enum Latency {
    NORMAL("normal"),
    BALANCED("balanced");

    private final String id;

    Latency(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
