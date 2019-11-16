package com.tca.zookeeper.zkClient;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author zhoua
 * @Date 2019/11/17
 * ZkClient 常用api
 */
@Slf4j
public class ZkClientApiOperationDemo {

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int CONNECTION_TIME_OUT = 5000;

    public static void main(String[] args) {
        // 1.建立连接, 获取zkClient对象
        ZkClient zkClient = new ZkClient(CONNECTION_URL_PORT, CONNECTION_TIME_OUT);
        // 2.创建临时节点
        zkClient.createEphemeral("/temp", "hello world".getBytes());
        // 3.删除节点
        boolean delete = zkClient.delete("/persist/hello");
        // 4.创建永久节点(递归创建)
        zkClient.createPersistent("/persist/hello", "hello world");
        // 5.获取节点值
        Object result = zkClient.readData("/persist/hello");
        log.info("读取结果 result = {}", result);
        // 断开连接
        zkClient.close();
    }
}
