package com.example.register.process;

import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;

/**
 *
 * 集群中的 server，继承 Application
 * GET：syncAll
 * --> 200 请求成功，对方返回的数据是可解析的
 * --> 303 不存在该类数据（对于syncAll对方尚未全同步结束），对方希望请求别的peer
 * --> 406 对方不支持该资源类型的请求，（server只支持json）
 * --> 500 服务器异常，重新register到别的peer，再进行syncAll
 * POST: replicate
 * --> 200 请求成功，对方已存在与其版本一致的资源
 * --> 201 请求成功，对方接受了新的资源的创建
 * --> 409 创建失败，对方的版本更高，你的修改会发送资源的冲突，希望发送discover
 * --> 500 服务器异常，重新register到别的peer
 * HEAD: isActive
 * --> 200 请求成功，对方返回的请求头包含active内容
 * --> 404 对方不存在需要检测的数据
 * --> 500 服务器异常，重新register到别的peer，再进行isActive
 */
public interface RegistryServer extends Application {

    enum ClusterType {
        SINGLE, P2P
    }

    /**
     *
     * 从 peer 拉取全部数据
     * GET /syncAll
     */
    void syncAll(ServiceProvider peerNode, boolean sync) throws Exception;

    /**
     *
     * 同步数据，对自己的数据表更新，同时向 peer 推更新：
     * - 如果是来自client的更新就发出对peers的同步；
     * - 否则不再向远同步。
     * POST /replicate
     * 把which序列化作为POST报文发送给peerNode
     */
    void replicate(ServiceProvider peerNode, ServiceProvider which, boolean sync) throws Exception;
    /**
     * GET /isActive
     */
    boolean isActive(ServiceProvider provider, boolean sync);

}
