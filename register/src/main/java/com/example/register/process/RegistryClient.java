package com.example.register.process;

import com.example.register.serviceInfo.MethodInstance;
import com.example.register.serviceInfo.ServiceProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;
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

    void register(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer/*第二次传播*/);

    void renew(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer);

    void offline(ServiceProvider who, boolean sync, boolean callByPeer, boolean secondPeer/*第二次传播*/);
    // get
    void discover(ServiceProvider peer, String appName, boolean sync, Collection<ServiceProvider> exclude) throws Exception;

    void replicate(ServiceProvider carryNode, ReplicationAction action, boolean sync, boolean comeFromPeer) throws Exception;

    ServiceProvider getMyself();

    void antiReplicate(ServiceProvider toWho, String appNames, boolean sync) throws Exception;

    Set<ServiceProvider> find(String appName);

    MethodInstance[] getAllMethodsMapping(String appName, ServiceProvider serviceProvider);
}
