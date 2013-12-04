package com.romanpulov.symphonytimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertOkCancelDialog extends DialogFragment {
	private final static String STRING_MESSAGE = "MESSAGE";
	private int message;	
	
	public interface OnOkButtonClick {
		public void OnOkButtonClickEvent(DialogFragment dialog);
	}
	
	private OnOkButtonClick okButtonClick;
	
	public static AlertOkCancelDialog newAlertOkCancelDialog(DMTimerRec dmTimerRec, int message) {
		AlertOkCancelDialog newDialog = new AlertOkCancelDialog() ;
		Bundle data = new Bundle();
		data.putParcelable(dmTimerRec.getClass().toString(), dmTimerRec);
		newDialog.setArguments(data);
		newDialog.setMessage(message);
		newDialog.setRetainInstance(false);
		return newDialog;
	}
	
	public void setMessage(int message) {
		this.message = message;
	}
	
	public void setOkButtonClick(OnOkButtonClick okButtonClick) {
		this.okButtonClick = okButtonClick;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		setOkButtonClick(((MainActivity)activity).onDeleteOkButtonClick);
	}
	
	@Override
	public void onSaveInstanceState(Bundle data) {
		// TODO Auto-generated method stub
		data.putInt(AlertOkCancelDialog.STRING_MESSAGE, message);
		super.onSaveInstanceState(data);
	}	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (null != savedInstanceState) {
			message = savedInstanceState.getInt(AlertOkCancelDialog.STRING_MESSAGE);
		}
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		dialogBuilder
		  .setMessage(message)
		  .setPositiveButton(R.string.caption_ok, 
				  new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (null != okButtonClick) {
							okButtonClick.OnOkButtonClickEvent(AlertOkCancelDialog.this);
						}
					}
				}
				  )
		  .setNegativeButton(R.string.caption_cancel, null)
		  ;
		return dialogBuilder.create();
	}
}
