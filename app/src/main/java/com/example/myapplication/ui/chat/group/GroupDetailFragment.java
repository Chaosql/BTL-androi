package com.example.myapplication.ui.chat.group;

import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import com.example.myapplication.ui.main.UiScreen;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;

public class GroupDetailFragment extends BaseFragment<FragmentFriendsListBinding> {
    public GroupDetailFragment() { super(FragmentFriendsListBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        BasePreviewAdapter friends = new BasePreviewAdapter(this,
                new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, "Minh Anh").with(R.id.tvLastMessage, getString(R.string.ui_uirows_text_2))
                        .with(R.id.tvTime, "14:30").click(() -> go(UiScreen.CHAT)),
                new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, getString(R.string.ui_uirows_text_3)).with(R.id.tvLastMessage, getString(R.string.ui_uirows_text_4))
                        .with(R.id.tvTime, "13:15").click(() -> go(UiScreen.CHAT)),
                new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, getString(R.string.ui_uirows_text_5))
                        .with(R.id.tvLastMessage, getString(R.string.ui_uirows_text_7))
                        .with(R.id.tvTime, getString(R.string.ui_uirows_text_9)).click(() -> go(UiScreen.PROFILE))) {
            @Override protected void bindActions(View row, PreviewRow item) {
                BaseFragment.on(row, R.id.btnMore, () -> dialog(R.layout.dialog_friend_options));
            }
        };
        list(root, R.id.rvFriendsList, friends);
        filter(root, R.id.etSearchFriends, friends);
        binding.btnHeaderMenu.setOnClickListener(bindingView -> UiDialogs.showPopup((MainActivity) requireActivity(),
                binding.btnHeaderMenu, R.layout.layout_friends_menu_popup));
        binding.tvHeaderTitle.setText(getString(R.string.ui_studyscreenfragment_text_2));
    }
}
