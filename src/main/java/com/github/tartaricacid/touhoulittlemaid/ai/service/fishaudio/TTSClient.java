package com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio;

import com.github.tartaricacid.touhoulittlemaid.ai.service.Service;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.TTSRequest;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.response.TTSCallback;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

public class TTSClient {
    private final HttpClient httpClient;
    private String baseUrl = "";
    private String apiKey = "";
    private TTSRequest request;

    public static TTSClient create(final HttpClient httpClient) {
        return new TTSClient(httpClient);
    }

    private TTSClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public TTSClient baseUrl(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
        return this;
    }

    public TTSClient apiKey(final String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public TTSClient request(TTSRequest request) {
        this.request = request;
        return this;
    }

    public void handle(Consumer<byte[]> consumer, Consumer<Throwable> failConsumer) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(Service.GSON.toJson(request)))
                .timeout(Duration.ofSeconds(20))
                .uri(URI.create(baseUrl + TTSRequest.getUrl()))
                .build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                .whenComplete((response, throwable) -> {
                    TTSCallback callback = new TTSCallback(consumer);
                    if (throwable != null) {
                        callback.onFailure(httpRequest, throwable);
                        failConsumer.accept(throwable);
                    } else {
                        callback.onResponse(response, failConsumer);
                    }
                });
    }
}
