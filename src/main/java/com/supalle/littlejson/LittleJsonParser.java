package com.supalle.littlejson;

import com.alibaba.fastjson2.util.JDKUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LittleJsonParser {

    static String text = "[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\"}]";

    public static void main(String[] args) {

        LittleJsonParser parser = new LittleJsonParser(text);
        Object obj = parser.parse();
        System.out.println(obj);

    }

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

    private final char[] chars;

    public LittleJsonParser(String text) {
        this.chars = JDKUtils.getCharArray(text);
    }

    public Object parse() {
        // Root Stack
        JsonStack stack = JsonStack.newArray(null, new ArrayList<>(1));

        char[] chars = this.chars;
        String key = null;

        for (int i = 0, len = chars.length; i < len; i++) {

            char c = chars[i];

            switch (c) {
                case '"': {
                    boolean wildcard = false;
                    int start = ++i;
                    for (; i < len; i++) {
                        char c1 = chars[i];
                        if (c1 == '"' && !wildcard) {
                            break;
                        } else if (c1 == '\\' && !wildcard) {
                            wildcard = true;
                        } else if (wildcard) {
                            wildcard = false;
                        }
                    }
                    int length = i - start;
                    assert length > 0;
                    String s = new String(chars, start, length);
                    if (key == null) {
                        key = s;
                    } else {
                        stack.push(key, s);
                        key = null;
                    }
                    continue;
                }
                case '{': {
                    stack = JsonStack.newObject(key, stack);
                    key = null;
                    continue;
                }
                case '[': {
                    stack = JsonStack.newArray(key, stack);
                    key = null;
                    continue;
                }
                case '}':
                case ']': {
                    stack = stack.getParent();
                    continue;
                }
                case ':':
                case ',': {
                    continue;
                }
            }

            if (c <= ' ' && ((1L << c) & SPACE) != 0) {
                c = chars[i + 1];
                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    i++;
                    c = chars[i + 1];
                }
                continue;
            }


            int literalIndex = i++;
            int length = 0;
            one:
            for (; i < len; i++) {
                char c1 = chars[i];
                switch (c1) {
                    case '，':
                    case '}':
                    case ']':
                    case '"':
                    case ' ': {
                        i--;
                        length = i - literalIndex;
                        break one;
                    }
                }
                if (c1 <= ' ' && ((1L << c1) & SPACE) != 0) {
                    length = i - literalIndex - 1;
                    break;
                }
            }
            String s = new String(chars, literalIndex, length);
            stack.push(key, s);
            key = null;
        }

        return ((ArrayList)stack.getObject()).get(0);
    }


    @RequiredArgsConstructor
    static class JsonStack {
        private final JsonStack parent;
        private final Map<String, Object> object;
        private final Collection<Object> array;
        private final boolean isArray;

        public void put(String key, Object value) {
            object.put(key, value);
        }

        public void add(Object value) {
            array.add(value);
        }

        public void push(String key, Object value) {
            if (isArray) {
                array.add(value);
                return;
            }
            object.put(key, value);
        }

        public Object getObject() {
            if (isArray) {
                return array;
            }
            return object;
        }

        public JsonStack getParent() {
            return parent;
        }

        public static JsonStack newObject(String key, JsonStack parent) {
            JsonStack stack = new JsonStack(parent, new HashMap<>(), null, false);
            parent.push(key, stack.getObject());
            return stack;
        }

        public static JsonStack newArray(String key, JsonStack parent) {
            JsonStack stack = new JsonStack(parent, null, new ArrayList<>(), true);
            parent.push(key, stack.getObject());
            return stack;
        }

        public static JsonStack newArray(JsonStack parent, Collection<Object> array) {
            return new JsonStack(parent, null, array, true);
        }

    }
}
