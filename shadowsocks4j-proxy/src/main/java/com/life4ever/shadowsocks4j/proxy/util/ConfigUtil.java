package com.life4ever.shadowsocks4j.proxy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life4ever.shadowsocks4j.proxy.callback.impl.Shadowsocks4jProxyFileCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.callback.impl.SystemRuleFileEventCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.callback.impl.UserRuleFileEventCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.config.CipherConfig;
import com.life4ever.shadowsocks4j.proxy.config.PacConfig;
import com.life4ever.shadowsocks4j.proxy.config.RuleConfig;
import com.life4ever.shadowsocks4j.proxy.config.ServerConfig;
import com.life4ever.shadowsocks4j.proxy.config.Shadowsocks4jProxyConfig;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.BLANK_STRING;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_CIPHER_METHOD;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.LINE_FEED;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.createRuleFile;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.startFileWatchService;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.updateFile;
import static com.life4ever.shadowsocks4j.proxy.util.HttpClientUtil.execute;

public class ConfigUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    private static final AtomicReference<ServerConfig> LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<ServerConfig> REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<CipherConfig> CIPHER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<PacConfig> PAC_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final ScheduledThreadPoolExecutor SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1);

    private static final List<String> SYSTEM_RULE_PRECISE_DOMAIN_NAME_WHITE_LIST = new ArrayList<>(16);

    private static final List<String> SYSTEM_RULE_FUZZY_DOMAIN_NAME_WHITE_LIST = new ArrayList<>(256);

    static {
        try {
            updateShadowsocks4jProxyConfig();
            startFileWatchService(Arrays.asList(new Shadowsocks4jProxyFileCallbackImpl(),
                    new SystemRuleFileEventCallbackImpl(),
                    new UserRuleFileEventCallbackImpl()));
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
                        .orElseGet(() -> new RuleConfig(DEFAULT_SYSTEM_RULE_TXT_LOCATION,
                                DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL,
                                DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL)));
                // 创建 system-rule.txt 并启动定时器
                startSystemRuleFileScheduler(pacConfig.getSystemRuleConfig());
                // 检查 userRule（可选）
                pacConfig.setUserRuleConfig(Optional.ofNullable(newPacConfig.getUserRuleConfig())
                        .orElseGet(RuleConfig::new));
                // 创建 user-rule.txt
                String userRuleFileLocation = pacConfig.getUserRuleConfig().getLocation();
                if (userRuleFileLocation != null) {
                    createRuleFile(userRuleFileLocation);
                }
            }
            PAC_CONFIG_ATOMIC_REFERENCE.set(pacConfig);
        } else {
            Optional.ofNullable(updatedPacConfig)
                    .ifPresent(PAC_CONFIG_ATOMIC_REFERENCE::set);
        }
    }

    private static void startSystemRuleFileScheduler(RuleConfig systemRuleConfig) throws Shadowsocks4jProxyException {
        createRuleFile(systemRuleConfig.getLocation());
        SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
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

    private static void writeSystemRule(String base64DecodedString) throws Shadowsocks4jProxyException {
        PacConfig pacConfig = PAC_CONFIG_ATOMIC_REFERENCE.get();
        String systemRuleLocation = pacConfig.getSystemRuleConfig().getLocation();
        updateFile(systemRuleLocation, base64DecodedString);
    }

    private static Shadowsocks4jProxyConfig loadConfigurationFile() throws Shadowsocks4jProxyException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(SHADOWSOCKS4J_PROXY_JSON_LOCATION))) {
            return OBJECT_MAPPER.readValue(bufferedReader, Shadowsocks4jProxyConfig.class);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    public static List<String> getSystemRulePreciseDomainNameWhiteList() {
        return SYSTEM_RULE_PRECISE_DOMAIN_NAME_WHITE_LIST;
    }

    public static List<String> getSystemRuleFuzzyDomainNameWhiteList() {
        return SYSTEM_RULE_FUZZY_DOMAIN_NAME_WHITE_LIST;
    }

    public static SocketAddress getLocalServerSocketAddress() {
        ServerConfig localServerConfig = LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(localServerConfig.getIp(), localServerConfig.getPort());
    }

    public static SocketAddress getRemoteServerSocketAddress() {
        ServerConfig remoteServerConfig = REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(remoteServerConfig.getIp(), remoteServerConfig.getPort());
    }

    public static String getSystemRuleLocation() {
        RuleConfig systemRuleConfig = PAC_CONFIG_ATOMIC_REFERENCE.get().getSystemRuleConfig();
        return systemRuleConfig.getLocation();
    }

    public static String getUserRuleLocation() {
        RuleConfig userRuleConfig = PAC_CONFIG_ATOMIC_REFERENCE.get().getUserRuleConfig();
        return userRuleConfig.getLocation();
    }

}
