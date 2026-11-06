package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.DialogEditIntroductionBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class EditIntroductionDialog extends BasePreviewDialog<DialogEditIntroductionBinding> {
    public EditIntroductionDialog(MainActivity activity) { super(activity, DialogEditIntroductionBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.etStatusTag.setText(activity.getString(R.string.ui_studyscreenfragment_text_14));
    }
}
