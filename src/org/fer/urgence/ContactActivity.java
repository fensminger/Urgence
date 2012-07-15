package org.fer.urgence;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;

public class ContactActivity extends Activity {
	
	private ListView mContactList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContactList = (ListView) findViewById(R.id.contactList);
        populateContactList();
    }

    /**
     * Populate the contact list based on account currently selected in the account spinner.
     */
    private void populateContactList() {
        // Build adapter with contact entries
        Cursor cursor = getContacts();
        String[] fields = new String[] {
                Phone.DISPLAY_NAME,
                Phone.NUMBER
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_entry, cursor,
                fields, new int[] {R.id.contactEntryText, R.id.contactNumber});
        mContactList.setAdapter(adapter);
    }

    /**
     * Obtains the contact list for the currently selected account.
     *
     * @return A cursor for for accessing the contact list.
     */
    private Cursor getContacts()
    {
        // Run query
        Uri uri = Phone.CONTENT_URI;
        String[] projection = new String[] {
        		Phone._ID,
                Phone.DISPLAY_NAME,
                Phone.NUMBER
        };
        boolean mShowInvisible = false;
        String selection = Phone.IN_VISIBLE_GROUP + " = '" +
                (mShowInvisible ? "0" : "1") + "'";
        String[] selectionArgs = null;
        String sortOrder = Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, null, selectionArgs, sortOrder);
    }
    
}
