package com.example.myapplication.ui.friends.adapter;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import android.view.View;

public class UserSearchAdapter extends BasePreviewAdapter {
    public UserSearchAdapter(BaseFragment owner) {
        super(owner,
                new PreviewRow(R.layout.item_user_search_result).with(R.id.tvUserName, owner.getString(R.string.ui_uirows_text_5)).with(R.id.tvUserPhone, owner.getString(R.string.ui_uirows_text_11))
                        .with(R.id.btnAction, owner.getString(R.string.ui_uirows_text_12)).click(() -> owner.dialog(R.layout.dialog_user_info)),
                new PreviewRow(R.layout.item_user_search_result).with(R.id.tvUserName, owner.getString(R.string.ui_uirows_text_13)).with(R.id.tvUserPhone, owner.getString(R.string.ui_uirows_text_14))
                        .with(R.id.btnAction, owner.getString(R.string.ui_uirows_text_12)).click(() -> owner.dialog(R.layout.dialog_user_info)));
    }

    @Override protected void bindActions(View root, PreviewRow row) {
        BaseFragment.on(root, R.id.btnAction, owner::previewNotice);
    }
}
