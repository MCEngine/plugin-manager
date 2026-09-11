package io.github.mcengine.pluginmanager.common.manager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just enough JSON to read the central server's responses and write its requests.
 *
 * <p>Written rather than depended on because the alternative is shading a JSON
 * library into every platform jar for four call sites, and because a Bukkit
 * plugin that pulls in Gson or Jackson competes with whatever version the server
 * already has on its classpath — a class-loading conflict that surfaces as an
 * unrelated plugin breaking.</p>
 *
 * <p>It is a reader for well-formed JSON, not a validator. Anything malformed
 * raises {@link JsonException}, which the caller reports as a failed poll.</p>
 */
public final class Json {

    private Json() {
    }

    /** Raised for anything this parser cannot read. */
    public static final class JsonException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public JsonException(String message) {
            super(message);
        }
    }

    /** Parses a document into {@link Map}, {@link List}, {@link String}, {@link Double}, {@link Boolean} or {@code null}. */
    public static Object parse(String text) {
        Parser parser = new Parser(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw new JsonException("Trailing content after the JSON document.");
        }
        return value;
    }

    /** Reads a nested string, or {@code null} when any step is missing. */
    public static String string(Object node, String... path) {
        Object value = walk(node, path);
        return value instanceof String text ? text : null;
    }

    /** Reads a nested number as a long, or {@code fallback} when missing. */
    public static long number(Object node, long fallback, String... path) {
        Object value = walk(node, path);
        return value instanceof Double amount ? (long) (double) amount : fallback;
    }

    /** Reads a nested array, or an empty list when missing. */
    @SuppressWarnings("unchecked")
    public static List<Object> array(Object node, String... path) {
        Object value = walk(node, path);
        return value instanceof List<?> list ? (List<Object>) list : List.of();
    }

    private static Object walk(Object node, String... path) {
        Object current = node;
        for (String key : path) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(key);
        }
        return current;
    }

    /** Escapes and quotes a string for inclusion in a document. */
    public static String quote(String value) {
        StringBuilder out = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    private static final class Parser {
        private final String text;
        private int at;

        Parser(String text) {
            this.text = text == null ? "" : text;
        }

        boolean atEnd() {
            return at >= text.length();
        }

        void skipWhitespace() {
            while (at < text.length() && Character.isWhitespace(text.charAt(at))) {
                at++;
            }
        }

        Object readValue() {
            skipWhitespace();
            if (atEnd()) {
                throw new JsonException("Unexpected end of JSON.");
            }
            char c = text.charAt(at);
            return switch (c) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't', 'f' -> readBoolean();
                case 'n' -> readNull();
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            at++; // {
            skipWhitespace();
            if (!atEnd() && text.charAt(at) == '}') {
                at++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                map.put(key, readValue());
                skipWhitespace();
                if (atEnd()) {
                    throw new JsonException("Unterminated object.");
                }
                char c = text.charAt(at++);
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw new JsonException("Expected , or } but found " + c);
                }
            }
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            at++; // [
            skipWhitespace();
            if (!atEnd() && text.charAt(at) == ']') {
                at++;
                return list;
            }
            while (true) {
                list.add(readValue());
                skipWhitespace();
                if (atEnd()) {
                    throw new JsonException("Unterminated array.");
                }
                char c = text.charAt(at++);
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw new JsonException("Expected , or ] but found " + c);
                }
            }
        }

        private String readString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (true) {
                if (atEnd()) {
                    throw new JsonException("Unterminated string.");
                }
                char c = text.charAt(at++);
                if (c == '"') {
                    return out.toString();
                }
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (atEnd()) {
                    throw new JsonException("Unterminated escape.");
                }
                char escape = text.charAt(at++);
                switch (escape) {
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    case '/' -> out.append('/');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (at + 4 > text.length()) {
                            throw new JsonException("Truncated unicode escape.");
                        }
                        out.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
                        at += 4;
                    }
                    default -> throw new JsonException("Unknown escape: \\" + escape);
                }
            }
        }

        private Boolean readBoolean() {
            if (text.startsWith("true", at)) {
                at += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", at)) {
                at += 5;
                return Boolean.FALSE;
            }
            throw new JsonException("Expected a boolean.");
        }

        private Object readNull() {
            if (!text.startsWith("null", at)) {
                throw new JsonException("Expected null.");
            }
            at += 4;
            return null;
        }

        private Double readNumber() {
            int start = at;
            while (at < text.length() && "+-0123456789.eE".indexOf(text.charAt(at)) >= 0) {
                at++;
            }
            if (start == at) {
                throw new JsonException("Expected a value at position " + start);
            }
            try {
                return Double.valueOf(text.substring(start, at));
            } catch (NumberFormatException cause) {
                throw new JsonException("Not a number: " + text.substring(start, at));
            }
        }

        private void expect(char expected) {
            skipWhitespace();
            if (atEnd() || text.charAt(at) != expected) {
                throw new JsonException("Expected " + expected + " at position " + at);
            }
            at++;
        }
    }
}
