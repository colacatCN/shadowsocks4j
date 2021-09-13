package com.life4ever.shadowsocks4j.proxy.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientUtil {

    private static final int CONNECTION_REQUEST_TIMEOUT = 10 * 1000;

    private static final int CONNECT_TIMEOUT = 60 * 1000;

    private static final int SOCKET_TIMEOUT = 60 * 1000;

    private static final RequestConfig REQUEST_CONFIG;

    private static CloseableHttpClient httpClient;

    static {
        REQUEST_CONFIG = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    private HttpClientUtil() {
    }

    public static String execute(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(REQUEST_CONFIG);
        try (CloseableHttpResponse response = getHttpClient().execute(httpGet)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public static void close() throws IOException {
        httpClient.close();
        httpClient = null;
    }

    private static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }
        return httpClient;
    }

}
