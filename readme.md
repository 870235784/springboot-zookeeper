1.分布式环境特点
    分布, 并发, 无序

2.分布式常见问题
    网络通信
    网络分区(脑裂):
        当网络发生异常导致分布式系统中部分节点之间的网络延时不断增大，最终导致组成分布式架构的所有节点，只有部分节点
        能够正常通信
    三态:
        成功, 失败, 超时
    分布式事务

3.zookeeper 
    3.1 简介
        zookeeper = 文件系统 + 监听通知机制
    3.2 应用
        数据发布/订阅
        负载均衡
        命名服务
        master选举
        分布式队列
        分布式锁
    3.3 特点
        顺序一致性: 从同一个客户端发起的事务请求，最终会严格按照顺序被应用到zookeeper中
        原子性: 所有的事务请求的处理结果在整个集群中的所有机器上的应用情况是一致的，也就是说，要么整个集群中的所有
                机器都成功应用了某一事务,要么全都不应用
        可靠性: 一旦服务器成功应用了某一个事务数据，并且对客户端做了响应，那么这个数据在整个集群中一定是同步并且保
                留下来的
        实时性: 一旦一个事务被成功应用，客户端就能够立即从服务器端读取到事务变更后的最新数据状态(zookeeper仅仅保证
                在一定时间内，近实时)
    3.4 安装
        3.4.1 单机安装
            第一步: 下载zookeeper的安装包
                http://apache.fayea.com/zookeeper/stable/zookeeper-3.4.10.tar.gz
            第二步: 解压zookeeper 
                tar -zxvf zookeeper-3.4.10.tar.gz
            第三步: cd 到 ZK_HOME/conf  , copy一份zoo.cfg
                cp  zoo_sample.cfg  zoo.cfg
            第四步：服务端启动/终止等 
                zkServer.sh {start|start-foreground|stop|restart|status|upgrade|print-cmd}
            第五步: 客户端启动
                zkCli.sh -server  ip:port
        3.4.2 集群安装 (暂略)
    3.5 zoo.cfg 主配置文件
        tickTime=2000  zookeeper中最小的时间单位长度 （ms）
        initLimit=10   follower节点启动后与leader节点完成数据同步的时间
        syncLimit=5    leader节点和follower节点进行心跳检测的最大延时时间
        dataDir=/tmp/zookeeper  表示zookeeper服务器存储快照文件的目录
        dataLogDir     表示配置 zookeeper事务日志的存储路径，默认指定在dataDir目录下
        clientPort     表示客户端和服务端建立连接的端口号： 2181
    3.6 znode
        3.6.1 概念：zookeeper维护一个类似文件系统的数据结构, 每个子项目都是一个znode节点
        3.6.2 分类：
                persistent, persistent_sequential, ephemeral, ephemeral_sequential
        3.6.3 基本增删查改操作
            ls ${path} : 列举path下的子结点
            create ${path} value : 创建节点
            get ${path} : 查看某个节点的详情
            set ${path} value : 修改某个节点的值
            delete ${path} : 删除某个节点
        3.6.4 节点常用属性
            czxid: 节点被创建时的zxid
            mzxid: 节点被修改时的zxid
            ctime: 节点被创建的时间
            mtime: 节点被修改的时间
            version: 节点的版本号
            cversion: 节点所拥有的子结点的版本号
            aversion: 节点的acl的版本号
            ephereralOwner: 如果此节点伟临时节点, 那么他的值为这个节点拥有者的会话; 否则值为0
        3.6.5 节点特点
            1.znode节点的数据可以有多个版本, 在查询znode数据时, 就需要带上版本信息
                get/set/delete ${path} version
            2.znode可以是临时znode, 由create -e生成的节点, 一旦连接断开, 该znode会被自动删除
            3.临时znode不能有子节点
            4.znode可以自动编号, 由create -s 生成的节点, 如在 create -s /app/node 已存在, 则会
                创建 /app/node00***001节点
            5.znode节点可以被监控, 该目录下某些信息的修改, 如节点数据, 子节点变化时, 可以主动通知
                监控注册的client
    3.7 集群
        3.7.1 角色
            Leader: 主角点, 通过选举机制来, 负责读和写操作
            Follower/Observer: 
                Follower 和 Observer 都能提供读服务, 不能提供写服务。两者区别在于, Observer机器不参与Leader选举过程,
                也不参与写操作的『过半写成功』策略, 因此Observer可以在不影响写性能的情况下提升集群的读性能
    3.8 session会话
        3.8.1 概念
            Session 是指客户端会话。
            在ZooKeeper 中, 一个客户端连接是指客户端和 ZooKeeper 服务器之间的TCP长连接。ZooKeeper 对外的服务端口默认
            是2181, 客户端启动时, 首先会与服务器建立一个TCP连接, 从第一次连接建立开始, 客户端会话的生命周期也开始了, 通
            过这个连接, 客户端能够通过心跳检测和服务器保持有效的会话, 也能够向 ZooKeeper 服务器发送请求并接受响应,同
            时还能通过该连接接收来自服务器的 Watch 事件通知。
        3.8.2 SessionTimeout
            Session 的 SessionTimeout 值用来设置一个客户端会话的超时时间。当由于服务器压力太大、网络故障或是客户端主动断开
            连接等各种原因导致客户端连接断开时, 只要在SessionTimeout 规定的时间内能够重新连接上集群中任意一台服务器, 那么之
            前创建的会话仍然有效。
    3.9 事务操作
        在ZooKeeper中, 能改变ZooKeeper服务器状态的操作称为事务操作。一般包括数据节点创建与删除, 数据内容更新和客户端会话
        创建与失效等操作。对应每一个事务请求, ZooKeeper都会为其分配一个全局唯一的事务ID, 用 ZXID 表示, 通常是一个64位的数字。
        每一个 ZXID对应一次更新操作, 从这些 ZXID 中可以间接地识别出 ZooKeeper 处理这些事务操作请求的全局顺序。
    3.10 java操作api
        3.10.1 java原生api (操作复杂, 不建议使用)
        3.10.2 zkClient (不建议使用)
            step1: jar包引入：
                    <dependency>
                        <groupId>com.101tec</groupId>
                        <artifactId>zkclient</artifactId>
                        <version>0.10</version>
                    </dependency>
        
                         
            
            
           
            

        
