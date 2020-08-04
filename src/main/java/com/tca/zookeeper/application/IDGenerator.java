package com.tca.zookeeper.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhoua
 * @Date 2020/8/4
 * 使用zookeeper生成分布式id -- 自增
 */
@Slf4j
public class IDGenerator {

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int CONNECTION_TIME_OUT = 5000;

    private static final int SESSION_TIME_OUT = 5000;

    private static final int BASE_SLEEP_TIME = 1000;

    private static final int MAX_RETRIES = 3;

    private static final String prefix = "/curator/idgenerator/node_";

    private static final CuratorFramework curatorFramework  = CuratorFrameworkFactory.builder().connectString(CONNECTION_URL_PORT).connectionTimeoutMs(CONNECTION_TIME_OUT)
            .sessionTimeoutMs(SESSION_TIME_OUT).retryPolicy(new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES))
            .build();

    static {
        curatorFramework.start();
    }

    private IDGenerator() {}

    /**
     * 生成id
     * @return
     */
    public static Long getId() {
        try {
            String path = curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(prefix);
            return Long.parseLong(path.replace(prefix, ""));
        } catch (Exception e) {
            log.error("生成分布式id出错 ", e);
            return null;
        }
    }


    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for(int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                log.info("获取分布式id = {}", getId());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

    }
}
