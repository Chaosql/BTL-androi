package com.example.myapplication;

import com.example.myapplication.data.remote.websocket.StompCodec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class StompCodecTest {
    @Test public void parsesMultipleFramesAndSkipsHeartbeats() {
        List<StompCodec.Frame> frames = new StompCodec().feed("\nCONNECTED\nversion:1.2\n\n\0\nMESSAGE\ndestination:/topic/conversation.c1\n\n{\"id\":\"m1\"}\0");
        assertEquals(2, frames.size()); assertEquals("CONNECTED", frames.get(0).command);
        assertEquals("/topic/conversation.c1", frames.get(1).headers.get("destination"));
    }
    @Test public void buffersPartialFramesAndPreservesWhitespace() {
        StompCodec parser = new StompCodec();
        assertTrue(parser.feed("MESSAGE\ndestination:/topic").isEmpty());
        assertTrue(parser.feed("/conversation.c1\n\n  hello\n").isEmpty());
        assertEquals("  hello\n", parser.feed("\0").get(0).body);
    }
    @Test public void contentLengthCountsUtf8BytesIncludingEmoji() {
        String content = "Xin chào 👍";
        String wire = "MESSAGE\r\ncontent-length:" + content.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + content + "\0";
        assertEquals(content, new StompCodec().feed(wire).get(0).body);
    }
    @Test public void contentLengthAllowsNullInsideBody() {
        assertEquals("a\0b", new StompCodec().feed("MESSAGE\ncontent-length:3\n\na\0b\0").get(0).body);
    }
    @Test public void malformedLengthIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new StompCodec().feed("MESSAGE\ncontent-length:-1\n\nx\0"));
    }
    @Test public void escapedHeadersAreDecoded() {
        Map<String, String> headers = new LinkedHashMap<>(); headers.put("note", "a:b\nc\\d");
        String wire = StompCodec.build("SEND", headers, "{}");
        assertEquals("a:b\nc\\d", new StompCodec().feed(wire).get(0).headers.get("note"));
    }
}
