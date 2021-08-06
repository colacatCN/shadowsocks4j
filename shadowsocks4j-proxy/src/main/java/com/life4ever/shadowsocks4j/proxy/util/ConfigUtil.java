package com.life4ever.shadowsocks4j.proxy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life4ever.shadowsocks4j.proxy.config.CipherConfig;
import com.life4ever.shadowsocks4j.proxy.config.PacConfig;
import com.life4ever.shadowsocks4j.proxy.config.RuleConfig;
import com.life4ever.shadowsocks4j.proxy.config.ServerConfig;
import com.life4ever.shadowsocks4j.proxy.config.Shadowsocks4jProxyConfig;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_CIPHER_METHOD;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_UPDATER_INTERVAL;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_UPDATER_URL;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.FILE_MONITOR_THREAD_NAME;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_CONF_DIR;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SYSTEM_RULE_YML;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_YML;

public class ConfigUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    private static final AtomicReference<ServerConfig> LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<ServerConfig> REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<CipherConfig> CIPHER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<PacConfig> PAC_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    static {
        try {
            updateShadowsocks4jProxyConfig();
            startWatchService();
        } catch (Shadowsocks4jProxyException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private ConfigUtil() {
    }

    private static void updateShadowsocks4jProxyConfig() throws Shadowsocks4jProxyException {
        // 加载配置文件
        Shadowsocks4jProxyConfig shadowsocks4jProxyConfig = loadApplicationJson();

        // 更新 local-server（强制）
        LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE.set(Optional.ofNullable(shadowsocks4jProxyConfig.getLocalServerConfig())
                .orElseThrow(() -> new Shadowsocks4jProxyException("Local server config is null.")));

        // 更新 remote-server（强制）
        REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE.set(Optional.ofNullable(shadowsocks4jProxyConfig.getRemoteServerConfig())
                .orElseThrow(() -> new Shadowsocks4jProxyException("Remote server config is null.")));

        // 更新 cipher
        updateCipherConfig(shadowsocks4jProxyConfig.getCipherConfig());

        // 更新 pac
        updatePacConfig(shadowsocks4jProxyConfig.getPacConfig());
    }

    private static void updateSystemRule() {

    }

    private static void updateUserRule() {

    }

    private static void updateCipherConfig(CipherConfig updatedCipherConfig) throws Shadowsocks4jProxyException {
        CipherConfig cipherConfig = CIPHER_CONFIG_ATOMIC_REFERENCE.get();
        if (cipherConfig == null) {
            cipherConfig = new CipherConfig();
            // 检查 updatedCipherConfig（强制）
            CipherConfig newCipherConfig = Optional.ofNullable(updatedCipherConfig)
                    .orElseThrow(() -> new Shadowsocks4jProxyException("Cipher config is null."));
            // 检查 password（强制）
            cipherConfig.setPassword(Optional.ofNullable(newCipherConfig.getPassword())
                    .orElseThrow(() -> new Shadowsocks4jProxyException("Cipher password is null.")));
            // 检查 method（可选）
            cipherConfig.setMethod(Optional.ofNullable(newCipherConfig.getMethod())
                    .orElse(DEFAULT_CIPHER_METHOD));
            CIPHER_CONFIG_ATOMIC_REFERENCE.set(cipherConfig);
        } else {
            Optional.ofNullable(updatedCipherConfig)
                    .ifPresent(CIPHER_CONFIG_ATOMIC_REFERENCE::set);
        }
    }

    private static void updatePacConfig(PacConfig updatedPacConfig) throws Shadowsocks4jProxyException {
        PacConfig pacConfig = PAC_CONFIG_ATOMIC_REFERENCE.get();
        if (pacConfig == null) {
            pacConfig = new PacConfig();
            // 检查 updatedPacConfig
            PacConfig newPacConfig = Optional.ofNullable(updatedPacConfig)
                    .orElseGet(() -> new PacConfig(Boolean.FALSE));
            // 检查 enablePacMode
            boolean enablePacMode = Optional.ofNullable(newPacConfig.isEnablePacMode())
                    .orElse(Boolean.FALSE);
            pacConfig.setEnablePacMode(enablePacMode);
            if (enablePacMode) {
                // 检查 systemRule
                pacConfig.setSystemRuleConfig(Optional.ofNullable(newPacConfig.getSystemRuleConfig())
                        .orElseGet(() -> new RuleConfig(DEFAULT_SYSTEM_RULE_LOCATION, DEFAULT_SYSTEM_RULE_UPDATER_URL, DEFAULT_SYSTEM_RULE_UPDATER_INTERVAL)));
                // 检查 userRule（可选）
                pacConfig.setUserRuleConfig(newPacConfig.getUserRuleConfig());
            }
            PAC_CONFIG_ATOMIC_REFERENCE.set(pacConfig);
        } else {
            Optional.ofNullable(updatedPacConfig)
                    .ifPresent(PAC_CONFIG_ATOMIC_REFERENCE::set);
        }
    }

    private static Shadowsocks4jProxyConfig loadApplicationJson() throws Shadowsocks4jProxyException {
        try (InputStream inputStream = new FileInputStream(SHADOWSOCKS4J_PROXY_JSON_LOCATION)) {
            return OBJECT_MAPPER.readValue(inputStream, Shadowsocks4jProxyConfig.class);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    private static void startWatchService() {
        Thread fileMonitorThread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Path.of(SHADOWSOCKS4J_CONF_DIR);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                for (; ; ) {
                    doWatchService(watchService);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }, FILE_MONITOR_THREAD_NAME);

        fileMonitorThread.setDaemon(true);
        fileMonitorThread.start();
    }

    private static void doWatchService(WatchService watchService) {
        try {
            WatchKey watchKey = watchService.take();
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                String fileName = ((Path) watchEvent.context()).getFileName().toString();
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                    resolveCreateEvent(fileName);
                } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                    resolveDeleteEvent(fileName);
                } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                    resolveModifyEvent(fileName);
                }
            }
            watchKey.reset();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        } catch (Shadowsocks4jProxyException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void resolveCreateEvent(String fileName) {

    }

    private static void resolveDeleteEvent(String fileName) {

    }

    private static void resolveModifyEvent(String fileName) throws Shadowsocks4jProxyException {
        switch (fileName) {
            case SHADOWSOCKS4J_PROXY_JSON:
                updateShadowsocks4jProxyConfig();
                break;
            case SYSTEM_RULE_YML:
                updateSystemRule();
                break;
            case USER_RULE_YML:
                updateUserRule();
                break;
            default:
                break;
        }
    }

    public static SocketAddress getLocalServerSocketAddress() {
        ServerConfig localServerConfig = LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(localServerConfig.getIp(), localServerConfig.getPort());
    }

    public static SocketAddress getRemoteServerSocketAddress() {
        ServerConfig remoteServerConfig = REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(remoteServerConfig.getIp(), remoteServerConfig.getPort());
    }

    public static String getCipherPassword() {
        return CIPHER_CONFIG_ATOMIC_REFERENCE.get().getPassword();
    }

    public static String getCipherMethod() {
        return CIPHER_CONFIG_ATOMIC_REFERENCE.get().getMethod();
    }

}
