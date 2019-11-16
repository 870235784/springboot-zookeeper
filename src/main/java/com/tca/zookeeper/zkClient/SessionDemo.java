package com.tca.zookeeper.zkClient;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author zhoua
 * @Date 2019/11/16
 * 客户端连接zookeeper服务
 */
@Slf4j
public class SessionDemo {

    private static final String CONNECTION_URL_PORT = "127.0.0.1:2181";

    private static final int CONNECTION_TIME_OUT = 5000;

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient(CONNECTION_URL_PORT, CONNECTION_TIME_OUT);
        log.info("建立连接成功！{}", zkClient);
    }

}
