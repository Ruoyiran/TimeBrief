package com.royran.timebrief.utils;

import android.app.Activity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpUtils {

    public static final MediaType mediaType = MediaType.get("application/octet-stream; charset=utf-8");
    private static final int CONNECT_TIMEOUT = 5;
    private static final int READ_TIMEOUT = 5;
    private static final int WRITE_TIMEOUT = 5;


    public static void post(final Activity activity, final String url, final byte[] data, final HttpCallback callback) {
        RequestBody body;
        if (data != null) {
            body = RequestBody.create(data, mediaType);
        } else {
            body = RequestBody.create(new byte[0], mediaType);
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                runOnUiThread(activity, () -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) {
                try {
                    ResponseBody body = response.body();
                    assert body != null;
                    byte[] bodyBytes = body.bytes();
                    body.close();
                    response.close();
                    runOnUiThread(activity, () -> {
                        if (callback != null) {
                            callback.onSuccess(bodyBytes);
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(activity, () -> {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    });
                }
            }
        });
    }

    private static void runOnUiThread(Activity activity, Runnable runnable) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(runnable);
    }
}
