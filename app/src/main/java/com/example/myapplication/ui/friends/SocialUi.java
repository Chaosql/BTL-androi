package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;

public final class SocialUi {
    public static final String UPDATED = "social_updated";
    public static String value(String value) { return value == null ? "" : value; }
    public static String name(String first, String last) {
        String name = (value(last) + " " + value(first)).trim();
        return name.isEmpty() ? "—" : name;
    }
    public static boolean friend(String status) { return "FRIEND".equals(status) || "ACCEPTED".equals(status); }
    public static boolean pending(String status) { return "PENDING".equals(status); }
    public static void avatar(ImageView view, String url) {
        Glide.with(view).load(url).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(view);
    }
    public static void updated(FragmentActivity activity) {
        activity.getSupportFragmentManager().setFragmentResult(UPDATED, new Bundle());
    }
    private SocialUi() { }
}
