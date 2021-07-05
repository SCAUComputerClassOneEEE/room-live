package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Set;

public interface RegistryClient extends Application, MethodNestable {

    enum ReplicationAction {

        REGISTER("register"),
        RENEW("renew"),
        OFFLINE("offline");
        private final String action;

        ReplicationAction(String offline) {
            action = offline;
        }

        public String getAction() {
            return action;
        }
    }

    void register(ServiceProvider who, boolean callByPeer, boolean secondPeer/*第二次传播*/) throws Exception;

    void renew(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer) throws Exception;

    // get
    void discover(String appName, boolean sync) throws Exception;

    void replicate(ServiceProvider carryNode, ReplicationAction action, boolean sync, boolean comeFromPeer) throws Exception;

    void offline(ServiceProvider who, boolean callByPeer, boolean secondPeer/*第二次传播*/) throws Exception;

    ServiceProvider getMyself();

    void antiReplicate(ServiceProvider toWho, List<String> apps, boolean sync) throws JsonProcessingException, Exception;

    Set<ServiceProvider> find(String appName);
}
