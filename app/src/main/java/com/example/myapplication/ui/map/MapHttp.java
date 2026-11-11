package com.example.myapplication.ui.map;

import android.content.Context;
import com.example.myapplication.BuildConfig;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.maplibre.android.module.http.HttpRequestUtil;

final class MapHttp {
    private static OkHttpClient client;
    static synchronized void install(Context context) {
        if (client == null) client = create(new File(context.getApplicationContext().getCacheDir(), "map-http"));
        HttpRequestUtil.setOkHttpClient(client);
    }
    static OkHttpClient create(File cacheDirectory) {
        return new OkHttpClient.Builder()
                .dns(preferIpv4(okhttp3.Dns.SYSTEM))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS)
                .cache(new Cache(cacheDirectory, 20L * 1024 * 1024))
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "StudyDate/" + BuildConfig.VERSION_NAME
                                + " (Android; https://github.com/Chaosql/BTL-androi)")
                        .build()))
                .build();
    }
    static okhttp3.Dns preferIpv4(okhttp3.Dns resolver) {
        return host -> {
            java.util.List<java.net.InetAddress> addresses = new java.util.ArrayList<>(resolver.lookup(host));
            addresses.sort(java.util.Comparator.comparingInt(address -> address instanceof java.net.Inet4Address ? 0 : 1));
            return addresses;
        };
    }
    private MapHttp() { }
}
