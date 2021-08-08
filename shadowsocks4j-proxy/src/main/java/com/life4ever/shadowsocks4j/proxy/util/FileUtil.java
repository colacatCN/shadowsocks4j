package com.life4ever.shadowsocks4j.proxy.util;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.BLANK_STRING;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.FILE_MONITOR_THREAD_NAME;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_CONF_DIR;

public class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    private static Map<String, FileEventCallback> FILE_EVENT_CALLBACK_MAP;

    private FileUtil() {
    }

    public static void createRuleFile(String ruleFileLocation) throws Shadowsocks4jProxyException {
        File ruleFile = new File(ruleFileLocation);
        try {
            if (!ruleFile.exists()) {
                Files.createFile(ruleFile.toPath());
            }
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    public static void updateFile(String filePath, String content) throws Shadowsocks4jProxyException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(BLANK_STRING);
            writer.flush();
            writer.write(content);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    public static void startFileWatchService(List<FileEventCallback> fileEventCallbackList) {
        FILE_EVENT_CALLBACK_MAP = fileEventCallbackList.stream()
                .collect(Collectors.toConcurrentMap(FileEventCallback::getFileName, fileEventCallback -> fileEventCallback));

        Thread thread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = FileSystems.getDefault().getPath(SHADOWSOCKS4J_CONF_DIR);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                for (; ; ) {
                    doFileWatchService(watchService);
                }
            } catch (IOException | Shadowsocks4jProxyException e) {
                LOG.error(e.getMessage(), e);
            }
        }, FILE_MONITOR_THREAD_NAME);

        thread.setDaemon(true);
        thread.start();
    }

    private static void doFileWatchService(WatchService watchService) throws Shadowsocks4jProxyException {
        try {
            WatchKey watchKey = watchService.take();
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                String fileName = ((Path) watchEvent.context()).getFileName().toString();

                FileEventCallback fileEventCallback = FILE_EVENT_CALLBACK_MAP.get(fileName);
                if (fileEventCallback == null) {
                    return;
                }

                if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                    fileEventCallback.resolveCreateEvent();
                } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                    fileEventCallback.resolveDeleteEvent();
                } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                    fileEventCallback.resolveModifyEvent();
                }
            }
            watchKey.reset();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

}
