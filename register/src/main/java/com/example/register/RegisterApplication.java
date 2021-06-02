package com.example.register;

import com.example.register.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class RegisterApplication {

    public static void main(String[] args) {
        Server server = new Server();
        server.start(8880);
//        SpringApplication.run(RegisterApplication.class, args);
    }

}
