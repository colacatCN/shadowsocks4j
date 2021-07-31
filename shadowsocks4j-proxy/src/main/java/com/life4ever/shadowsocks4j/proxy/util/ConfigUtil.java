package com.life4ever.shadowsocks4j.proxy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.life4ever.shadowsocks4j.proxy.config.ServerConfig;
import com.life4ever.shadowsocks4j.proxy.config.Shadowsocks4jProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.APPLICATION_CONFIG_FILE_NAME;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_CIPHER_METHOD;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_DIR;

public class ConfigUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    private static Shadowsocks4jProxyConfig SHADOWSOCKS4J_PROXY_CONFIG;

    static {
        try {
            SHADOWSOCKS4J_PROXY_CONFIG = loadShadowsocks4jProxyConfig();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private ConfigUtil() {
    }

    public static InetSocketAddress getLocalServerInetSocketAddress() {
        ServerConfig localServerConfig = SHADOWSOCKS4J_PROXY_CONFIG.getLocalServerConfig();
        return new InetSocketAddress(localServerConfig.getIp(), localServerConfig.getPort());
    }

    public static InetSocketAddress getRemoteServerInetSocketAddress() {
        ServerConfig remoteServerConfig = SHADOWSOCKS4J_PROXY_CONFIG.getRemoteServerConfig();
        return new InetSocketAddress(remoteServerConfig.getIp(), remoteServerConfig.getPort());
    }

    public static String getCipherPassword() {
        return SHADOWSOCKS4J_PROXY_CONFIG.getCipherConfig().getPassword();
    }

    public static String getCipherMethod() {
        return Optional.ofNullable(SHADOWSOCKS4J_PROXY_CONFIG.getCipherConfig().getMethod())
                .orElse(DEFAULT_CIPHER_METHOD);
    }

    private static Shadowsocks4jProxyConfig loadShadowsocks4jProxyConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        Path path = Paths.get(USER_DIR, APPLICATION_CONFIG_FILE_NAME);
        return objectMapper.readValue(Files.newInputStream(path), Shadowsocks4jProxyConfig.class);
    }

}
