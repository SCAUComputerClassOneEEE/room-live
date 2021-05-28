package com.example.register;

public class DemoTest {

    public DemoTest() {

    }

    @RpcProvider("test")
    public void server() {
        System.out.println(this.getClass().getName() + " for server:" + this);
    }

    @RpcProvider("test1")
    public void server1() {
        System.out.println(this.getClass().getName() + " for server1:" + this);
    }

    @RpcProvider("test2")
    public void server2() {
        System.out.println(this.getClass().getName() + " for server2:" + this);
    }

}
