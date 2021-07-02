package com.example.register.process;


import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.client.ProcessedRunnable;
import com.example.register.trans.client.ResultType;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    protected static ServiceApplicationsTable table;
    protected ApplicationClient client;
    protected volatile ServiceProvider myPeer; /*向他发送心跳 hb/30s*/
    protected Set<ServiceProvider> otherPeers;
    protected ServiceProvider mySelf;

    public DiscoveryNodeProcess(ServiceProvidersBootConfig config) throws Exception {
        init(config);
    }

    protected void init(ServiceProvidersBootConfig config) throws Exception {
        mySelf = config.getSelfNode();
        // initialize the table with config and myself
        table = new ServiceApplicationsTable(
                config,
                ServiceApplicationsTable.SERVER_PEER_NODE);
        // initialize the client and server's thread worker for working.
        client = new ApplicationClient(this, config);
        client.start();
        /*
        * register myself
        * */
        register();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() throws Exception {
        offline();
    }


    /**
     * 阻塞
     *
     * 把自身注册到peer
     * 如果返回非2XX
     * table删除该peer，重新调用register
     *
     * 最后修改myPeer, otherPeers
     * */
    @Override
    public final void register() throws Exception {
        if (myPeer == null) {
            Object[] servers = table.getAppsAsSet(ServiceApplicationsTable.SERVER_PEER_NODE).toArray();
            if (servers.length <= 0)
                throw new RuntimeException("server num is zero...no server is running now");
            myPeer = (ServiceProvider) servers[0];
        }
        replicate(myPeer, mySelf, ReplicationAction.REGISTER, true, false);
    }

    @Override
    public void offline() throws Exception {
        replicate(myPeer, mySelf, ReplicationAction.OFFLINE, true, false);
        client.stopThread();
    }

    /**
     * 非阻塞
     *
     * 发送心跳报文到myPeer
     * 收到404，调用register到myPeer
     * 收到500，调用register到otherPeers，table删除该peer
     * */
    @Override
    public final void renew(boolean sync) throws Exception {
        replicate(myPeer, mySelf, ReplicationAction.RENEW, sync, false);
    }

    @Override
    public final void discover(ServiceProvider peer, String appNames, boolean sync) throws Exception {
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byClient(client)
                .access(HttpMethod.GET, "/discover?appNames=" + appNames)
                .connectWith(peer)
                .done(new ProcessedRunnable() {
                    @Override
                    public void successAndThen(HttpResponseStatus status, String resultString) throws Exception {
                        if (status.equals(HttpResponseStatus.CREATED)) {
                            // --> 200 请求成功，对方返回的数据是可解析的
                            Map<String, Set<ServiceProvider>> serviceProviders = JSONUtil.readMapSetValue(resultString);
                            table.compareAndReturnUpdate(serviceProviders);
                        } else if (status.equals(HttpResponseStatus.SEE_OTHER)) {
                            // --> 303 不存在该类数据（对于discover该资源还没有注册，或者已经下线），对方希望请求别的peer
                            /*
                             *选择最优的 server
                             */
                            ServiceProvider optimal = table.getOptimal(ServiceApplicationsTable.SERVER_PEER_NODE);
                            discover(optimal, appNames, sync);
                        } else {
                            // --> 500 服务器异常，重新register到别的peer，再进行discover
                            table.removeApp(peer);
                            register();
                            discover(peer, appNames, sync);
                        }
                    }

                    @Override
                    public void failAndThen(ResultType errorType, String resultString) throws Exception {
                        table.removeApp(peer);
                    }
                }).create();
        /*block to sub taskQueue*/
        executor.sub();
        if (sync) {
            /*wait until executor's doneRunnable end*/
            executor.sync();
        }
    }

    @Override
    public void replicate(ServiceProvider goalPeerNode, ServiceProvider carryNode, ReplicationAction action, boolean sync, boolean comeFromPeer) throws Exception {
        String url = action.getAction();
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byClient(client)
                .access(HttpMethod.POST, url)
                .connectWith(myPeer)
                .done(new ProcessedRunnable() {
                    @Override
                    public void successAndThen(HttpResponseStatus status, String resultString) throws Exception {

                    }

                    @Override
                    public void failAndThen(ResultType errorType, String resultString) throws Exception {
                        if (!action.equals(ReplicationAction.OFFLINE)) {
                            table.removeApp(myPeer);
                            myPeer = null;
                            register();
                        }
                    }
                }).create();
        /*block to sub taskQueue*/
        executor.sub();
        /*wait until executor's doneRunnable end*/
        if (sync)
            executor.sync();
    }
}
