package com.example.register.serviceInfo;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

@Data
public class MethodInstance {
    private String name;

    private String[] value;

    private String[] path;

    private RequestMethod[] method;

    private String[] params;

    private String[] headers;

    private String[] consumes;

    private String[] produces;
}
