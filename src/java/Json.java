import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Minimal JSON reader for the data file; no database or external dependencies. */
final class Json {
    private final String text;
    private int pos;
    private Json(String text) { this.text = text; }
    static Object read(Path path) throws IOException {
        Json parser = new Json(Files.readString(path, StandardCharsets.UTF_8));
        Object result = parser.value();
        parser.space();
        if (parser.pos != parser.text.length()) throw parser.error("Trailing data");
        return result;
    }
    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at character " + pos);
    }
    private void space() { while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++; }
    private boolean take(char c) { space(); if (pos < text.length() && text.charAt(pos) == c) { pos++; return true; } return false; }
    private Object value() {
        space();
        if (pos == text.length()) throw error("Expected value");
        char c = text.charAt(pos);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (text.startsWith("true", pos)) { pos += 4; return true; }
        if (text.startsWith("false", pos)) { pos += 5; return false; }
        if (text.startsWith("null", pos)) { pos += 4; return null; }
        int start = pos;
        if (c == '-') pos++;
        while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
        if (pos < text.length() && text.charAt(pos) == '.') { pos++; while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++; }
        if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
            pos++; if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) pos++;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
        }
        try { return new java.math.BigDecimal(text.substring(start, pos)); }
        catch (NumberFormatException ex) { throw error("Invalid number"); }
    }
    private Map<String,Object> object() {
        take('{'); Map<String,Object> result = new LinkedHashMap<>();
        if (take('}')) return result;
        do { space(); if (pos >= text.length() || text.charAt(pos) != '"') throw error("Expected key");
            String key = string(); if (!take(':')) throw error("Expected colon"); result.put(key, value());
        } while (take(','));
        if (!take('}')) throw error("Expected closing brace"); return result;
    }
    private List<Object> array() {
        take('['); List<Object> result = new ArrayList<>(); if (take(']')) return result;
        do { result.add(value()); } while (take(','));
        if (!take(']')) throw error("Expected closing bracket"); return result;
    }
    private String string() {
        if (!take('"')) throw error("Expected string"); StringBuilder b = new StringBuilder();
        while (pos < text.length()) {
            char c = text.charAt(pos++); if (c == '"') return b.toString();
            if (c == '\\') {
                if (pos == text.length()) throw error("Incomplete escape");
                c = text.charAt(pos++);
                switch (c) {
                    case '"': case '\\': case '/': b.append(c); break;
                    case 'b': b.append('\b'); break; case 'f': b.append('\f'); break;
                    case 'n': b.append('\n'); break; case 'r': b.append('\r'); break; case 't': b.append('\t'); break;
                    case 'u': if (pos + 4 > text.length()) throw error("Incomplete unicode escape");
                        try { b.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16)); }
                        catch (NumberFormatException e) { throw error("Invalid unicode escape"); } pos += 4; break;
                    default: throw error("Invalid escape");
                }
            } else { if (c < 32) throw error("Control character in string"); b.append(c); }
        } throw error("Unterminated string");
    }
}
