# 简单的服务注册与发现

> [room-live/register(github.com)](https://github.com/SCAUComputerClassOneEEE/room-live/tree/master/register)

## 项目简介与设计

### 背景

集群中的关系网络是复杂且动态，在微服务时代，模块与模块间是远端进程间的通信（主要是套接字），通过IP : Port的形式访问。

如果为了模块间的调用，把通信地址写死在配置文件，甚至代码中，当旧的机器宕机下线、新的机器上线工作时，这份静态的地址信息将无效，导致调用服务模块也不得不下线维护，非常痛苦。

服务注册与发现的作用就是解决静态通信地址，在动态的集群关系中的维护问题。

### 设计

在分布式中有一个著名的理论——CAP定理，精髓在于要么是AP，要么是CP，要么是CA。

服务注册中心是选择AP还是CP？	

> 服务注册中心的功能：````
>
> - 服务注册：实例将自身服务信息注册到注册中心，这部分信息包括服务的主机IP和服务的Port，以及暴露服务自身状态和访问协议信息等。
> - 服务发现：实例请求注册中心所依赖的服务信息，服务实例通过注册中心，获取到注册到其中的服务实例的信息，通过这些信息去请求它们提供的服务。

对于大规模应用在服务协调的Zookeeper中间件，任何时刻对它的访问得到的都是一致性的数据，但它半数以上的机器下线的时候，Zookeeper将无法服务，所以它没有保证可用性。

在room-live/register中，采用AP，服务注册集群的模式设计成对等（peer to peer）的，优先保证了服务的可用性。

就是说某个或一部分peer节点的意外宕机，不会影响到其他节点的正常运。

这得益于节点对节点的主动尝试连接和失败后切换。只要有一个服务注册中心的节点在运作，可以保证整个服务的可用性。

![](https://raw.githubusercontent.com/hiluyx/cloudingImage/master/img/%E8%AE%BE%E8%AE%A1.png)

当然这样的设计失去的是数据的一致性，例如某个服务提供者的宕机信号可能还来不及广播到服务注册中心的每个peer节点，其他的服务调用者就发现了旧的错误的信息。

但是，**在服务提供的机器的数量足够的情况下，这是可容忍的！**服务注册中心的服务发现让每一类注册的服务能够被调用，可能调用者需要多尝试。虽然者增加了通信的成本，但是这样简单可用的服务注册易于开发维护，健壮性也高。

### 实现

项目分为三层设计

- 服务通信层
- 服务治理层
- 服务存储层

![](https://raw.githubusercontent.com/hiluyx/cloudingImage/master/img/%E6%A8%A1%E5%9E%8B.png)

和两大模块，它们共用服务存储层

- 服务提供者和调用者（DiscoveryNodeProcess）
- 服务注册中心（NameCenterPeerProcess）

> 这两个模块设计成独立的线程 process 。一个服务中具备哪种角色就启用哪些线程。
>
> - node角色：启动DiscoveryNodeProcess；
> - peer角色：启动DiscoveryNodeProcess和NameCenterPeerProcess。

