package com.example.register.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public static <K, T> Map<K, Set<T>> readMapSetValue(String str, TypeReference<Map<K, Set<T>>> typeReference) throws IOException {
        return mapper.readValue(str, typeReference);
    }
}
