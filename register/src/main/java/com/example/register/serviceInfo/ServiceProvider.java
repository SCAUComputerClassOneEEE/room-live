package com.example.register.serviceInfo;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

public class ServiceProvider {

    public static enum TypeServiceProvider {
        Client,
        Server,
    }

    private InstanceInfo info;
    private TypeServiceProvider type;

    public ServiceProvider(String host, int port, TypeServiceProvider type) {
        info = new InstanceInfo(host, port);
        this.type = type;
    }

    public InstanceInfo getInfo() {
        return info;
    }

    public void setInfo(InstanceInfo info) {
        this.info = info;
    }

    public TypeServiceProvider getType() {
        return type;
    }

    public void setType(TypeServiceProvider type) {
        this.type = type;
    }

}
