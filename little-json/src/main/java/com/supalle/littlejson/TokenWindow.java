package com.supalle.littlejson;

import com.alibaba.fastjson2.util.JDKUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class TokenWindow {

    private static final long SPACE = (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    private static final int INDEX_BASE = (1 << 29) - 1;
    private static final int KIND_BASE = (7 << 29);
    private static final String JSON_ROOT_KEY = "ROOT";
    // kind
    private static final int KIND_WHITESPACE = 0;// whitespace
    private static final int KIND_OBJECT = 1 << 29;// object
    private static final int KIND_ARRAY = 2 << 29;// array
    private static final int KIND_STRING = 3 << 29;// string
    private static final int KIND_LITERAL = 4 << 29;// literal
    private static final int KIND_END = 5 << 29;// }/]
    private static final BiFunction<char[], Boolean, String> FUNCTION;

    static {
        try {
            FUNCTION = JDKUtils.getStringCreatorJDK8();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    private final char[] chars;
    private int prev;
    private int last;
    private boolean initialized;
    private JsonStack stack;
    private String key;

    public TokenWindow(char[] chars) {
        this.chars = chars;
        stack = new JsonStack();
        stack.add(Json.newRoot());
    }

    public void add(int token) {
        if (initialized) {
            last = token;
            doParse();
            this.prev = this.last;
        } else {
            prev = token;
            initialized = true;
        }
    }

    private void doParse() {

        final int prev = this.prev;
        final int kind = (prev & KIND_BASE);
        if (kind == KIND_WHITESPACE) {
            return;
        }
        JsonStack stack = this.stack;
        switch (kind) {
            case KIND_OBJECT: {
                Json parent = stack.peek();
                if (parent.isArray()) {
                    List<Object> items = parent.getArray();
                    Json newJson = Json.newObject();
                    stack.add(newJson);
                    items.add(newJson.getObject());
                } else {
                    Map<String, Object> map = parent.getObject();
                    Json newJson = Json.newObject();
                    stack.add(newJson);
                    map.put(key, newJson.getObject());
                    key = null;
                }
                return;
            }
            case KIND_ARRAY: {
                Json parent = stack.peek();
                if (parent.isArray()) {
                    List<Object> items = parent.getArray();
                    Json newJson = Json.newArray();
                    stack.add(newJson);
                    items.add(newJson.getArray());
                } else {
                    Map<String, Object> map = parent.getObject();
                    Json newJson = Json.newArray();
                    stack.add(newJson);
                    map.put(key, newJson.getArray());
                    key = null;
                }
                return;
            }
            case KIND_STRING: {
                final int index = (prev & INDEX_BASE);
                int nextToken = this.last;
                int nextIndex = (nextToken & INDEX_BASE) - ((nextToken & KIND_BASE) == KIND_END ? 0 : 1);
//                BiFunction<char[], Boolean, String> function = FUNCTION;
//                char[] values = Arrays.copyOfRange(chars, (index + 1), (index + 1) + (nextIndex - index - 2));
//                String s = FUNCTION.apply(values, true);
                String s = new String("new String(chars, (index + 1), (nextIndex - index - 2))");
                Json parent = stack.peek();
                if (parent.isArray()) {
                    List<Object> items = parent.getArray();
                    items.add(s);
                } else {
                    Map<String, Object> map = parent.getObject();
                    if (key != null) {
                        map.put(key, s);
                        key = null;
                    } else {
                        key = s;
                    }
                }
                return;
            }
            case KIND_LITERAL: {
                final int index = (prev & INDEX_BASE);
                int nextToken = this.last;
                int nextIndex = (nextToken & INDEX_BASE) - ((nextToken & KIND_BASE) == KIND_END ? 0 : 1);
                String s = "new String(chars, index, (nextIndex - index))";
//                char[] values = Arrays.copyOfRange(chars, index, index + (nextIndex - index));
//                String s = FUNCTION.apply(values, true);
                Json parent = stack.peek();
                if (parent.isArray()) {
                    List<Object> items = parent.getArray();
                    items.add(s);
                } else {
                    Map<String, Object> map = parent.getObject();
                    if (key != null) {
                        map.put(key, s);
                        key = null;
                    } else {
                        key = s;
                    }
                }
                return;
            }
            case KIND_END: {
                stack.pop();
            }
        }

    }

    public void done() {
        add(0x1FFFFFFF);
    }

    public JsonStack getStack() {
        return stack;
    }

    public Json getResult() {
        return stack.pop();
    }
}
