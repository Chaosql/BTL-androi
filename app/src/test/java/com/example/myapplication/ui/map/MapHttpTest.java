package com.example.myapplication.ui.map;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapHttpTest {
    @Test public void triesIpv4BeforeIpv6WithoutDroppingEitherRoute() throws Exception {
        java.net.InetAddress ipv6 = java.net.InetAddress.getByAddress(new byte[16]);
        java.net.InetAddress ipv4 = java.net.InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        java.util.List<java.net.InetAddress> addresses = MapHttp.preferIpv4(host -> java.util.Arrays.asList(ipv6, ipv4)).lookup("maps.test");
        assertEquals(java.util.Arrays.asList(ipv4, ipv6), addresses);
        assertEquals(java.util.Collections.singletonList(ipv6), MapHttp.preferIpv4(host -> java.util.Collections.singletonList(ipv6)).lookup("ipv6.test"));
    }
    @Test public void mapRequestsIdentifyAppAndReuseFreshCachedTiles() throws Exception {
        File directory = Files.createTempDirectory("studydate-map-http").toFile();
        OkHttpClient client = MapHttp.create(directory);
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            server.enqueue(new MockResponse().setHeader("Cache-Control", "public, max-age=3600").setBody("tile-data"));
            Request request = new Request.Builder().url(server.url("/tile.png")).build();
            try (Response first = client.newCall(request).execute()) { assertEquals("tile-data", first.body().string()); }
            try (Response cached = client.newCall(request).execute()) {
                assertEquals("tile-data", cached.body().string()); assertNotNull(cached.cacheResponse());
            }
            assertEquals(1, server.getRequestCount());
            assertTrue(server.takeRequest(2, TimeUnit.SECONDS).getHeader("User-Agent").startsWith("StudyDate/"));
            assertTrue(client.connectTimeoutMillis() > 0); assertTrue(client.readTimeoutMillis() > 0);
            assertTrue(client.callTimeoutMillis() > 0);
        } finally {
            client.cache().delete(); client.dispatcher().executorService().shutdown(); client.connectionPool().evictAll();
            Files.deleteIfExists(directory.toPath());
        }
    }
}
