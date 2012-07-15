package org.fer.urgence;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
	
	public static final String URGENCE_PREFS = "Urgence_Prefs";

	private static final String PREF_IS_SMS = "IsSMS_";
	private static final String PREF_PHONE_NUMBER = "PhoneNumber_";
	private static final String PREF_NAME = "Name_";

	private static final int MAX_PHONE_NUMBER = 4;

	private static final int CONTACT_PICKER_RESULT = 1001;  
	
	PhoneNumber[] phoneNumberList = new PhoneNumber[MAX_PHONE_NUMBER];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int i = 0;
        phoneNumberList[i++] = (PhoneNumber) findViewById(R.id.phoneNumber0);
        phoneNumberList[i++] = (PhoneNumber) findViewById(R.id.phoneNumber1);
        phoneNumberList[i++] = (PhoneNumber) findViewById(R.id.phoneNumber2);
        phoneNumberList[i++] = (PhoneNumber) findViewById(R.id.phoneNumber3);
        
        for(i = 0; i<MAX_PHONE_NUMBER; i++) {
        	 phoneNumberList[i].initSetContact(this, i);
        }
        phoneNumberList[0].setSmsVisible(View.INVISIBLE);
        initFromPrefs();
    }

    public void doLaunchContactPicker(int pos) {  
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
                Contacts.CONTENT_URI);  
        contactPickerIntent = new Intent(Intent.ACTION_PICK,  
        		ContactsContract.CommonDataKinds.Phone.CONTENT_URI);  
        startActivityForResult(contactPickerIntent, MainActivity.CONTACT_PICKER_RESULT+pos);  
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {  
	        switch (requestCode) {  
	        case CONTACT_PICKER_RESULT:  
	        case CONTACT_PICKER_RESULT+1:  
	        case CONTACT_PICKER_RESULT+2:  
	        case CONTACT_PICKER_RESULT+3:
	        	int numberTel = requestCode - CONTACT_PICKER_RESULT;
	        	ShortContact shortContact = handleResultFromContactActivity(data);
	        	phoneNumberList[numberTel].setShortContact(shortContact);
	            break;  
	        }  
	  
	    } else {  
	        // gracefully handle failure  
	    	Log.d("Urgence", "Warning: activity result not ok");  
	    }  
	}

	private ShortContact handleResultFromContactActivity(Intent data) {
		// handle contact results  
		Uri result = data.getData(); 
		String id = result.getLastPathSegment();  
		Log.d("Urgence", "Got a result: "  
		    + result.toString() + " -> id : " + id);  
		String[] projection = new String[] {
				Phone._ID,
		        Phone.DISPLAY_NAME,
		        Phone.NUMBER
		};
		Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
				, projection
				, ContactsContract.Contacts._ID + "=?", new String[] {id}, null);
		int indexName = cursor.getColumnIndex(Phone.DISPLAY_NAME);
		int indexPhoneNumber = cursor.getColumnIndex(Phone.NUMBER);
		cursor.moveToFirst();
		String name = cursor.getString(indexName);
		String phoneNumber = cursor.getString(indexPhoneNumber);
		Log.d("Urgence", "Name : " + name + ", phoneNumber : " + phoneNumber);
		return new ShortContact(name, phoneNumber, null);
	}

	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		for(int i = 0; i<MAX_PHONE_NUMBER; i++) {
			phoneNumberList[i].restore(savedInstanceState, i);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		for(int i = 0; i<MAX_PHONE_NUMBER; i++) {
			phoneNumberList[i].save(outState, i);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		SharedPreferences prefs = getSharedPreferences(URGENCE_PREFS, Activity.MODE_MULTI_PROCESS);
		for(int i = 0; i<MAX_PHONE_NUMBER ; i++) {
			ShortContact shortContact = phoneNumberList[i].getShortContact();
			Editor editor = prefs.edit();
			editor.putString(PREF_NAME+i, shortContact.getContactName().toString());
			editor.putString(PREF_PHONE_NUMBER+i, shortContact.getPhoneNumber().toString());
			editor.putBoolean(PREF_IS_SMS+i, shortContact.isSms());
			editor.commit();
		}
		super.onStop();
	}  
    
   private void initFromPrefs() {
		SharedPreferences prefs = getSharedPreferences(URGENCE_PREFS, Activity.MODE_MULTI_PROCESS);
		for(int i = 0; i<MAX_PHONE_NUMBER ; i++) {
			String name = prefs.getString(PREF_NAME+i, null);
			if (name!=null) {
				String phoneNumber = prefs.getString(PREF_PHONE_NUMBER+i, null);
				Boolean isSms = prefs.getBoolean(PREF_IS_SMS+i, false);
				ShortContact shortContact = new ShortContact(name, phoneNumber, isSms);
				phoneNumberList[i].setShortContact(shortContact);
			}
		}
   }
}
