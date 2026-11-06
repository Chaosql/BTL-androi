package com.example.myapplication.ui.friends;

import com.example.myapplication.databinding.FragmentSearchUserBinding;
import com.example.myapplication.ui.friends.adapter.UserSearchAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class SearchUserFragment extends BaseFragment<FragmentSearchUserBinding> {
    public SearchUserFragment() { super(FragmentSearchUserBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        UserSearchAdapter results = new UserSearchAdapter(this);
        list(root, R.id.rvSearchResults, results);
        binding.btnSearch.setOnClickListener(bindingView -> results.filter(binding.edtKeyword.getText().toString()));
        filter(root, R.id.edtKeyword, results);
    }
}
