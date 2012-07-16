package org.fer.urgence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhoneNumber extends LinearLayout {

	private static final String SMS = "SMS";
	private static final String PHONE_NUMBER = "PHONE_NUMBER";
	private static final String PHONE_NAME = "PHONE_NAME";
	
	private TextView name;
	private TextView phoneNumber;
	private CheckBox cbSms;
	private Button contactBtn;
	
	private CharSequence defaultName;
	private CharSequence defaultPhoneNumber;
	
	private DialPhoneNumber simpleDial;
	
	public PhoneNumber(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PhoneNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PhoneNumber(Context context) {
		super(context);
		init(context);
	}

	public void init(Context context) {
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v=layoutInflater.inflate(R.layout.phone_number,this);
		
		name = (TextView) v.findViewById(R.id.phoneName);
		phoneNumber = (TextView) v.findViewById(R.id.phoneNumber);
		cbSms = (CheckBox) v.findViewById(R.id.cbSms);
		contactBtn = (Button) v.findViewById(R.id.btSelectContact);
		
		defaultName = name.getText();
		defaultPhoneNumber = phoneNumber.getText();
		
		final LongClickResetContact longClickResetContact = new LongClickResetContact();
		name.setOnLongClickListener(longClickResetContact);
		phoneNumber.setOnLongClickListener(longClickResetContact);
		cbSms.setOnLongClickListener(longClickResetContact);
		
		final DialListener dialListener = new DialListener();
		name.setOnClickListener(dialListener);
		phoneNumber.setOnClickListener(dialListener);
	}
	
	public void setShortContact(ShortContact shortContact) {
		name.setText(shortContact.getContactName());
		phoneNumber.setText(shortContact.getPhoneNumber());
		if (shortContact.isSms()!=null) {
			cbSms.setChecked(shortContact.isSms());
		}
	}
	
	public void resetShortContactWithDlgConfirmation() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    	Resources ressources = getResources();
    	builder.setMessage(ressources.getText(R.string.phone_number_reset))
    	.setCancelable(false)
    	.setNegativeButton(ressources.getText(R.string.phone_number_no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// We do nothing on cancel.
			}
		})
    	.setPositiveButton(ressources.getText(R.string.phone_number_yes), new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			name.setText(defaultName);
    			phoneNumber.setText(defaultPhoneNumber);
    		}
    	});
    	final AlertDialog alert = builder.create();
    		
		alert.show();
	}
	
	public ShortContact getShortContact() {
		return new ShortContact(name.getText(), phoneNumber.getText(), cbSms.isChecked());
	}
	
	public void setSmsVisible(int visibility) {
		cbSms.setVisibility(visibility);
	}
	
	public void restore(Bundle savedInstanceState, int pos) {
		if (savedInstanceState!=null) {
			CharSequence contactName = savedInstanceState.getCharSequence(PHONE_NAME+pos);
			if (contactName!=null) {
				name.setText(contactName);
			}
			CharSequence contactPhoneNumber = savedInstanceState.getCharSequence(PHONE_NUMBER+pos);
			if (contactPhoneNumber!=null) {
				phoneNumber.setText(contactPhoneNumber);
			}
			Boolean contactSms = savedInstanceState.getBoolean(SMS+pos);
			if (contactSms!=null) {
				cbSms.setChecked(contactSms);
			}
		}
	}

	public void save(Bundle outState, int pos) {
		outState.putCharSequence(PHONE_NUMBER+pos, phoneNumber.getText());
		outState.putCharSequence(PHONE_NAME+pos, name.getText());
		outState.putBoolean(SMS+pos, cbSms.isChecked());
	}

	public void initSetContact(final OnLaunchContactPicker mainActivity, final int pos) {
		contactBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mainActivity.onLaunchContactPicker(pos);
			}
		});
	}

	private final class LongClickResetContact implements OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			resetShortContactWithDlgConfirmation();
			return true;
		}
	}

	private final class DialListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			simpleDial.call();
		}
	}

	public void initDial(DialPhoneNumber simpleDial) {
		this.simpleDial = simpleDial;
	}
}

