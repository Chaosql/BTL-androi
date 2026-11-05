package com.example.myapplication.ui.friends.adapter;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import com.example.myapplication.ui.main.UiScreen;
import android.view.View;

public class FriendsAdapter extends BasePreviewAdapter {
    public FriendsAdapter(BaseFragment owner, boolean group) {
        super(owner,
                new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, "Minh Anh").with(R.id.tvLastMessage, owner.getString(R.string.ui_uirows_text_2))
                        .with(R.id.tvTime, "14:30").click(() -> owner.go(UiScreen.CHAT)),
                new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, owner.getString(R.string.ui_uirows_text_3)).with(R.id.tvLastMessage, owner.getString(R.string.ui_uirows_text_4))
                        .with(R.id.tvTime, "13:15").click(() -> owner.go(UiScreen.CHAT)),
                new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, group ? owner.getString(R.string.ui_uirows_text_5) : owner.getString(R.string.ui_studyscreenfragment_text_2))
                        .with(R.id.tvLastMessage, group ? owner.getString(R.string.ui_uirows_text_7) : owner.getString(R.string.ui_uirows_text_8))
                        .with(R.id.tvTime, owner.getString(R.string.ui_uirows_text_9)).click(() -> owner.go(group ? UiScreen.PROFILE : UiScreen.GROUP_DETAIL)));
    }

    @Override protected void bindActions(View root, PreviewRow row) {
        BaseFragment.on(root, R.id.btnMore, () -> owner.dialog(R.layout.dialog_friend_options));
    }
}
