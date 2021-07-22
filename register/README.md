# 简单的服务注册与发现

> [room-live/register(github.com)](https://github.com/SCAUComputerClassOneEEE/room-live/tree/master/register)

## 未来设想

### 网络分区故障的处理

当出现一定时间内大量的心跳丢失（网络出现分区，peer之间没有办法同步消息），服务注册中心将不再对实例信息进行移除。保证有正确数据的存在，宁可服务某些实例的数据是有误的。


## 项目简介

### 背景

集群中的关系网络是复杂且动态，在微服务时代，模块与模块间是远端进程间的通信（主要是套接字），通过IP : Port的形式访问。

如果为了模块间的调用，把通信地址写死在配置文件，甚至代码中，当旧的机器宕机下线、新的机器上线工作时，这份静态的地址信息将无效，导致调用服务模块也不得不下线维护，非常痛苦。

服务注册与发现的作用就是解决静态通信地址，在动态的集群关系中的维护问题。

### 特点

- 轻量级，与框架无关。
- 支持SpringBoot Starter开箱即用，注解式开发。
- 简单对等，只需要最终一致性，不需要复杂的算法。
- 线性扩展，新增服务注册中心的实例，只会增加 1 的集群内同步网络负担。
- 通讯简单，不需要保持长连接，基于HTTP和JSON进行数据的传输、解析。
- 容错性好，当服务注册中心无法服务，客户端保留实例表的缓存可以继续工作一段时间。

### 缺点

- 语言限制，客户端的实现只能是嵌入式Java Api。
- 功能较少，开发时间短。
- 缺少集群数据监控。
- 仅支持单例上的路由，没有全局的实时负载均衡。

## 项目设计

### 设计

在分布式中有一个著名的理论——CAP定理，要么是AP，要么是CP。

服务注册中心是选择AP还是CP？	

> 服务注册中心的功能：
>
> - 服务注册：实例将自身服务信息注册到注册中心，这部分信息包括服务的主机IP和服务的Port，以及暴露服务自身状态和访问协议信息等。
> - 服务发现：实例请求注册中心所依赖的服务信息，服务实例通过注册中心，获取到注册到其中的服务实例的信息，通过这些信息去请求它们提供的服务。

对于大规模应用在服务协调的Zookeeper中间件，任何时刻对它的访问得到的都是一致性的数据，但它半数以上的机器下线的时候，Zookeeper将无法服务，所以它没有保证可用性。

在room-live/register中，采用AP，服务注册集群的**模式设计成对等（peer to peer）**的，优先保证了服务的可用性。

就是说某个或一部分peer节点的意外宕机，不会影响到其他节点的正常运行。

这得益于节点对节点的主动尝试连接和失败后切换。只要有一个服务注册中心的节点在运作，可以保证整个服务的可用性。

![](https://raw.githubusercontent.com/hiluyx/cloudingImage/master/img/%E8%AE%BE%E8%AE%A1.png)

当然这样的设计失去的是数据的一致性，例如某个服务提供者的宕机信号可能还来不及广播到服务注册中心的每个peer节点，其他的服务调用者就发现了旧的错误的信息。

但是，**在服务提供的机器的数量足够的情况下，这是可容忍的！**服务注册中心的服务发现让每一类注册的服务能够被调用，可能调用者需要多尝试。虽然者增加了通信的成本，但是这样简单可用的服务注册易于开发维护，健壮性也高。

### 实现（Java with Netty）

**一、项目分为三层设计**

- 服务通信层
- 服务治理层
- 服务存储层

![](https://raw.githubusercontent.com/hiluyx/cloudingImage/master/img/%E6%A8%A1%E5%9E%8B.png)

**二、两大模块，它们共用服务存储层**

- 服务提供者和调用者（DiscoveryNodeProcess）
- 服务注册中心（NameCenterPeerProcess）

> 这两个模块设计成独立的线程 process 。一个服务中具备哪种角色就启用哪些线程。
>
> - node角色：启动DiscoveryNodeProcess，对所需服务**实例列表缓存**，采用轮询的负载均衡算法。当超过半数的服务连接失败后，重新拉取该服务实例；
> - peer角色：启动DiscoveryNodeProcess和NameCenterPeerProcess，对集群内的所有服务实例缓存，并且对太久没有心跳的实例进行剔除，并集群内广播。

**三、action抽象**

- 推（push、replicate）

  - 注册（register）
  - 心跳（renew）
  - 下线（offline）

  > 把上述的三个动作抽象成replicate，分两阶段主动向peer推送数据。
  >
  > - node --> peer，广播的第一阶段，服务提供者（node）向服务注册中心（peer）推送数据；
  > - peer  --> peer，广播的第二阶段，服务注册中心集群内同步数据，广播在此阶段阻断。

- 拉（pull、discover）

  - 发现服务（find）
  - 发现服务的api（getAllMethodsMapping）
  - 全量同步（syncAll）

  > 把上述的三个动作抽象成discover，主动到peer拉取数据。
  >
  > - node <-- peer，调用者（node）发现；
  >- peer  <-- peer，新的服务注册中心节点启动，全同步。

## 库安装使用

### mvn本地打包

```shell
mvn clean install
```

### pom.xml添加依赖


```xml
<dependency>
	<groupId>com.luyunxi</groupId>
	<artifactId>register</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

### yml配置

```yml
register:
  serviceType: peer # 指明服务角色
  self:
    host: localhost # 本地实例
    port: 8081
    app-name: server-peer-node-service
  peers:
    - host: localhost # peer集群实例
      port: 8081
      app-name: server-peer-node-service
    - host: localhost
      port: 8083
      app-name: server-peer-node-service
  server-port: 8081
  heart-beat-intervals: 3000
```

### 简单的调用

```java
@RestController
public class HelloController {
    @Resource
    private SpringNameCenterPeerProcess nameCenterPeerProcess; // peer 启动这个

    @Resource
    private SpringDiscoverNodeProcess springDiscoverNodeProcess; // node 启动这个

    @RegisterMapping(value = "/test", name = "testApi")
    public String hello() {
		return "hello world!";
    }
}
```

