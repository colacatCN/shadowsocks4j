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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_FUZZY_MATCHER;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_PRECISE_MATCHER;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.WHITE_LIST_START_FLAG;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.BLANK_STRING;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_CIPHER_METHOD;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.FILE_MONITOR_THREAD_NAME;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.LINE_FEED;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_CONF_DIR;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SYSTEM_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.util.HttpClientUtil.execute;

public class ConfigUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    private static final AtomicReference<ServerConfig> LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<ServerConfig> REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<CipherConfig> CIPHER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<PacConfig> PAC_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final ScheduledThreadPoolExecutor SYSTEM_RULE_FILE_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1);

    private static final List<String> PRECISE_DOMAIN_NAME_WHITE_LIST = new ArrayList<>(16);

    private static final List<String> FUZZY_DOMAIN_NAME_WHITE_LIST = new ArrayList<>(256);

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
        Shadowsocks4jProxyConfig shadowsocks4jProxyConfig = loadConfigurationFile();

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
                        .orElseGet(() -> new RuleConfig(DEFAULT_SYSTEM_RULE_TXT_LOCATION, DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL, DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL)));
                // 创建 system-rule.txt 并启动定时器
                createSystemRuleFile(pacConfig.getSystemRuleConfig());
                // 检查 userRule（可选）
                pacConfig.setUserRuleConfig(newPacConfig.getUserRuleConfig());
                // 创建 user-rule.txt
                createUserRuleFile(pacConfig.getUserRuleConfig());
            }
            PAC_CONFIG_ATOMIC_REFERENCE.set(pacConfig);
        } else {
            Optional.ofNullable(updatedPacConfig)
                    .ifPresent(PAC_CONFIG_ATOMIC_REFERENCE::set);
        }
    }

    private static void createSystemRuleFile(RuleConfig systemRuleConfig) throws Shadowsocks4jProxyException {
        File systemRuleFile = new File(systemRuleConfig.getLocation());
        if (!systemRuleFile.exists()) {
            try {
                Files.createFile(systemRuleFile.toPath());
            } catch (IOException e) {
                throw new Shadowsocks4jProxyException(e.getMessage(), e);
            }
        }

        SYSTEM_RULE_FILE_EXECUTOR_SERVICE.scheduleAtFixedRate(
                () -> {
                    try {
                        String base64EncodedString = execute(systemRuleConfig.getUpdateUrl());
                        String base64DecodedString = new String(Base64.getDecoder().decode(base64EncodedString.replaceAll(LINE_FEED, BLANK_STRING)), StandardCharsets.UTF_8);
                        writeSystemRule(base64DecodedString);
                    } catch (Shadowsocks4jProxyException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                , 0L, systemRuleConfig.getUpdateInterval(), TimeUnit.MILLISECONDS);
    }

    private static void createUserRuleFile(RuleConfig userRuleConfig) throws Shadowsocks4jProxyException {
        File userRuleFile = new File(userRuleConfig.getLocation());
        if (!userRuleFile.exists()) {
            try {
                Files.createFile(userRuleFile.toPath());
            } catch (IOException e) {
                throw new Shadowsocks4jProxyException(e.getMessage(), e);
            }
        }
    }

    private static void writeSystemRule(String base64DecodedString) throws Shadowsocks4jProxyException {
        PacConfig pacConfig = PAC_CONFIG_ATOMIC_REFERENCE.get();
        String systemRuleLocation = pacConfig.getSystemRuleConfig().getLocation();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(systemRuleLocation))) {
            writer.write(BLANK_STRING);
            writer.flush();
            writer.write(base64DecodedString);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    private static Shadowsocks4jProxyConfig loadConfigurationFile() throws Shadowsocks4jProxyException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(SHADOWSOCKS4J_PROXY_JSON_LOCATION))) {
            return OBJECT_MAPPER.readValue(bufferedReader, Shadowsocks4jProxyConfig.class);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    private static void startWatchService() {
        Thread fileMonitorThread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = FileSystems.getDefault().getPath(SHADOWSOCKS4J_CONF_DIR);
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
            case SYSTEM_RULE_TXT:
                updateSystemRule();
                break;
            case USER_RULE_TXT:
                updateUserRule();
                break;
            default:
                break;
        }
    }

    private static void updateSystemRule() throws Shadowsocks4jProxyException {
        PacConfig pacConfig = PAC_CONFIG_ATOMIC_REFERENCE.get();
        String systemRuleLocation = pacConfig.getSystemRuleConfig().getLocation();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(systemRuleLocation))) {
            String line;
            boolean whiteListStart = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(WHITE_LIST_START_FLAG)) {
                    whiteListStart = true;
                }
                if (whiteListStart) {
                    if (line.startsWith(DOMAIN_NAME_PRECISE_MATCHER)) {
                        PRECISE_DOMAIN_NAME_WHITE_LIST.add(line);
                    }
                    if (line.startsWith(DOMAIN_NAME_FUZZY_MATCHER)) {
                        FUZZY_DOMAIN_NAME_WHITE_LIST.add(line);
                    }
                }
            }
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    private static void updateUserRule() {

    }

    public static SocketAddress getLocalServerSocketAddress() {
        ServerConfig localServerConfig = LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(localServerConfig.getIp(), localServerConfig.getPort());
    }

    public static SocketAddress getRemoteServerSocketAddress() {
        ServerConfig remoteServerConfig = REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(remoteServerConfig.getIp(), remoteServerConfig.getPort());
    }

}
