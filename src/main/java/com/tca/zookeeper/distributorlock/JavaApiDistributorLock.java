package com.tca.zookeeper.distributorlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoua
 * @Date 2019/11/24
 */
@Slf4j
public class JavaApiDistributorLock {

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int SESSION_TIME_OUT = 5000;

    private static final String LOCK_PATH_PARENT = "/lock";

    private static final byte[] LOCK_PATH_VALUE = {1};

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private CountDownLatch watcherCountDownLatch = new CountDownLatch(1);

    private String sequence;

    private  ZooKeeper zooKeeper;

    /**
     * 构造器
     */
    public JavaApiDistributorLock() {
        this.zooKeeper = getInstance();
    }


    /**
     * 创建客户端
     * @return
     * @throws Exception
     */
    private ZooKeeper getInstance() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(CONNECTION_URL_PORT, SESSION_TIME_OUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info("创建客户端, 连接成功!");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            log.error("创建客户端失败!", e);
        }
        return zooKeeper;
    }


    /**
     * 获取锁
     * @return
     */
    public boolean lock() {
        try {
            // 1.在/lock 下创建子节点(临时有序节点)
            sequence = zooKeeper.create(LOCK_PATH_PARENT + "/", LOCK_PATH_VALUE, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL).replace(LOCK_PATH_PARENT + "/", "");
            log.info("当前节点为: {}", sequence);
            // 2.获取/lock 下的所有子节点
            List<String> children = zooKeeper.getChildren(LOCK_PATH_PARENT, false);
            // 3.子节点排序
            children.sort(String::compareTo);
            // 4.判断当前创建的子节点是否是最小子节点 是->获取锁成功 否->获取当前子结点的上一个子节点并监控其删除事件
            if (sequence.equals(children.get(0))) {
                log.info("当前节点为最小子节点, 获取锁成功! sequence = {}, threadName = {}",
                        sequence, Thread.currentThread().getName());
                return true;
            }
            log.info("当前节点不是最小节点!开始监控上个节点");
            int preSequenceIndex = children.indexOf(sequence) - 1;
            log.info("当前节点是 {}, 上个节点是: {}", sequence, children.get(preSequenceIndex));
            zooKeeper.exists(LOCK_PATH_PARENT + "/" + children.get(preSequenceIndex), new LockWatcher(watcherCountDownLatch));
            watcherCountDownLatch.await(SESSION_TIME_OUT, TimeUnit.MILLISECONDS);
            log.info("上个节点被删除, 当前节点为最小子节点, 获取锁成功! sequence = {}, threadName = {}",
                    sequence, Thread.currentThread().getName());
            return true;
        } catch (Exception e) {
            log.error("获取锁失败!", e);
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        log.info("释放锁成功！sequence = {}, threadName = {}", sequence, Thread.currentThread().getName());
        try {
            zooKeeper.delete(LOCK_PATH_PARENT + "/" + sequence, -1);
        } catch (Exception e) {
            log.error("释放锁失败!", e);
        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                JavaApiDistributorLock javaApiDistributorLock = new JavaApiDistributorLock();
                javaApiDistributorLock.lock();
                javaApiDistributorLock.unlock();
            });
        }
        executorService.shutdown();
    }

    public static class LockWatcher implements Watcher {

        private CountDownLatch countDownLatch;

        public LockWatcher (CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void process(WatchedEvent event) {
            log.info("{} 节点被删除!", event.getPath());
            countDownLatch.countDown();
        }
    }

}
