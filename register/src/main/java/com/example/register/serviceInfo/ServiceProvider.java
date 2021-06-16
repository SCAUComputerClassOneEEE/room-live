package com.example.register.serviceInfo;

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
}
