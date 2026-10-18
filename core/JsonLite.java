package yudxx.kursh.core;

import java.util.ArrayList;
import java.util.List;

public class JsonLite {

    public static class Field {
        public final String path;
        public final String kind;
        public final String text;
        public final int start;
        public final int end;

        public Field(String path, String kind, String text, int start, int end) {
            this.path = path;
            this.kind = kind;
            this.text = text;
            this.start = start;
            this.end = end;
        }
    }

    private final String src;
    private int pos;
    private final List<Field> fields = new ArrayList<>();

    private JsonLite(String src) {
        this.src = src;
    }

    public static List<Field> fields(String text) throws Exception {
        JsonLite parser = new JsonLite(text);
        parser.skipWs();
        parser.parseValue("");
        return parser.fields;
    }

    public static boolean looksLikeJson(String text) {
        if (text == null) return false;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return false;
        char c = trimmed.charAt(0);
        return c == '{' || c == '[';
    }

    public static String patch(String text, List<Field> edits) {
        List<Field> sorted = new ArrayList<>(edits);
        sorted.sort((a, b) -> Integer.compare(b.start, a.start));
        StringBuilder sb = new StringBuilder(text);
        for (Field field : sorted) {
            sb.replace(field.start, field.end, field.text);
        }
        return sb.toString();
    }

    private void parseValue(String path) throws Exception {
        if (pos >= src.length()) return;
        char c = src.charAt(pos);
        if (c == '{') {
            parseObject(path);
        } else if (c == '[') {
            parseArray(path);
        } else if (c == '"') {
            skipString();
        } else {
            parseScalar(path);
        }
    }

    private void parseObject(String path) throws Exception {
        pos++;
        skipWs();
        if (pos < src.length() && src.charAt(pos) == '}') {
            pos++;
            return;
        }
        while (pos < src.length()) {
            skipWs();
            if (src.charAt(pos) != '"') throw new Exception("bad key at " + pos);
            String key = readString();
            skipWs();
            if (pos >= src.length() || src.charAt(pos) != ':') throw new Exception("missing colon at " + pos);
            pos++;
            skipWs();
            String child = path.isEmpty() ? key : path + "." + key;
            parseValue(child);
            skipWs();
            if (pos >= src.length()) return;
            char c = src.charAt(pos);
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == '}') {
                pos++;
                return;
            }
            throw new Exception("bad object at " + pos);
        }
    }

    private void parseArray(String path) throws Exception {
        pos++;
        skipWs();
        if (pos < src.length() && src.charAt(pos) == ']') {
            pos++;
            return;
        }
        int index = 0;
        while (pos < src.length()) {
            skipWs();
            parseValue(path + "[" + index + "]");
            index++;
            skipWs();
            if (pos >= src.length()) return;
            char c = src.charAt(pos);
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == ']') {
                pos++;
                return;
            }
            throw new Exception("bad array at " + pos);
        }
    }

    private void parseScalar(String path) {
        int start = pos;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ',' || c == '}' || c == ']' || c == ' ' || c == '\n' || c == '\r' || c == '\t') break;
            pos++;
        }
        String raw = src.substring(start, pos);
        if (raw.isEmpty()) return;
        if (raw.equals("true") || raw.equals("false")) {
            fields.add(new Field(path, "bool", raw, start, pos));
            return;
        }
        if (raw.equals("null")) return;
        String kind = raw.contains(".") || raw.contains("e") || raw.contains("E") ? "number" : "int";
        if (raw.startsWith("-") && !raw.contains(".")) kind = "int";
        fields.add(new Field(path, kind, raw, start, pos));
    }

    private String readString() throws Exception {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == '\\') {
                pos++;
                if (pos >= src.length()) break;
                char n = src.charAt(pos);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 < src.length()) {
                            sb.append((char) Integer.parseInt(src.substring(pos + 1, pos + 5), 16));
                            pos += 4;
                        }
                        break;
                    default: sb.append(n);
                }
                pos++;
                continue;
            }
            if (c == '"') {
                pos++;
                return sb.toString();
            }
            sb.append(c);
            pos++;
        }
        throw new Exception("unterminated string");
    }

    private void skipString() throws Exception {
        readString();
    }

    private void skipWs() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') pos++;
            else break;
        }
    }
}
