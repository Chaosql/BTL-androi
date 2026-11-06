package com.example.myapplication;

import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import com.example.myapplication.ui.chat.ChatImage;
import java.io.*;
import java.nio.file.Files;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowContentResolver;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ChatImageTest {
    private ContentResolver resolver;
    private File cache;
    private final Uri uri = Uri.parse("content://chat.image.tests/photo.jpg");
    @Before public void setup() {
        Context context = RuntimeEnvironment.getApplication();
        resolver = context.getContentResolver(); cache = context.getCacheDir();
        ShadowContentResolver.registerProviderInternal("chat.image.tests", new ImageProvider());
    }
    private byte[] jpeg(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.RED);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output); bitmap.recycle();
        return output.toByteArray();
    }
    private void input(Uri uri, byte[] bytes) {
        Shadows.shadowOf(resolver).registerInputStream(uri, new ByteArrayInputStream(bytes));
    }
    private int files() {
        File[] files = cache.listFiles((directory, name) -> name.startsWith("chat_source_") || name.startsWith("chat_image_"));
        return files == null ? 0 : files.length;
    }
    @Test public void largePhotoIsCompressedToJpegWithinBounds() throws Exception {
        input(uri, jpeg(3840, 2160));
        File output;
        try (ChatImage image = ChatImage.prepare(resolver, uri, cache)) {
            output = image.file();
            BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(output.getAbsolutePath(), bounds);
            assertEquals(1280, bounds.outWidth); assertEquals(720, bounds.outHeight);
            assertEquals("image/jpeg", bounds.outMimeType);
            assertEquals("image/jpeg", image.part().body().contentType().toString());
            assertTrue(output.length() > 0);
        }
        assertFalse(output.exists());
    }
    @Test public void cameraExifOrientationIsAppliedToPixels() throws Exception {
        File original = new File(cache, "rotation_test.jpg");
        try {
            Files.write(original.toPath(), jpeg(800, 400));
            ExifInterface exif = new ExifInterface(original);
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90)); exif.saveAttributes();
            input(uri, Files.readAllBytes(original.toPath()));
            try (ChatImage image = ChatImage.prepare(resolver, uri, cache)) {
                BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(image.file().getAbsolutePath(), bounds);
                assertEquals(400, bounds.outWidth); assertEquals(800, bounds.outHeight);
            }
        } finally { original.delete(); }
    }
    @Test public void wrongMimeTypeIsRejected() {
        Uri file = Uri.parse("content://chat.image.tests/file.pdf");
        input(file, jpeg(4, 4)); int before = files();
        assertThrows(IOException.class, () -> ChatImage.prepare(resolver, file, cache));
        assertEquals(before, files());
    }
    @Test public void corruptImageDoesNotLeaveTemporaryFiles() {
        input(uri, new byte[]{1, 2, 3, 4}); int before = files();
        assertThrows(IOException.class, () -> ChatImage.prepare(resolver, uri, cache));
        assertEquals(before, files());
    }
    @Test public void oversizedInputIsRejectedWithoutLeavingFiles() {
        input(uri, new byte[ChatImage.MAX_INPUT_BYTES + 1]); int before = files();
        assertThrows(IOException.class, () -> ChatImage.prepare(resolver, uri, cache));
        assertEquals(before, files());
    }
    @Test public void cancellationCleansUpTemporaryFiles() {
        input(uri, jpeg(8, 4)); int before = files();
        Thread.currentThread().interrupt();
        try { assertThrows(IOException.class, () -> ChatImage.prepare(resolver, uri, cache)); }
        finally { Thread.interrupted(); }
        assertEquals(before, files());
    }
    public static class ImageProvider extends ContentProvider {
        @Override public boolean onCreate() { return true; }
        @Override public String getType(Uri uri) { return uri.getPath().endsWith(".pdf") ? "application/pdf" : "image/jpeg"; }
        @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) { return null; }
        @Override public Uri insert(Uri uri, ContentValues values) { return null; }
        @Override public int delete(Uri uri, String selection, String[] args) { return 0; }
        @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { return 0; }
    }
}
