package com.example.myapplication.ui.friends.adapter;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import android.view.View;

public class BlockedUsersAdapter extends BasePreviewAdapter {
    public BlockedUsersAdapter(BaseFragment owner) {
        super(owner, new PreviewRow(R.layout.item_blocked_user)
                .with(R.id.tvUserName, owner.getString(R.string.ui_uirows_text_16)).with(R.id.tvUserPhone, owner.getString(R.string.ui_uirows_text_17)));
    }

    @Override protected void bindActions(View root, PreviewRow row) {
        BaseFragment.on(root, R.id.btnUnblock, () -> owner.dialog(R.layout.dialog_confirm_delete));
    }
}
