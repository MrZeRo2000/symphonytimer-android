package com.romanpulov.symphonytimer.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.DialogFragment;

import com.romanpulov.symphonytimer.R;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onSaveInstanceState(Bundle data) {
        data.putInt(AlertOkCancelDialogFragment.STRING_MESSAGE, mMessage);
        super.onSaveInstanceState(data);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            mMessage = savedInstanceState.getInt(AlertOkCancelDialogFragment.STRING_MESSAGE);
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder
          .setMessage(mMessage)
          .setPositiveButton(R.string.caption_ok,
                  new OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          if (null != okButtonClick) {
                              okButtonClick.OnOkButtonClickEvent(AlertOkCancelDialogFragment.this);
                          }
                      }
                  }
          )
          .setNegativeButton(R.string.caption_cancel, null)
          ;
        return dialogBuilder.create();
    }
}