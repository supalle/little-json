package com.supalle.littlejson;

import java.util.ArrayList;
import java.util.List;

public class JsonStack {
    private final List<Json> list = new ArrayList<>();

    public void add(Json json) {
        list.add(json);
    }

    public Json pop() {
        return list.remove(list.size() - 1);
    }

    public Json peek() {
        return list.get(list.size() - 1);
    }

}