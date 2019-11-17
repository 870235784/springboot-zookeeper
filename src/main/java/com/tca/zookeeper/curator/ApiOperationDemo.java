package com.tca.zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author zhoua
 * @Date 2019/11/17
 */
@Slf4j
public class ApiOperationDemo {

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 获取操作对象
        CuratorFramework curatorFramework = SessionDemo.getInstance();

        // 1.新增节点
        String path = "/curator/curator1";
        if (curatorFramework.checkExists().forPath(path) == null) {
            String result = curatorFramework.create().creatingParentsIfNeeded().forPath(path, "hello world"
                    .getBytes());
            log.info("新增节点成功! result = {}", result);
        }

        // 2.查看节点
        Stat stat = new Stat();
        String readResult = new String(curatorFramework.getData().storingStatIn(stat).forPath(path));
        log.info("读取节点成功！readResult = {}, stat = {}", readResult, stat);

        // 3.更新节点(更新的节点必须存在, 才能调用setData方法)
        Stat setStat = curatorFramework.setData().forPath("/curator", "hello world".getBytes());
        log.info("修改节点成功! setStat = {}", setStat);

        // 4.删除节点
        String callBackPath = "/callback";
        if (curatorFramework.checkExists().forPath(callBackPath) != null) {
            curatorFramework.delete().deletingChildrenIfNeeded().forPath(callBackPath);
            log.info("删除 /callback 及其子节点成功！");
        }

        // 5.异步创建节点并添加回调
        if (curatorFramework.checkExists().forPath(callBackPath) == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            curatorFramework.create().creatingParentsIfNeeded().inBackground((curatorFrameworkCallback, curatorEvent) ->
                {
                    log.info("节点创建完成! {}, {}", curatorEvent.getResultCode(), curatorEvent.getType());
                    log.info("执行回调函数的线程: {}", Thread.currentThread().getName());
                    countDownLatch.countDown();
                }, executor).forPath(callBackPath, "hello world".getBytes());
            countDownLatch.await();
            executor.shutdown();
            log.info("执行主函数的线程: {}", Thread.currentThread().getName());
        }


    }
}
