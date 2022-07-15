package com.supalle.littlejson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FinalSupalleJsonParser {

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

    public FinalSupalleJsonParser(String text) {
        this.chars = text.toCharArray();
    }

    public IntList scan() {

        IntList tokens = new IntList();

        char[] array = chars;
        int length = array.length;
        boolean inString = false;
        int lastKind = KIND_WHITESPACE;
        boolean wildcard = false;
        for (int i = 0; i < length; i++) {
            char c = array[i];
            if (inString) {
                if (c == '"' && !wildcard) {
                    inString = false;
                } else if (c == '\\' && !wildcard) {
                    wildcard = true;
                } else if (wildcard) {
                    wildcard = false;
                }
                continue;
            }

            switch (c) {
                case '"': {
                    tokens.add((KIND_STRING | i));
                    lastKind = KIND_STRING;
                    inString = true;
                    continue;
                }
                case '{': {
                    tokens.add((KIND_OBJECT | i));
                    lastKind = KIND_OBJECT;
                    continue;
                }
                case '[': {
                    tokens.add((KIND_ARRAY | i));
                    lastKind = KIND_ARRAY;
                    continue;
                }
                case '}':
                case ']': {
                    tokens.add((KIND_END | i));
                    lastKind = KIND_END;
                    continue;
                }
                case ':':
                case ',':
                    continue;
            }

            if (c <= ' ' && ((1L << c) & SPACE) != 0) {
                tokens.add(i);
                lastKind = KIND_WHITESPACE;
                c = array[i + 1];
                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    i++;
                    c = array[i + 1];
                }
                continue;
            }

            if (lastKind == KIND_LITERAL) {
                continue;
            }
            tokens.add((KIND_LITERAL | i));
            lastKind = KIND_LITERAL;
        }

        return tokens;
    }

    public Object parseObject() {
        return parse().getArray().get(0);
    }

    public Json parse() {
        final int[] tokens = scan().getElements();
        final int size = tokens.length;
        final char[] chars = this.chars;
        final JsonStack stack = new JsonStack();
        stack.add(Json.newRoot());
        String key = null;
        for (int i = 0; i < size; i++) {
            final int token = tokens[i];
            final int kind = (token & KIND_BASE);
            if (kind == KIND_WHITESPACE) {
                continue;
            }

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
                    continue;
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
                    continue;
                }
                case KIND_STRING: {
                    final int index = (token & INDEX_BASE);
                    int nextToken = tokens[i + 1];
                    int nextIndex = (nextToken & INDEX_BASE) - ((nextToken & KIND_BASE) == KIND_END ? 0 : 1);
                    String s = new String(chars, (index + 1), (nextIndex - index - 2));
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
                    continue;
                }
                case KIND_LITERAL: {
                    final int index = (token & INDEX_BASE);
                    int nextToken = tokens[i + 1];
                    int nextIndex = (nextToken & INDEX_BASE) - ((nextToken & KIND_BASE) == KIND_END ? 0 : 1);
                    String s = new String(chars, index, (nextIndex - index));
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
                    continue;
                }
                case KIND_END: {
                    stack.pop();
                }
            }
        }
        return stack.pop();
    }

    static String text = "[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\"}]";

    public static void main(String[] args) {

        FinalSupalleJsonParser parser = new FinalSupalleJsonParser(text);
        System.out.println(parser.parse());
        System.out.println(parser.parseObject());

    }

}
