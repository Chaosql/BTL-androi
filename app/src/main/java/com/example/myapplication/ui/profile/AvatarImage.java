package com.example.myapplication.ui.profile;

import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

final class AvatarImage {
    static final int MAX_BYTES = 5 * 1024 * 1024;
    static MultipartBody.Part read(ContentResolver resolver, Uri uri) throws IOException {
        String mime = resolver.getType(uri);
        if (mime == null || !mime.startsWith("image/")) throw new IOException("Invalid image type");
        try (InputStream input = resolver.openInputStream(uri);
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            if (input == null) throw new IOException("Missing image");
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted() || bytes.size() + count > MAX_BYTES)
                    throw new IOException("Image too large or canceled");
                bytes.write(buffer, 0, count);
            }
            byte[] data = bytes.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Invalid image");
            String extension = "image/png".equals(mime) ? ".png" : "image/webp".equals(mime) ? ".webp" : ".jpg";
            return MultipartBody.Part.createFormData("file", "avatar" + extension,
                    RequestBody.create(MediaType.parse(mime), data));
        }
    }
    private AvatarImage() { }
}
