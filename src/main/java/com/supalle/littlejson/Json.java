package com.supalle.littlejson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json {

    private final boolean isArray;

    private final Object target;

    public Json(boolean isArray, Object target) {
        this.isArray = isArray;
        this.target = target;
    }

    public static Json newRoot() {
        return new Json(true, new ArrayList<>(1));
    }

    public static Json newObject() {
        return new Json(false, new HashMap<String, Object>());
    }

    public static Json newArray() {
        return new Json(true, new ArrayList<>());
    }

    public Map<String, Object> getObject() {
        return (Map<String, Object>) target;
    }

    public List<Object> getArray() {
        return (List<Object>) target;
    }

    public boolean isArray() {
        return isArray;
    }
}
