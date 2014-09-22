package myphone.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import myphone.activity.ContactsActivity;
import myphone.activity.R;
import myphone.utils.MyPhoneAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressLint("NewApi")
public class ContactsFragment extends ListFragment {
	
	OnContactSelectedListener mCallbacks;
	
	public static final String TAG = "Contact";
	
	static String[] Headlines = {
        "Article One",
        "Article Two"
    };
	
	
	private final static String[] FROM_COLUMNS = {
        Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.HONEYCOMB ?
                Contacts.DISPLAY_NAME_PRIMARY :
                Contacts.DISPLAY_NAME
	};
	
	private final static int[] TO_IDS = {
        R.id.contact_list_item
	};
	// Define global mutable variables
    // Define a ListView object
    ListView mContactsList;
    // Define variables for the contact the user selects

    // the column DISPLAY_NAME
    private static final String DISPLAY_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME;
    
    private static final String[] PROJECTION = {
        Contacts._ID,
        Contacts.LOOKUP_KEY,
        DISPLAY_NAME
    };
 
    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
//    // The column index for the LOOKUP_KEY column
//    private static final int LOOKUP_KEY_INDEX = 1;
    // The column index for the DISPLAY_NAME column
    private static final int DISPLAY_NAME_INDEX = 2;
    // The column index for the CommonDataKinds.Phone.NUMBER column
    private static final int NUMBER_INDEX = 2;

//    private static final String SELECTION = DISPLAY_NAME + " = \"Alan\"";
    private static final String SELECTION = null;
    
    /*
     * 监视list内item点击事件
     */
    public interface OnContactSelectedListener {
    	public void onItemSelected(int position);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);

    	mContactsList = (ListView) getActivity().findViewById(R.layout.fragment_contacts);
    	Cursor c = getActivity().getContentResolver().query(Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);
    	
    	Log.i(TAG, c.getCount() + "");

    	ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
    	ArrayList<String> mContactIds = new ArrayList<String>();
    	while (c.moveToNext()) {
    		HashMap<String, String> one = new HashMap<String, String>();
    		one.put("contact_id", c.getString(CONTACT_ID_INDEX));
    		one.put("dispaly_name", c.getString(DISPLAY_NAME_INDEX));
    		mContactIds.add(c.getString(CONTACT_ID_INDEX));
    		items.add(one);
    	}
    	ContactsActivity.setmContactIds(mContactIds);

//    	ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_contact_list, R.id.contact_list_item, items)
    	
    	MyPhoneAdapter adapter = new MyPhoneAdapter(getActivity(), items, R.layout.item_contact_list, new String[] {"contact_id", "dispaly_name"}, new int[] {R.id.contact_id, R.id.contact_list_item});
        // Create an array adapter for the list view, using the Ipsum headlines array
    	
        setListAdapter(adapter);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.contact_list) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	try {
    		Log.i(TAG, "onAttach");
    		mCallbacks = (OnContactSelectedListener)activity;
		} catch (ClassCastException e) {
			// TODO: handle exception
			throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
		}
    	
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	// TODO Auto-generated method stub
    	mCallbacks.onItemSelected(position);
    }

}
