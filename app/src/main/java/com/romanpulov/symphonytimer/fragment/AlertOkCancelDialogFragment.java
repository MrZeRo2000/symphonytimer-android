package com.romanpulov.symphonytimer.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.fragment.app.DialogFragment;

import com.romanpulov.symphonytimer.R;
import org.jetbrains.annotations.NotNull;

public class AlertOkCancelDialogFragment extends DialogFragment {
    private final static String STRING_MESSAGE = "MESSAGE";

    private int mMessage;

    public interface OnOkButtonClick {
        void OnOkButtonClickEvent(DialogFragment dialog);
    }

    private OnOkButtonClick okButtonClick;

    public static AlertOkCancelDialogFragment newAlertOkCancelDialog(Parcelable object, int message) {
        AlertOkCancelDialogFragment newDialog = new AlertOkCancelDialogFragment() ;
        Bundle data = new Bundle();
        if (null != object) {
            data.putParcelable(object.getClass().toString(), object);
        }
        newDialog.setArguments(data);
        newDialog.setMessage(message);
        newDialog.setRetainInstance(true);
        return newDialog;
    }

    public void setMessage(int message) {
        this.mMessage = message;
    }

    public void setOkButtonClick(OnOkButtonClick okButtonClick) {
        this.okButtonClick = okButtonClick;
    }

    @Override
    public void onSaveInstanceState(Bundle data) {
        data.putInt(AlertOkCancelDialogFragment.STRING_MESSAGE, mMessage);
        super.onSaveInstanceState(data);
    }

    @Override
    public @NotNull Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            mMessage = savedInstanceState.getInt(AlertOkCancelDialogFragment.STRING_MESSAGE);
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder
          .setMessage(mMessage)
          .setPositiveButton(R.string.caption_ok,
                  (dialog, which) -> {
                      if (null != okButtonClick) {
                          okButtonClick.OnOkButtonClickEvent(AlertOkCancelDialogFragment.this);
                      }
                  }
          )
          .setNegativeButton(R.string.caption_cancel, null)
          ;
        return dialogBuilder.create();
    }
}