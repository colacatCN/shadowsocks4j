package com.life4ever.shadowsocks4j.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.life4ever.shadowsocks4j.config.ServerConfig;
import com.life4ever.shadowsocks4j.config.ShadowsocksConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.life4ever.shadowsocks4j.consts.Shadowsocks4jConst.APPLICATION_CONFIG_FILE_NAME;
import static com.life4ever.shadowsocks4j.consts.Shadowsocks4jConst.USER_DIR;

@Slf4j
public class ConfigUtil {

    private static ShadowsocksConfig SHADOWSOCKS_CONFIG;

    static {
        try {
            SHADOWSOCKS_CONFIG = loadShadowsocksConfig();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static InetSocketAddress getLocalServerInetSocketAddress() {
        ServerConfig localServerConfig = SHADOWSOCKS_CONFIG.getLocalServerConfig();
        return new InetSocketAddress(localServerConfig.getIp(), localServerConfig.getPort());
    }

    public static InetSocketAddress getRemoteServerInetSocketAddress() {
        ServerConfig remoteServerConfig = SHADOWSOCKS_CONFIG.getRemoteServerConfig();
        return new InetSocketAddress(remoteServerConfig.getIp(), remoteServerConfig.getPort());
    }

    private static ShadowsocksConfig loadShadowsocksConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        Path path = Paths.get(USER_DIR, APPLICATION_CONFIG_FILE_NAME);
        return objectMapper.readValue(Files.newInputStream(path), ShadowsocksConfig.class);
    }

}
