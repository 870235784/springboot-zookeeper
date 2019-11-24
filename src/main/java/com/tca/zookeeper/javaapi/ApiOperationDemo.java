package com.tca.zookeeper.javaapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoua
 * @Date 2019/11/23
 */
@Slf4j
public class ApiOperationDemo {

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int SESSION_TIME_OUT = 5000;

    private static final long SLEEP_TIME = 2;

    private static final Stat STAT = new Stat();

    public static void main(String[] args) throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper(CONNECTION_URL_PORT, SESSION_TIME_OUT, event -> {
            log.info("watch的回调方法被调用, type = {}, state = {}", event.getType(), event.getState());
        });
        TimeUnit.SECONDS.sleep(SLEEP_TIME);

        // 创建节点(创建节点的时候没有事件监听)
        String ephemeralPath = "/javaapi";
        String createResult = zooKeeper.create(ephemeralPath, "hello world".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        log.info("节点创建成功! result = {}", createResult);
        TimeUnit.SECONDS.sleep(SLEEP_TIME);

        // 修改数据节点
        zooKeeper.getData(createResult, event -> {
            log.info("watch的回调方法被调用, type = {}, state = {}, ", event.getType(), event.getState());
            log.info("节点变化前, stat = {}", STAT);
            log.info("节点变化后, stat = {}", STAT);
            try {
                zooKeeper.getData(createResult, true, STAT); // 继续监听
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, STAT); // 添加节点监听
        zooKeeper.setData(createResult, "xxx".getBytes(), -1);
        TimeUnit.SECONDS.sleep(SLEEP_TIME);
        zooKeeper.setData(createResult, "yyy".getBytes(), -1);
        TimeUnit.SECONDS.sleep(SLEEP_TIME);
    }
}
