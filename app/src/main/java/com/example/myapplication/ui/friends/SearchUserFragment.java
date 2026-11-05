package com.example.myapplication.ui.friends;

import com.example.myapplication.ui.friends.adapter.UserSearchAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class SearchUserFragment extends BaseFragment {
    public SearchUserFragment() { super(R.layout.fragment_search_user); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        UserSearchAdapter results = new UserSearchAdapter(this);
        list(root, R.id.rvSearchResults, results);
        on(root, R.id.btnSearch, () -> results.filter(value(root, R.id.edtKeyword)));
        filter(root, R.id.edtKeyword, results);
    }
}
