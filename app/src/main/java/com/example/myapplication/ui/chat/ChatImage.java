package com.example.myapplication.ui.chat;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import java.io.*;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class ChatImage implements Closeable {
    public static final int MAX_INPUT_BYTES = 20 * 1024 * 1024;
    public static final int MAX_EDGE = 1280;
    private final File file;
    private ChatImage(File file) { this.file = file; }
    public File file() { return file; }
    public MultipartBody.Part part() {
        return MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file));
    }
    public static ChatImage prepare(ContentResolver resolver, Uri uri, File cache) throws IOException {
        String type = resolver.getType(uri);
        if (type == null || !type.startsWith("image/")) throw new IOException("Unsupported image type");
        File source = new File(cache, "chat_source_" + UUID.randomUUID());
        File output = new File(cache, "chat_image_" + UUID.randomUUID() + ".jpg");
        Bitmap bitmap = null;
        boolean complete = false;
        try {
            try (InputStream input = resolver.openInputStream(uri); OutputStream destination = new FileOutputStream(source)) {
                if (input == null) throw new IOException("Image unavailable");
                byte[] buffer = new byte[8192];
                int count;
                int total = 0;
                while ((count = input.read(buffer)) != -1) {
                    checkCanceled();
                    total += count;
                    if (total > MAX_INPUT_BYTES) throw new IOException("Image too large");
                    destination.write(buffer, 0, count);
                }
                if (total == 0) throw new IOException("Empty image");
            }
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(source.getAbsolutePath(), bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || bounds.outWidth > 100000 || bounds.outHeight > 100000)
                throw new IOException("Invalid image");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            while (bounds.outWidth / options.inSampleSize > MAX_EDGE * 2 || bounds.outHeight / options.inSampleSize > MAX_EDGE * 2)
                options.inSampleSize *= 2;
            checkCanceled();
            bitmap = BitmapFactory.decodeFile(source.getAbsolutePath(), options);
            if (bitmap == null) throw new IOException("Cannot decode image");
            int orientation = ExifInterface.ORIENTATION_NORMAL;
            try {
                orientation = new ExifInterface(source).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException ignored) { }
            Matrix transform = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: transform.setScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_180: transform.setRotate(180); break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL: transform.setScale(1, -1); break;
                case ExifInterface.ORIENTATION_TRANSPOSE: transform.setRotate(90); transform.postScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_90: transform.setRotate(90); break;
                case ExifInterface.ORIENTATION_TRANSVERSE: transform.setRotate(270); transform.postScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_270: transform.setRotate(270); break;
                default: break;
            }
            if (!transform.isIdentity()) {
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transform, true);
                if (rotated != bitmap) bitmap.recycle();
                bitmap = rotated;
            }
            int edge = Math.max(bitmap.getWidth(), bitmap.getHeight());
            if (edge > MAX_EDGE) {
                float ratio = (float) MAX_EDGE / edge;
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth() * ratio)),
                        Math.max(1, Math.round(bitmap.getHeight() * ratio)), true);
                if (scaled != bitmap) bitmap.recycle();
                bitmap = scaled;
            }
            checkCanceled();
            try (OutputStream destination = new FileOutputStream(output)) {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 80, destination)) throw new IOException("Cannot encode image");
            }
            checkCanceled();
            if (output.length() == 0) throw new IOException("Empty encoded image");
            checkCanceled();
            complete = true;
            return new ChatImage(output);
        } catch (OutOfMemoryError error) {
            throw new IOException("Insufficient memory to decode image", error);
        } finally {
            if (bitmap != null) bitmap.recycle();
            source.delete();
            if (!complete) output.delete();
        }
    }
    private static void checkCanceled() throws IOException {
        if (Thread.currentThread().isInterrupted()) throw new IOException("Image preparation canceled");
    }
    @Override public void close() { file.delete(); }
}
