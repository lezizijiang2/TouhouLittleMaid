package com.github.tartaricacid.touhoulittlemaid.ai.service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public interface ResponseCallback<T> {
    void onFailure(HttpRequest request, Throwable e);

    void onResponse(HttpResponse<T> response, Consumer<Throwable> failConsumer);

    default boolean isSuccessful(HttpResponse<T> response) {
        int statusCode = response.statusCode();
        return 200 <= statusCode && statusCode < 300;
    }
}
