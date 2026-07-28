package me.ngcsonsplash.bdslauncher.util;

import java.util.*;
import java.nio.file.*;

public class Json {

    public static Object parse(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.isEmpty()) return null;
        return new Parser(json).parseValue();
    }

    public static Object parseFile(Path file) throws Exception {
        return parse(Files.readString(file));
    }

    public static String stringify(Object obj) {
        return stringify(obj, false, 0);
    }

    public static String prettyPrint(Object obj) {
        return stringify(obj, true, 0);
    }

    @SuppressWarnings("unchecked")
    private static String stringify(Object obj, boolean pretty, int indent) {
        if (obj == null) return "null";
        if (obj instanceof String s) {
            StringBuilder sb = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"', '\\' -> { sb.append('\\'); sb.append(c); }
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> sb.append(c);
                }
            }
            return sb.append('"').toString();
        }
        if (obj instanceof Boolean || obj instanceof Number) return obj.toString();
        if (obj instanceof Map map) {
            if (map.isEmpty()) return "{}";
            String in = pretty ? "  ".repeat(indent) : "";
            String nl = pretty ? "\n" : "";
            StringBuilder sb = new StringBuilder("{").append(nl);
            boolean first = true;
            for (Object key : map.keySet()) {
                if (!first) sb.append(",").append(nl);
                first = false;
                String k = key.toString();
                sb.append(pretty ? "  ".repeat(indent + 1) : "")
                  .append(stringify(k, false, 0)).append(": ")
                  .append(stringify(map.get(k), pretty, indent + 1));
            }
            return sb.append(nl).append(in).append("}").toString();
        }
        if (obj instanceof List list) {
            if (list.isEmpty()) return "[]";
            String in = pretty ? "  ".repeat(indent) : "";
            String nl = pretty ? "\n" : "";
            StringBuilder sb = new StringBuilder("[").append(nl);
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",").append(nl);
                sb.append(pretty ? "  ".repeat(indent + 1) : "")
                  .append(stringify(list.get(i), pretty, indent + 1));
            }
            return sb.append(nl).append(in).append("]").toString();
        }
        return String.valueOf(obj);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object obj) {
        return obj instanceof List ? (List<Object>) obj : null;
    }

    public static String asString(Object obj) {
        return obj instanceof String s ? s : null;
    }

    public static int asInt(Object obj) {
        return obj instanceof Number n ? n.intValue() : 0;
    }

    public static boolean asBool(Object obj) {
        return obj instanceof Boolean b && b;
    }

    private static class Parser {
        private final String json;
        private int pos;

        Parser(String json) { this.json = json; }

        Object parseValue() {
            skipWS();
            if (pos >= json.length()) return null;
            return switch (json.charAt(pos)) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> { pos += 4; yield true; }
                case 'f' -> { pos += 5; yield false; }
                case 'n' -> { pos += 4; yield null; }
                default -> parseNumber();
            };
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWS();
            if (match('}')) return map;
            while (true) {
                skipWS();
                String key = parseString();
                skipWS();
                expect(':');
                map.put(key, parseValue());
                skipWS();
                if (!match(',')) break;
            }
            expect('}');
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWS();
            if (match(']')) return list;
            while (true) {
                list.add(parseValue());
                skipWS();
                if (!match(',')) break;
            }
            expect(']');
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\' && pos < json.length()) {
                    char n = json.charAt(pos++);
                    sb.append(switch (n) {
                        case '"', '\\', '/' -> n;
                        case 'b' -> '\b';
                        case 'f' -> '\f';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case 'u' -> {
                            if (pos + 4 > json.length()) yield '?';
                            yield (char) Integer.parseInt(json.substring(pos, pos + 4), 16);
                        }
                        default -> n;
                    });
                    if (n == 'u') pos += 4;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Number parseNumber() {
            int start = pos;
            if (pos < json.length() && json.charAt(pos) == '-') pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
            boolean isDouble = pos < json.length() && json.charAt(pos) == '.';
            if (isDouble) {
                pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
            }
            if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
                isDouble = true;
                pos++;
                if (pos < json.length() && (json.charAt(pos) == '+' || json.charAt(pos) == '-')) pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
            }
            String num = json.substring(start, pos);
            if (isDouble) return Double.parseDouble(num);
            long v = Long.parseLong(num);
            if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) return (int) v;
            return v;
        }

        void skipWS() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
        }

        boolean match(char c) {
            skipWS();
            if (pos < json.length() && json.charAt(pos) == c) { pos++; return true; }
            return false;
        }

        void expect(char c) {
            if (!match(c)) throw new RuntimeException("Expected '" + c + "' at " + pos);
        }
    }
}
