package com.life4ever.shadowsocks4j.proxy.util;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {

    private static OkHttpClient okHttpClient;

    private HttpClientUtil() {
    }

    public static String execute(String url) throws Shadowsocks4jProxyException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        String result;
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            result = Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
        return result;
    }

    private static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60 * 1000L, TimeUnit.MILLISECONDS)
                    .readTimeout(60 * 1000L, TimeUnit.MILLISECONDS)
                    .writeTimeout(60 * 1000L, TimeUnit.MILLISECONDS)
                    .build();
        }
        return okHttpClient;
    }

}
