package com.tca.zookeeper.distributorlock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhoua
 * @Date 2019/11/24
 */
public class CuratorDistributorLock {

    private static int count = 0;

    private static final String LOCK_PATH = "/lock/curator";

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int SESSION_TIME_OUT = 5000;

    private static final int CONNECTION_TIME_OUT = 5000;

    private static final int BASE_SLEEP_TIME = 1000;

    private static final int MAX_RETRIES = 3;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    private static final Lock NATIVE_LOCK = new ReentrantLock();

    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(10000);

    public static void main(String[] args) throws Exception{
//        noLockMethod();
//        nativeLockMethod();
        distributorLockMethod();
    }

    /**
     * 没有加锁时
     */
    private static void noLockMethod() throws Exception{
        for (int i = 0; i < 10000; i++) {
            threadPool.execute(() -> {
                count++;
                COUNT_DOWN_LATCH.countDown();
            });
        }
        threadPool.shutdown();
        COUNT_DOWN_LATCH.await();
        System.out.println("操作结束, 结果为 count = " + count);
    }

    /**
     * 使用jdk锁
     */
    private static void nativeLockMethod() throws Exception{
        for (int i = 0; i < 10000; i++) {
            threadPool.execute(() -> {
                try {
                    NATIVE_LOCK.lock();
                    count++;
                    COUNT_DOWN_LATCH.countDown();
                } finally {
                    NATIVE_LOCK.unlock();
                }
            });
        }
        threadPool.shutdown();
        COUNT_DOWN_LATCH.await();
        System.out.println("操作结束, 结果为 count = " + count);
    }

    /**
     * 使用分布式锁
     */
    private static void distributorLockMethod() throws Exception{
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString(CONNECTION_URL_PORT)
                .connectionTimeoutMs(CONNECTION_TIME_OUT)
                .sessionTimeoutMs(SESSION_TIME_OUT).retryPolicy(new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES))
                .build();
        curatorFramework.start();
        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, LOCK_PATH);

        for (int i = 0; i < 10000; i++) {
            threadPool.execute(() -> {
                try {
                    interProcessMutex.acquire();
                    count++;
                    System.out.println(count);
                    COUNT_DOWN_LATCH.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        interProcessMutex.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        threadPool.shutdown();
        COUNT_DOWN_LATCH.await();
        System.out.println("操作结束, 结果为 count = " + count);
    }
}
