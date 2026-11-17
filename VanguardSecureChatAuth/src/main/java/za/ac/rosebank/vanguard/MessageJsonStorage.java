package za.ac.rosebank.vanguard;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessageJsonStorage {

    private MessageJsonStorage() {
        // Utility class: it only has static methods, so it should never be instantiated
    }

    //Reads the JSON file into an array of messages; a missing or empty file gives an empty array
    public static List<Message> read(Path path) throws IOException {
        List<Message> messages = new ArrayList<>();
        if (!Files.exists(path)) {
            return messages;
        }
        String content = Files.readString(path, StandardCharsets.UTF_8).trim();
        if (content.isEmpty()) {
            return messages;
        }
        for (Map<String, String> fields : new JsonParser(content).parseArray()) {
            messages.add(toMessage(fields));
        }
        return messages;
    }

    //Writes every message in the array to the file as one JSON array (replaces the old content)
    public static void write(Path path, List<Message> messages) throws IOException {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            json.append(i == 0 ? "\n" : ",\n").append(messages.get(i).toJson());
        }
        json.append(messages.isEmpty() ? "]\n" : "\n]\n");
        Files.writeString(path, json.toString(), StandardCharsets.UTF_8);
    }

    //Turns the fields of one JSON object back into a Message (the hash is rebuilt from the text)
    private static Message toMessage(Map<String, String> fields) throws IOException {
        int messageNumber;
        try {
            messageNumber = Integer.parseInt(fields.getOrDefault("messageNumber", "0"));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid messageNumber in JSON file.", e);
        }
        Message message = new Message(fields.getOrDefault("messageID", ""), messageNumber,
                fields.getOrDefault("recipient", ""), fields.getOrDefault("message", ""));
        message.setSender(fields.getOrDefault("sender", "Unknown"));
        return message;
    }

    /** Small recursive-descent parser for an array of flat JSON objects (RFC 8259, Bray, 2017). */
    private static final class JsonParser {
        private final String text;
        private int pos = 0;

        private JsonParser(String text) {
            this.text = text;
        }

        //array = "[" [ object *( "," object ) ] "]"
        private List<Map<String, String>> parseArray() throws IOException {
            List<Map<String, String>> objects = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                pos++;
            } else {
                boolean more = true;
                while (more) {
                    objects.add(parseObject());
                    skipWhitespace();
                    if (peek() == ',') {
                        pos++;
                    } else {
                        expect(']');
                        more = false;
                    }
                }
            }
            skipWhitespace();
            if (pos != text.length()) {
                throw error("Unexpected content after the end of the array");
            }
            return objects;
        }

        //object = "{" [ member *( "," member ) ] "}" where every value is a string or a number
        private Map<String, String> parseObject() throws IOException {
            Map<String, String> fields = new HashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return fields;
            }
            boolean more = true;
            while (more) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                fields.put(key, parseValue());
                skipWhitespace();
                if (peek() == ',') {
                    pos++;
                } else {
                    expect('}');
                    more = false;
                }
            }
            return fields;
        }

        //A value is either a quoted string or a plain number, true, false or null
        private String parseValue() throws IOException {
            if (peek() == '"') {
                return parseString();
            }
            int start = pos;
            while (pos < text.length() && ",}] \n\r\t".indexOf(text.charAt(pos)) < 0) {
                pos++;
            }
            if (start == pos) {
                throw error("Expected a value");
            }
            return text.substring(start, pos);
        }

        //string = quote *char quote, where a backslash starts an escape sequence
        private String parseString() throws IOException {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') {
                    return value.toString();
                }
                if (c == '\\') {
                    value.append(parseEscape());
                } else {
                    value.append(c);
                }
            }
            throw error("Unterminated string");
        }

        //Handles the escape sequences \" \\ \/ \b \f \n \r \t and \\uXXXX
        private char parseEscape() throws IOException {
            if (pos >= text.length()) {
                throw error("Unfinished escape sequence");
            }
            char c = text.charAt(pos++);
            switch (c) {
                case '"':
                case '\\':
                case '/':
                    return c;
                case 'b':
                    return '\b';
                case 'f':
                    return '\f';
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 't':
                    return '\t';
                case 'u':
                    return parseUnicode();
                default:
                    throw error("Invalid escape sequence \\" + c);
            }
        }

        //Reads the four hex digits that follow \\u
        private char parseUnicode() throws IOException {
            if (pos + 4 > text.length()) {
                throw error("Unfinished unicode escape");
            }
            try {
                char c = (char) Integer.parseInt(text.substring(pos, pos + 4), 16);
                pos += 4;
                return c;
            } catch (NumberFormatException e) {
                throw error("Invalid unicode escape");
            }
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        //Returns the current character without consuming it, or 0 at the end of the text
        private char peek() {
            return pos < text.length() ? text.charAt(pos) : 0;
        }

        private void expect(char expected) throws IOException {
            if (peek() != expected) {
                throw error("Expected '" + expected + "'");
            }
            pos++;
        }

        private IOException error(String problem) {
            return new IOException("Invalid JSON at position " + pos + ": " + problem + ".");
        }
    }
}