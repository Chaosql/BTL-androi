package com.example.myapplication.ui.profile;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class EditIntroductionDialog extends BasePreviewDialog {
    public EditIntroductionDialog(MainActivity activity) { super(activity, R.layout.dialog_edit_introduction); }

    @Override protected void bind(View content, AlertDialog dialog) {
        BaseFragment.text(content, R.id.etStatusTag, activity.getString(R.string.ui_studyscreenfragment_text_14));
    }
}
