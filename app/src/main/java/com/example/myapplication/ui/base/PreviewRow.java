package com.example.myapplication.ui.base;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewRow {
    public final int layout;
    public final Map<Integer, String> text = new LinkedHashMap<>();
    public Runnable click;
    public PreviewRow(int layout) { this.layout = layout; }
    public PreviewRow with(int id, String value) { text.put(id, value); return this; }
    public PreviewRow click(Runnable action) { click = action; return this; }
}
