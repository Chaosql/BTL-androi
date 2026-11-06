package com.example.myapplication.data.remote.websocket;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class StompCodec {
    private byte[] pending = new byte[0];
    public static final class Frame {
        public final String command;
        public final Map<String, String> headers;
        public final String body;
        Frame(String command, Map<String, String> headers, String body) {
            this.command = command; this.headers = headers; this.body = body;
        }
    }
    public List<Frame> feed(String text) {
        byte[] added = text.getBytes(StandardCharsets.UTF_8);
        if (pending.length + added.length > 1024 * 1024) throw new IllegalArgumentException("STOMP frame too large");
        byte[] bytes = Arrays.copyOf(pending, pending.length + added.length);
        System.arraycopy(added, 0, bytes, pending.length, added.length);
        List<Frame> frames = new ArrayList<>();
        int start = 0;
        while (start < bytes.length) {
            while (start < bytes.length && (bytes[start] == '\n' || bytes[start] == '\r')) start++;
            int headerEnd = -1, bodyStart = -1;
            for (int i = start; i + 1 < bytes.length; i++) {
                if (bytes[i] == '\n' && bytes[i + 1] == '\n') { headerEnd = i; bodyStart = i + 2; break; }
                if (i + 3 < bytes.length && bytes[i] == '\r' && bytes[i + 1] == '\n' && bytes[i + 2] == '\r' && bytes[i + 3] == '\n') {
                    headerEnd = i; bodyStart = i + 4; break;
                }
            }
            if (headerEnd < 0) break;
            String[] lines = new String(bytes, start, headerEnd - start, StandardCharsets.UTF_8).replace("\r", "").split("\n");
            Map<String, String> headers = new LinkedHashMap<>();
            for (int i = 1; i < lines.length; i++) {
                int colon = lines[i].indexOf(':');
                if (colon > 0) headers.putIfAbsent(unescape(lines[i].substring(0, colon)), unescape(lines[i].substring(colon + 1)));
            }
            int end = bodyStart;
            if (headers.containsKey("content-length")) {
                int size = Integer.parseInt(headers.get("content-length"));
                if (size < 0 || size > 1024 * 1024) throw new IllegalArgumentException("Invalid STOMP length");
                end += size;
                if (end >= bytes.length) break;
                if (bytes[end] != 0) throw new IllegalArgumentException("Invalid STOMP terminator");
            } else {
                while (end < bytes.length && bytes[end] != 0) end++;
                if (end == bytes.length) break;
            }
            frames.add(new Frame(lines[0], headers, new String(bytes, bodyStart, end - bodyStart, StandardCharsets.UTF_8)));
            start = end + 1;
        }
        pending = Arrays.copyOfRange(bytes, start, bytes.length);
        return frames;
    }
    public static String build(String command, Map<String, String> headers, String body) {
        StringBuilder frame = new StringBuilder(command).append('\n');
        for (Map.Entry<String, String> header : headers.entrySet())
            frame.append(escape(header.getKey())).append(':').append(escape(header.getValue())).append('\n');
        if (!body.isEmpty()) frame.append("content-length:").append(body.getBytes(StandardCharsets.UTF_8).length).append('\n');
        return frame.append('\n').append(body).append('\0').toString();
    }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n").replace(":", "\\c"); }
    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                result.append(next == 'n' ? '\n' : next == 'r' ? '\r' : next == 'c' ? ':' : next);
            } else result.append(character);
        }
        return result.toString();
    }
}
