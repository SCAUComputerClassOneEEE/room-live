package com.example.register.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T readValue(String str, Class<T> cl) throws IOException {
        if (StringUtil.isNullOrEmpty(str))
            throw new NullPointerException("str content is null ro empty.");
        return mapper.readValue(str, cl);
    }

    public static String writeValue(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static <K, T> Map<K, Set<T>> readMapSetValue(String str) throws IOException {
        return mapper.readValue(str, new TypeReference<Map<K, Set<T>>>(){});
    }

    public static String printRequest(HttpRequest request) {
        String uri = request.uri();
        HttpMethod method = request.method();
        List<Map.Entry<String, String>> entries = request.headers().entries();
        StringBuilder headersStr = new StringBuilder();
        entries.forEach(e->{
            String value = e.getValue();
            String key = e.getKey();
            headersStr.append("<").append(value).append(":").append(key).append(">");
        });
        return "{uri:" + uri + ", method: " + method + ", headers: " + headersStr;
    }
}
