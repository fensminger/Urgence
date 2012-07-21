package org.fer.urgence;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import static org.fer.urgence.PreferenceMgr.*;

public class MainActivity extends Activity implements OnLaunchContactPicker {
	
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
        phoneNumberList[i++] = (PhoneNumber) findViewById(R.id.phoneNumber4);
        
        for(i = 0; i<MAX_PHONE_NUMBER; i++) {
        	 phoneNumberList[i].initSetContact(this, i);
        }
        phoneNumberList[0].setSmsVisible(View.INVISIBLE);
        initFromPrefs();
    }

    public void onLaunchContactPicker(int pos) {  
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
                Contacts.CONTENT_URI);  
        contactPickerIntent = new Intent(Intent.ACTION_PICK,  
        		ContactsContract.CommonDataKinds.Phone.CONTENT_URI);  
        startActivityForResult(contactPickerIntent, MainActivity.CONTACT_PICKER_RESULT+pos);  
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {  
			if (requestCode>=CONTACT_PICKER_RESULT && requestCode<(CONTACT_PICKER_RESULT+MAX_PHONE_NUMBER)) {
	        	int numberTel = requestCode - CONTACT_PICKER_RESULT;
	        	ShortContact shortContact = handleResultFromContactActivity(data);
	        	phoneNumberList[numberTel].setShortContact(shortContact);
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
		PreferenceMgr prefMgr = new PreferenceMgr(prefs);
		List<ShortContact> contacts = new ArrayList<ShortContact>();
		for(int i = 0; i<MAX_PHONE_NUMBER ; i++) {
			ShortContact shortContact = phoneNumberList[i].getShortContact();
			contacts.add(shortContact);
		}
		prefMgr.saveAllContacts(contacts);
		super.onStop();
	}  
    
   private void initFromPrefs() {
		SharedPreferences prefs = getSharedPreferences(URGENCE_PREFS, Activity.MODE_MULTI_PROCESS);
		PreferenceMgr prefMgr = new PreferenceMgr(prefs);
		DialPhoneNumber simpleDial = new SimpleDial();
		DialPhoneNumber simpleWithSmsDial = new SimpleWithSmsDial();
		List<ShortContact> contacts = prefMgr.getAllContacts();
		
		for(int i = 0; i<MAX_PHONE_NUMBER ; i++) {
			ShortContact contact = (i<contacts.size())?contacts.get(i):null;
			if (contact !=null && contact.getContactName()!=null) {
				phoneNumberList[i].setShortContact(contacts.get(i));
			}
			if (i==0) {
				phoneNumberList[i].initDial(simpleWithSmsDial);
			} else {
				phoneNumberList[i].initDial(simpleDial);
			}
		}
   }
   
   private ShortContact[] getAllShortContacts() {
	   ShortContact[] res = new ShortContact[MAX_PHONE_NUMBER];
	   for(int i = 0; i<MAX_PHONE_NUMBER; i++) {
		   res[i] = phoneNumberList[i].getShortContact();
	   }
	   return res;
   }
   
   private class SimpleDial implements DialPhoneNumber {
		@Override
		public void call() {
			ShortContact[] contacts = new ShortContact[] {phoneNumberList[0].getShortContact()};
			DialService dialService = new DialService();
			dialService.dial(MainActivity.this, contacts);
		}

	}
	
	private class SimpleWithSmsDial implements DialPhoneNumber {
		@Override
		public void call() {
			ShortContact[] contacts = getAllShortContacts();
			DialService dialService = new DialService();
			dialService.dial(MainActivity.this, contacts);
		}

	}
}
