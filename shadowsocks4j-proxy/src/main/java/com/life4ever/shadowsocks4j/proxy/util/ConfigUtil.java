package com.life4ever.shadowsocks4j.proxy.util;

import com.life4ever.shadowsocks4j.proxy.callback.impl.Shadowsocks4jProxyFileCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.callback.impl.SystemRuleFileEventCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.callback.impl.UserRuleFileEventCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.config.CipherConfig;
import com.life4ever.shadowsocks4j.proxy.config.PacConfig;
import com.life4ever.shadowsocks4j.proxy.config.ServerConfig;
import com.life4ever.shadowsocks4j.proxy.config.Shadowsocks4jProxyConfig;
import com.life4ever.shadowsocks4j.proxy.enums.MatcherModeEnum;
import com.life4ever.shadowsocks4j.proxy.enums.ShadowsocksProxyModeEnum;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.DEFAULT_CIPHER_METHOD;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.SYSTEM_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.USER_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.constant.StringConstant.BLANK_STRING;
import static com.life4ever.shadowsocks4j.proxy.constant.StringConstant.LINE_FEED;
import static com.life4ever.shadowsocks4j.proxy.enums.MatcherModeEnum.FUZZY;
import static com.life4ever.shadowsocks4j.proxy.enums.MatcherModeEnum.PRECISE;
import static com.life4ever.shadowsocks4j.proxy.enums.ShadowsocksProxyModeEnum.LOCAL;
import static com.life4ever.shadowsocks4j.proxy.enums.ShadowsocksProxyModeEnum.REMOTE;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.createRuleFile;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.loadConfigurationFile;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.startFileWatchService;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.updateFile;
import static com.life4ever.shadowsocks4j.proxy.util.HttpClientUtil.execute;

public class ConfigUtil {

    private static final AtomicReference<ServerConfig> LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<ServerConfig> REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<CipherConfig> CIPHER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<PacConfig> PAC_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final Map<MatcherModeEnum, Set<String>> SYSTEM_RULE_WHITE_MAP = new EnumMap<>(MatcherModeEnum.class);

    private static final Map<MatcherModeEnum, Set<String>> USER_RULE_WHITE_MAP = new EnumMap<>(MatcherModeEnum.class);

    private static final Set<String> PRECISE_DOMAIN_NAME_WHITE_SET = new HashSet<>(32);

    private static final Set<String> FUZZY_DOMAIN_NAME_WHITE_SET = new HashSet<>(512);

    private static final ScheduledThreadPoolExecutor SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1);

    private static final Lock LOCK = new ReentrantLock();

    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    private static ShadowsocksProxyModeEnum proxyMode;

    private ConfigUtil() {
    }

    public static void updateShadowsocks4jProxyConfig() throws Shadowsocks4jProxyException {
        // 加载配置文件
        Shadowsocks4jProxyConfig shadowsocks4jProxyConfig = loadConfigurationFile();
        LOG.info("Shadowsocks4j proxy configuration is {}.", shadowsocks4jProxyConfig);

        // 更新 local-server（强制）
        LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE.set(Optional.ofNullable(shadowsocks4jProxyConfig.getLocalServerConfig())
                .orElseThrow(() -> new Shadowsocks4jProxyException("Local server configuration is null.")));

        // 更新 remote-server（强制）
        REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE.set(Optional.ofNullable(shadowsocks4jProxyConfig.getRemoteServerConfig())
                .orElseThrow(() -> new Shadowsocks4jProxyException("Remote server configuration is null.")));

        // 更新 cipher
        updateCipherConfig(shadowsocks4jProxyConfig.getCipherConfig());

        // 更新 pac
        if (LOCAL.equals(proxyMode)) {
            updatePacConfig(shadowsocks4jProxyConfig.getPacConfig());
        }
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
                // 检查 updateUrl（可选）
                pacConfig.setUpdateUrl(Optional.ofNullable(newPacConfig.getUpdateUrl())
                        .orElse(DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL));
                // 检查 updateInterval（可选）
                pacConfig.setUpdateInterval(Optional.ofNullable(newPacConfig.getUpdateInterval())
                        .orElse(DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL));
                // 创建 system-rule.txt，并启动更新定时器
                startSystemRuleFileScheduler(SYSTEM_RULE_TXT_LOCATION, pacConfig.getUpdateUrl(), pacConfig.getUpdateInterval());
                // 创建 user-rule.txt
                createRuleFile(USER_RULE_TXT_LOCATION);
            }
            PAC_CONFIG_ATOMIC_REFERENCE.set(pacConfig);
        } else {
            Optional.ofNullable(updatedPacConfig)
                    .ifPresent(PAC_CONFIG_ATOMIC_REFERENCE::set);
        }
    }

    private static void startSystemRuleFileScheduler(String fileLocation, String updateUrl, Long updateInterval) throws Shadowsocks4jProxyException {
        createRuleFile(fileLocation);
        SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
                () -> {
                    try {
                        String base64EncodedString = execute(updateUrl);
                        String base64DecodedString = new String(Base64.getDecoder().decode(base64EncodedString.replaceAll(LINE_FEED, BLANK_STRING)), StandardCharsets.UTF_8);
                        updateFile(fileLocation, base64DecodedString);
                    } catch (Shadowsocks4jProxyException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                , 0L, updateInterval, TimeUnit.MILLISECONDS);
        LOG.info("Start scheduled executor service for system rule.");
    }

    public static boolean needRelayToRemoteServer(String domainName) {
        LOCK.lock();
        try {
            if (PRECISE_DOMAIN_NAME_WHITE_SET.contains(domainName)) {
                return false;
            } else {
                return FUZZY_DOMAIN_NAME_WHITE_SET.stream()
                        .noneMatch(domainName::endsWith);
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static void updatePreciseDomainNameWhiteSet(Set<String> updatedPreciseDomainNameWhiteSet, boolean updateSystemRule) {
        Set<String> oldPreciseDomainNameWhiteSet;
        if (updateSystemRule) {
            oldPreciseDomainNameWhiteSet = SYSTEM_RULE_WHITE_MAP.computeIfAbsent(PRECISE, matcherMode -> new HashSet<>(16));
            SYSTEM_RULE_WHITE_MAP.put(PRECISE, updatedPreciseDomainNameWhiteSet);
        } else {
            oldPreciseDomainNameWhiteSet = USER_RULE_WHITE_MAP.computeIfAbsent(PRECISE, matcherMode -> new HashSet<>(16));
            USER_RULE_WHITE_MAP.put(PRECISE, updatedPreciseDomainNameWhiteSet);
        }
        PRECISE_DOMAIN_NAME_WHITE_SET.removeAll(oldPreciseDomainNameWhiteSet);
        PRECISE_DOMAIN_NAME_WHITE_SET.addAll(updatedPreciseDomainNameWhiteSet);
    }

    public static void updateFuzzyDomainNameWhiteSet(Set<String> updatedFuzzyDomainNameWhiteSet, boolean updateSystemRule) {
        Set<String> oldFuzzyDomainNameWhiteSet;
        if (updateSystemRule) {
            oldFuzzyDomainNameWhiteSet = SYSTEM_RULE_WHITE_MAP.computeIfAbsent(FUZZY, matcherMode -> new HashSet<>(16));
            SYSTEM_RULE_WHITE_MAP.put(FUZZY, updatedFuzzyDomainNameWhiteSet);
        } else {
            oldFuzzyDomainNameWhiteSet = USER_RULE_WHITE_MAP.computeIfAbsent(FUZZY, matcherMode -> new HashSet<>(16));
            USER_RULE_WHITE_MAP.put(FUZZY, updatedFuzzyDomainNameWhiteSet);
        }
        FUZZY_DOMAIN_NAME_WHITE_SET.removeAll(oldFuzzyDomainNameWhiteSet);
        FUZZY_DOMAIN_NAME_WHITE_SET.addAll(updatedFuzzyDomainNameWhiteSet);
    }

    public static void clearSystemRuleWhiteMap() {
        updatePreciseDomainNameWhiteSet(Collections.emptySet(), true);
        updateFuzzyDomainNameWhiteSet(Collections.emptySet(), true);
    }

    public static void clearUserRuleWhiteMap() {
        updatePreciseDomainNameWhiteSet(Collections.emptySet(), false);
        updateFuzzyDomainNameWhiteSet(Collections.emptySet(), false);
    }

    public static SocketAddress getLocalServerSocketAddress() {
        ServerConfig localServerConfig = LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(localServerConfig.getIp(), localServerConfig.getPort());
    }

    public static SocketAddress getRemoteServerSocketAddress() {
        ServerConfig remoteServerConfig = REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE.get();
        return new InetSocketAddress(remoteServerConfig.getIp(), remoteServerConfig.getPort());
    }

    public static void activateLocalMode() throws Shadowsocks4jProxyException {
        proxyMode = LOCAL;
        updateShadowsocks4jProxyConfig();
        startFileWatchService(Arrays.asList(new Shadowsocks4jProxyFileCallbackImpl(),
                new SystemRuleFileEventCallbackImpl(),
                new UserRuleFileEventCallbackImpl()));
    }

    public static void activateRemoteMode() throws Shadowsocks4jProxyException {
        proxyMode = REMOTE;
        updateShadowsocks4jProxyConfig();
        startFileWatchService(Collections.singletonList(new Shadowsocks4jProxyFileCallbackImpl()));
    }

    public static void lockWhiteList() {
        LOCK.lock();
    }

    public static void unlockWhiteList() {
        LOCK.unlock();
    }

}
