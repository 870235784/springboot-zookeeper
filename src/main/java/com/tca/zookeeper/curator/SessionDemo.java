package com.tca.zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author zhoua
 * @Date 2019/11/17
 * 创建session
 */
@Slf4j
public class SessionDemo {

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int CONNECTION_TIME_OUT = 5000;

    private static final int SESSION_TIME_OUT = 5000;

    private static final int BASE_SLEEP_TIME = 1000;

    private static final int MAX_RETRIES = 3;

    public static void main(String[] args) {
        // 创建session 方式一
        CuratorFramework curatorFramework1 = CuratorFrameworkFactory.newClient(CONNECTION_URL_PORT, SESSION_TIME_OUT, CONNECTION_TIME_OUT,
                new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES));
        log.info("创建成功！, {}", curatorFramework1);

        // 创建session 方式二
        CuratorFramework curatorFramework2 = CuratorFrameworkFactory.builder().connectString(CONNECTION_URL_PORT).connectionTimeoutMs(CONNECTION_TIME_OUT)
                .sessionTimeoutMs(SESSION_TIME_OUT).retryPolicy(new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES))
                .build();
        log.info("创建成功！, {}", curatorFramework2);
    }

    /**
     * 获取实例并启动
     * @return
     */
    public static CuratorFramework getInstance() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString(CONNECTION_URL_PORT).connectionTimeoutMs(CONNECTION_TIME_OUT)
                .sessionTimeoutMs(SESSION_TIME_OUT).retryPolicy(new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES))
                .build();
        curatorFramework.start();
        return curatorFramework;
    }


}
