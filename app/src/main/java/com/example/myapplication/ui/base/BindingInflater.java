package com.example.myapplication.ui.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.viewbinding.ViewBinding;

public interface BindingInflater<B extends ViewBinding> {
    B inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent);
}
