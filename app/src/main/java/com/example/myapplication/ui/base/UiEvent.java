package com.example.myapplication.ui.base;

public final class UiEvent<T> {
    private final T value;
    private boolean consumed;
    public UiEvent(T value) { this.value = value; }
    public T consume() { if (consumed) return null; consumed = true; return value; }
}
