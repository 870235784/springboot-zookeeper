package com.tca.zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoua
 * @Date 2019/11/18
 * 节点监控事件
 *  1.PathChildrenCache
 *  2.NodeCache
 *  3.TreeCache: PathChildrenCache + NodeCache
 */
@Slf4j
public class ZNodeEventDemo {

    private static final int SLEEP_SECONDS = 2;

    public static void main(String[] args) throws Exception {
        nodeCache();
        pathChildrenCache();
    }

    /**
     * nodeCache 演示
     */
    private static void nodeCache() throws Exception {
        CuratorFramework curatorFramework = SessionDemo.getInstance();
        String nodeCachePath = "/nodeCache";
        // 1.创建测试节点
        if (curatorFramework.checkExists().forPath(nodeCachePath) == null) {
            curatorFramework.create().forPath(nodeCachePath, "hello world".getBytes());
        }
        // 2.创建NodeCache对象并开启
        NodeCache nodeCache = new NodeCache(curatorFramework, nodeCachePath);
        nodeCache.start(true);
        // 3.添加监听
        nodeCache.getListenable().addListener(() -> {
            log.info("触发节点监听事件");
            log.info("当前节点修改后的值为: {}", new String(nodeCache.getCurrentData().getData()));
        });
        // 4.主线程修改节点
        curatorFramework.setData().forPath(nodeCachePath, "xxx".getBytes());
        curatorFramework.setData().forPath(nodeCachePath, "yyy".getBytes());
        curatorFramework.setData().forPath(nodeCachePath, "zzz".getBytes());
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
    }

    /**
     * pathChildrenCache 演示
     * @throws Exception
     */
    private static void pathChildrenCache() throws Exception {
        CuratorFramework curatorFramework = SessionDemo.getInstance();
        String nodeCachePath = "/nodeCache";
        // 1.创建测试节点
        if (curatorFramework.checkExists().forPath(nodeCachePath) == null) {
            curatorFramework.create().forPath(nodeCachePath, "hello world".getBytes());
        }
        // 2.创建PathChildrenCache并启动
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, nodeCachePath, true);
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        // 3.添加监听
        pathChildrenCache.getListenable().addListener((curatorFrameworkListener, pathChildrenCacheEvent) -> {
            log.info("触发节点监听事件");
            ChildData childData = pathChildrenCacheEvent.getData();
            switch (pathChildrenCacheEvent.getType()) {
                case CHILD_ADDED:
                    log.info("添加子节点: {} = {}", childData.getPath(), new String(childData.getData()));
                    break;
                case CHILD_UPDATED:
                    log.info("更新子节点: {} = {}", childData.getPath(), new String(childData.getData()));
                    break;
                case CHILD_REMOVED:
                    log.info("删除子节点: {}", childData.getPath());
                    break;
                default:
                    break;
            }
        });
        String childPath = nodeCachePath + "/child";
        // 4.新增子节点
        curatorFramework.create().creatingParentsIfNeeded().forPath(childPath, "hello world".getBytes());
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
        // 5.修改子节点
        curatorFramework.setData().forPath(childPath, "xxx".getBytes());
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
        // 6.删除子节点
        curatorFramework.delete().forPath(childPath);
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
    }
}
