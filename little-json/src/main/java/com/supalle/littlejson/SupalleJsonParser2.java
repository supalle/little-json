package com.supalle.littlejson;

import com.alibaba.fastjson2.util.JDKUtils;

public class SupalleJsonParser2 {

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

    public SupalleJsonParser2(String text) {
        this.chars = JDKUtils.getCharArray(text);
    }

    public Json scan() {

//        IntList tokens = new IntList();
        TokenWindow tokens = new TokenWindow(this.chars);

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
        tokens.done();
        return tokens.getResult();
    }

    public Object parseObject() {
        return scan();
//        return scan().getArray().get(0);
    }


    static String text = "[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\"}]";

    public static void main(String[] args) {

        SupalleJsonParser2 parser = new SupalleJsonParser2(text);
        System.out.println(parser.parseObject());


//        List<Character> chars = new ArrayList<>();
//        chars.add(' ');
//        chars.add('\n');
//        chars.add('\r');
//        chars.add('\f');
//        chars.add('\t');
//        chars.add('\b');
//        chars.add('\\');
//        chars.add('\"');
//        chars.add('{');
//        chars.add('}');
//        chars.add('[');
//        chars.add(']');
//        chars.add(',');
//        chars.add(':');
//        chars.sort(Character::compareTo);
//        for (Character c : chars) {
//            System.out.println(("\\" + c.toString()) + " = " + ((int) c));
//        }
    }
}
