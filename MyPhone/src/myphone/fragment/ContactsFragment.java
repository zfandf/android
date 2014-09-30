package myphone.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import myphone.activity.R;
import myphone.utils.ContactInfo;
import myphone.utils.MyPhoneAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

@SuppressLint("NewApi")
public class ContactsFragment extends ListFragment {
	
	OnContactSelectedListener mCallbacks;
	
	public static final String TAG = "Contact";
	
    // Define a ListView object
    ListView mContactsList;

    /*
     * 监视list内item点击事件
     */
    public interface OnContactSelectedListener {
    	public void onItemSelected(int position, View v);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);

    	mContactsList = (ListView) getActivity().findViewById(R.layout.fragment_contacts);

    	ArrayList<HashMap<String, Object>> items = ContactInfo.getIntance().getContactList(getActivity());

    	MyPhoneAdapter adapter = new MyPhoneAdapter(getActivity(), items, R.layout.item_contact_list, new String[] {"contact_id", "dispaly_name"}, new int[] {R.id.contact_id, R.id.contact_list_item});
    	
        setListAdapter(adapter);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
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
    	mCallbacks.onItemSelected(position, v);
    }
    
}
