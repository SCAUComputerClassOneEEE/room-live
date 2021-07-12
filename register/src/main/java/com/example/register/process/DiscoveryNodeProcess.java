package com.example.register.process;


import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.client.ProcessedRunnable;
import com.example.register.trans.client.ResultType;
import com.example.register.utils.JSONUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 *
 *
 * 集群服务里的 client，继承 Application 接口
 * 功能：
 * 1. 注册，
 * 2. 续约，
 * 3. 发现，
 * 4. 下线，被动停止服务，一般是进程挂掉续约断了
 *
 * 收到的状态码：
 * GET：discover
 *
 * --> 200 请求成功，对方返回的数据是可解析的
 *
 * --> 303 不存在该类数据（对于discover该资源还没有注册，或者已经下线），对方希望请求别的peer
 * --> 500 服务器异常，重新register到别的peer，再进行discover
 *
 * POST: register
 *
 * --> 201 请求成功，对方注册了该资源并且将replicate到其他peers
 * --> 202 已经注册成功，不做处理
 *
 * --> 500 服务器异常，重新register到别的peer
 *
 * PUT: renew
 *
 * --> 200 请求成功，对方接受了该资源的修改（心跳维持状态）并且将replicate到其他peers
 *
 * --> 404 不存在该实例，对方希望发送register
 * --> 500 服务器异常，重新register到别的peer
 *
 * DELETE: offline
 *
 * --> 200 下线成功
 *
 * --> 303 不存在该类数据（对于offline该资源还没有注册，或者已经下线）
 * --> 409 下线失败，因为对方认为你下线了别的node
 * --> 500 服务器异常，对别的peer进行offline
 *
 * POST: replicate
 *
 * --> 200 请求成功，对方已存在与其版本一致的资源
 * --> 201 请求成功，对方接受了新的资源的创建
 *
 * --> 409 创建失败，对方的版本更高，你的修改会发送资源的冲突，希望发送discover
 * --> 500 服务器异常，重新register到别的peer
 * */
public class DiscoveryNodeProcess implements RegistryClient{

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryNodeProcess.class);

    protected static ServiceApplicationsTable table;
    protected ApplicationClient client;
    protected ServiceProvider mySelf;
    protected volatile boolean stop;

    public boolean flagForStop() { return !stop; }

    public DiscoveryNodeProcess(ApplicationBootConfig config) {
        mySelf = config.getSelfNode();
        // initialize the table with config and myself
        try {
            table = new ServiceApplicationsTable(config);
            // initialize the client and server's thread worker for working.
            client = new ApplicationClient(this, config);
            client.start();
        } catch (Exception e) {
            logger.error("DiscoveryNodeProcess boot error" + e.getMessage());
            throw e;
        }
    }

    @Override
    public void start() {
        /*
         * register myself
         * */
        stop = false;
        /*register(mySelf,true, false, false);
        logger.info("client register to " + (mySelf.isPeer() ? "echo peer" : "first peer"));*/
    }

    @Override
    public void stop() throws Exception {
        if (stop) return;
        stop = true;
        offline(mySelf,true, false, false);
        client.stopThread();
    }

    @Override
    public boolean isRunning() {
        return client.isAlive();
    }

    /* 阻塞
     * 把自身注册到peer
     * 如果返回非2XX
     * table删除该peer，重新调用register
     * 最后修改myPeer, otherPeers*/
    @Override
    public final void register(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer/*第二次传播*/) {
        if (who == null) {
            who = mySelf;
        }
        logger.debug("try to register " + who.toString());
        table.putAppIfAbsent(who.getAppName(), who);
        try {
            if (!secondPeer)
                replicate(who, ReplicationAction.REGISTER, sync, callByPeer);
        } catch (Exception e) {
            logger.error("when register " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public final void offline(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer/*第二次传播*/) {
        if (who == null) {
            who = mySelf;
        }
        logger.debug("try to offline " + who.toString());
        table.removeApp(who);
        try {
            if (!secondPeer)
                replicate(who, ReplicationAction.OFFLINE, sync, callByPeer);
        } catch (Exception e) {
            logger.error("when offline " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* 非阻塞
     * 发送心跳报文到myPeer
     * 收到404，调用register到myPeer
     * 收到500，调用register到otherPeers，table删除该peer*/
    @Override
    public final void renew(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer/*第二次传播， 第三次调用*/) {
        if (who == null) {
            who = mySelf;
        }
        logger.debug("try to renew " + who.toString());
        table.renewApp(who);
        try {
            if (!secondPeer)
                replicate(who, ReplicationAction.RENEW, sync, callByPeer);
        } catch (Exception e) {
            logger.error("when renew " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public final void discover(ServiceProvider peer, String appNames, boolean sync,
                               Collection<ServiceProvider> exclude/*排除没有该资源的*/) throws Exception {
        if (peer == null) {
            if (exclude == null)
                exclude = new HashSet<>();
            peer = table.getOptimalServer(exclude);
        }
        if (peer == null) {
            throw new NullPointerException("table any more server(" + appNames + ") for discovering.");
        }
        final Collection<ServiceProvider> _exclude = exclude;
        final ServiceProvider myConPeer = peer;
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byClient(client)
                .access(HttpMethod.GET, "/discover")
                .connectWith(peer)
                .addHeader("Content-Type", "text/plain")
                .addHeader("content-length", appNames.length())
                .done(new ProcessedRunnable() {
                    @Override
                    public void successAndThen(HttpResponseStatus status, String resultString) throws Exception {
                        if (status.equals(HttpResponseStatus.CREATED)) {
                            // --> 200 请求成功，对方返回的数据是可解析的
                            Map<String, Set<ServiceProvider>> serviceProviders = JSONUtil.readMapSetValue(resultString);
                            Map<String, Set<ServiceProvider>> newerSet = table.compareAndReturnUpdate(serviceProviders);
                            // replicate newerSet
                            StringBuilder newAppNames = new StringBuilder();
                            newerSet.forEach((s, serviceProviders1) -> newAppNames.append(s));
                            newAppNames.deleteCharAt(newAppNames.length() - 1);
                            antiReplicate(myConPeer, newAppNames.toString(), false);
                        } else if (status.equals(HttpResponseStatus.SEE_OTHER)) {
                            // --> 303 不存在该类数据（对于discover该资源还没有注册，或者已经下线），对方希望请求别的peer
                            /*选择最优的 server*/
                            _exclude.add(myConPeer);
                            discover(null, appNames, sync, _exclude);
                        } else {
                            table.removeApp(myConPeer);
                            discover(null, appNames, sync, _exclude);
                        }
                    }

                    @Override
                    public void failAndThen(ResultType errorType, String resultString) {
                        table.removeApp(myConPeer);
                    }
                }).withBody(appNames).create();
        /*block to sub taskQueue*/
        executor.sub();
        if (sync) {
            /*wait until executor's doneRunnable end*/
            executor.sync();
        }
    }

    /*
     *
     * setup callByPeer secondPeer
     *     1          f          f // node 出发
     *     2          t          f // peer 广播
     *     3          t          t // 结束广播
     * */
    @Override
    public final void replicate(ServiceProvider carryNode,
                          ReplicationAction action,
                          boolean sync, boolean callByPeer) throws Exception {
        String body = JSONUtil.writeValue(carryNode);
        List<HttpTaskCarrierExecutor> executors = new LinkedList<>();
        List<ServiceProvider> myPeerTemp = new LinkedList<>();
        ServiceProvider optimalServer = table.getOptimalServer(null);
        if (optimalServer == null)
            throw new NullPointerException("table any more server(default server) for replicating.");
        myPeerTemp.add(optimalServer);
        /* if the replication call by a peer, // comeFromPeer = true
        * it need broadcast this action;
        * else just to myPeer. // comeFromPeer = false*/
        Iterator<ServiceProvider> servers = callByPeer
                ? table.getServers() : myPeerTemp.iterator();
        while (servers.hasNext()) {
            ServiceProvider myConPeer = servers.next();
            if (myConPeer == null ) continue;
            if (!myConPeer.equals(carryNode)/*不向被传播者传播*/ && !myConPeer.equals(mySelf)/*不向自己传播*/) {
                String url = action.getAction();
                HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                        .byClient(client)
                        .access(HttpMethod.POST, "/" + url)
                        .connectWith(myConPeer)
                        .addHeader("REPLICATION", callByPeer ? "PEER" : "NODE")
                        .addHeader("Content-Type", "json")
                        .addHeader("content-length", body.getBytes().length)
                        .done(new ProcessedRunnable() {
                            @Override
                            public void failAndThen(ResultType errorType, String resultString) throws Exception {
                                if (callByPeer) {
                                    offline(myConPeer, false, true, false);
                                    return;
                                }
                                table.removeApp(myConPeer);
                                /* 如果是client的同步操作需要再同步尝试 */
                                replicate(carryNode, action, sync, false);
                            }
                        }).withBody(body).create();
                /*block to sub taskQueue*/
                executor.sub();
                executors.add(executor);
            }
            servers.remove();
        }
        /*wait until executor's doneRunnable end*/
        if (sync)
            for (HttpTaskCarrierExecutor executor : executors) {
                executor.sync();
            }
    }

    @Override
    public final void antiReplicate(ServiceProvider toWho, String appNames, boolean sync) throws Exception {
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byClient(client)
                .access(HttpMethod.POST, "/antiReplicate")
                .connectWith(toWho)
                .done(new ProcessedRunnable() {
                    @Override
                    public void failAndThen(ResultType errorType, String resultString) {
                        offline(toWho, false, false, false);
                    }
                }).withBody(appNames).create();
        /*block to sub taskQueue*/
        executor.sub();
        if (sync)
            executor.sync();
    }

    @Override
    public Set<ServiceProvider> find(String appName) {
        return table.getAppsAsSet(appName);
    }

    @Override
    public ServiceProvider getMyself() {
        return mySelf;
    }
}
