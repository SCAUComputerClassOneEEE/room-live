package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;

public interface RegistryClient extends Application, MethodNestable {

    enum ReplicationAction {

        REGISTER("register"),
        RENEW("renew"),
        OFFLINE("offline");
        private final String action;

        ReplicationAction(String offline) {
            action = offline;
        }

        String getAction() {
            return action;
        }
    }

    void register() throws Exception;

    void renew(boolean sync) throws Exception;

    void discover(ServiceProvider peer, String appName, boolean sync) throws Exception;

    void replicate(ServiceProvider goalPeerNode, ServiceProvider carryNode, ReplicationAction action, boolean sync, boolean comeFromPeer) throws Exception;

    void offline() throws Exception;

}
