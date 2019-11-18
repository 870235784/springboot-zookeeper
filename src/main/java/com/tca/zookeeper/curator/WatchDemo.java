package com.tca.zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * @author zhoua
 * @Date 2019/11/18
 * zookeeper 监听事件
 */
@Slf4j
public class WatchDemo {

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        // 获取操作对象
        CuratorFramework curatorFramework = SessionDemo.getInstance();
        // 1.创建节点
        String watchPath = "/watch";
        if (curatorFramework.checkExists().forPath(watchPath) == null) {
            curatorFramework.create().forPath(watchPath, "hello world".getBytes());
        }
        // 2.添加监听 (这种方式只能监听一次)
        byte[] bytes = curatorFramework.getData().usingWatcher(new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                log.info("监听到事件: {}", watchedEvent.getType());
                countDownLatch.countDown();
            }
        }).forPath(watchPath);
        log.info("数据读取成功! {}", new String(bytes));
        // 3.第一次修改数据
        curatorFramework.setData().forPath(watchPath, "xxx".getBytes());
        // 4.第二次修改
        curatorFramework.setData().forPath(watchPath, "yyy".getBytes());
        countDownLatch.await();
        // 5.读取修改结果
        log.info("读取最后结果: {}", new String(curatorFramework.getData().forPath(watchPath)));
    }
}
