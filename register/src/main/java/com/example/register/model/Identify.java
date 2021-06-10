package com.example.register.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Identify {
    private String appName;
    private List<Subscriber> ss = new LinkedList<>();
}
