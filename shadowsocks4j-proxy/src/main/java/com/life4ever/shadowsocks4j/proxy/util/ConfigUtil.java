package com.life4ever.shadowsocks4j.proxy.util;

import com.life4ever.shadowsocks4j.proxy.callback.impl.Shadowsocks4jProxyFileCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.callback.impl.SystemRuleFileEventCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.callback.impl.UserRuleFileEventCallbackImpl;
import com.life4ever.shadowsocks4j.proxy.config.CipherConfig;
import com.life4ever.shadowsocks4j.proxy.config.PacConfig;
import com.life4ever.shadowsocks4j.proxy.config.ServerConfig;
import com.life4ever.shadowsocks4j.proxy.config.Shadowsocks4jProxyConfig;
import com.life4ever.shadowsocks4j.proxy.constant.AEADCipherAlgorithmConstant;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

import static com.life4ever.shadowsocks4j.proxy.constant.AdBlockPlusFilterConstant.DOMAIN_NAME_FUZZY_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.constant.AdBlockPlusFilterConstant.DOMAIN_NAME_PRECISE_PATTERN;
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
import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.update;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.createRuleFile;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.loadConfigurationFile;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.startFileWatchService;
import static com.life4ever.shadowsocks4j.proxy.util.FileUtil.updateFile;
import static com.life4ever.shadowsocks4j.proxy.util.HttpClientUtil.execute;

public class ConfigUtil {

    private static final AtomicReference<ServerConfig> LOCAL_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<ServerConfig> REMOTE_SERVER_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final AtomicReference<PacConfig> PAC_CONFIG_ATOMIC_REFERENCE = new AtomicReference<>();

    private static final Map<MatcherModeEnum, Set<String>> SYSTEM_RULE_WHITE_MAP = new EnumMap<>(MatcherModeEnum.class);

    private static final Map<MatcherModeEnum, Set<String>> USER_RULE_WHITE_MAP = new EnumMap<>(MatcherModeEnum.class);

    private static final Set<String> PRECISE_DOMAIN_NAME_WHITE_SET = new HashSet<>(32);

    private static final Set<String> FUZZY_DOMAIN_NAME_WHITE_SET = new HashSet<>(256);

    private static final ScheduledThreadPoolExecutor SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1);

    private static final Lock LOCK = new ReentrantLock();

    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    private static ShadowsocksProxyModeEnum proxyMode;

    private static ScheduledFuture<?> scheduledFuture;

    private static boolean schedulerIsRunning;

    static {
        SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE.setRemoveOnCancelPolicy(true);
    }

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
        // 检查 updatedCipherConfig（强制）
        CipherConfig newCipherConfig = Optional.ofNullable(updatedCipherConfig)
                .orElseThrow(() -> new Shadowsocks4jProxyException("Cipher config is null!"));
        // 检查 password（强制）
        String password = Optional.ofNullable(newCipherConfig.getPassword())
                .orElseThrow(() -> new Shadowsocks4jProxyException("Cipher password is null!"));
        // 检查 salt（可选）
        String salt = Optional.ofNullable(newCipherConfig.getSalt())
                .orElseGet(() -> new StringBuilder(password).reverse().toString());
        // 检查 method（可选）
        String method = Optional.ofNullable(newCipherConfig.getMethod())
                .orElse(AEADCipherAlgorithmConstant.DEFAULT_CIPHER_METHOD);
        // 更新 CipherUtil
        update(password, salt, method);
    }

    private static void updatePacConfig(PacConfig updatedPacConfig) throws Shadowsocks4jProxyException {
        PacConfig newPacConfig = Optional.ofNullable(updatedPacConfig)
                .orElseGet(() -> new PacConfig(Boolean.FALSE));

        boolean enablePacMode = Optional.ofNullable(newPacConfig.isEnablePacMode())
                .orElse(Boolean.FALSE);

        if (!enablePacMode && schedulerIsRunning) {
            shutdownSystemRuleFileScheduler();
        }

        PacConfig pacConfig = new PacConfig(enablePacMode);
        if (enablePacMode) {
            if (schedulerIsRunning) {
                shutdownSystemRuleFileScheduler();
            }
            PacConfig oldPacConfig = PAC_CONFIG_ATOMIC_REFERENCE.get();
            pacConfig.setUpdateUrl(Optional.ofNullable(newPacConfig.getUpdateUrl())
                    .orElseGet(() -> oldPacConfig.getUpdateUrl() == null ? DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL : oldPacConfig.getUpdateUrl()));
            pacConfig.setUpdateInterval(Optional.ofNullable(newPacConfig.getUpdateInterval())
                    .orElseGet(() -> oldPacConfig.getUpdateInterval() == null ? DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL : oldPacConfig.getUpdateInterval()));
            startSystemRuleFileScheduler(SYSTEM_RULE_TXT_LOCATION, pacConfig.getUpdateUrl(), pacConfig.getUpdateInterval());
            createRuleFile(USER_RULE_TXT_LOCATION);
        }
        PAC_CONFIG_ATOMIC_REFERENCE.set(pacConfig);
    }

    private static void startSystemRuleFileScheduler(String fileLocation, String updateUrl, long updateInterval) throws Shadowsocks4jProxyException {
        createRuleFile(fileLocation);
        scheduledFuture = SYSTEM_RULE_FILE_SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
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
        schedulerIsRunning = true;
        LOG.info("Start scheduled task for system-rule.txt");
    }

    private static void shutdownSystemRuleFileScheduler() {
        scheduledFuture.cancel(false);
        schedulerIsRunning = false;
        LOG.info("Shutdown scheduled task for system-rule.txt");
    }

    public static boolean needRelayToRemoteServer(String domainName) {
        boolean enablePacMode = PAC_CONFIG_ATOMIC_REFERENCE.get().isEnablePacMode();
        if (!enablePacMode) {
            return true;
        }

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

    public static void matchDomainName(String rule, Set<String> preciseDomainNameWhiteList, Set<String> fuzzyDomainNameWhiteList) {
        String domainName;
        Matcher matcher = DOMAIN_NAME_PRECISE_PATTERN.matcher(rule);
        if (matcher.find() && (domainName = matcher.group(2)) != null) {
            preciseDomainNameWhiteList.add(domainName);
            return;
        }
        matcher = DOMAIN_NAME_FUZZY_PATTERN.matcher(rule);
        if (matcher.find() && (domainName = matcher.group(2)) != null) {
            fuzzyDomainNameWhiteList.add(domainName);
        }
    }

    public static void updatePreciseDomainNameWhiteSet(Set<String> updatedPreciseDomainNameWhiteSet, boolean updateSystemRule) {
        Set<String> oldPreciseDomainNameWhiteSet = updateWhiteMap(updatedPreciseDomainNameWhiteSet, PRECISE, updateSystemRule);
        PRECISE_DOMAIN_NAME_WHITE_SET.removeAll(oldPreciseDomainNameWhiteSet);
        PRECISE_DOMAIN_NAME_WHITE_SET.addAll(updatedPreciseDomainNameWhiteSet);
    }

    public static void updateFuzzyDomainNameWhiteSet(Set<String> updatedFuzzyDomainNameWhiteSet, boolean updateSystemRule) {
        Set<String> oldFuzzyDomainNameWhiteSet = updateWhiteMap(updatedFuzzyDomainNameWhiteSet, FUZZY, updateSystemRule);
        FUZZY_DOMAIN_NAME_WHITE_SET.removeAll(oldFuzzyDomainNameWhiteSet);
        FUZZY_DOMAIN_NAME_WHITE_SET.addAll(updatedFuzzyDomainNameWhiteSet);
    }

    private static Set<String> updateWhiteMap(Set<String> updatedDomainNameWhiteSet, MatcherModeEnum matcherMode, boolean updateSystemRule) {
        Set<String> oldDomainNameWhiteSet;
        if (updateSystemRule) {
            oldDomainNameWhiteSet = SYSTEM_RULE_WHITE_MAP.computeIfAbsent(matcherMode, key -> new HashSet<>(32));
            SYSTEM_RULE_WHITE_MAP.put(matcherMode, updatedDomainNameWhiteSet);
        } else {
            oldDomainNameWhiteSet = USER_RULE_WHITE_MAP.computeIfAbsent(matcherMode, key -> new HashSet<>(32));
            USER_RULE_WHITE_MAP.put(matcherMode, updatedDomainNameWhiteSet);
        }
        return oldDomainNameWhiteSet;
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
