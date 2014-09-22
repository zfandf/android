package myphone.activity;

import java.util.ArrayList;

import myphone.fragment.ContactsFragment;
import myphone.fragment.FooterFragment;
import myphone.utils.ContactInfo;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class ContactsActivity extends FragmentActivity implements ContactsFragment.OnContactSelectedListener, OnClickListener {

	public static final String TAG = "main";
	
	public ContactInfo mContactInfo = ContactInfo.getIntance();
	
	public static ArrayList<String> mContactIds = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
        if (savedInstanceState != null) {
            return;
        }

        // Create a new Fragment to be placed in the activity layout
        ContactsFragment contactlistFragment = new ContactsFragment();
        
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, contactlistFragment).commit();
        
        
        // init search
        Button searchBtn = (Button)findViewById(R.id.contact_search_btn);
        searchBtn.setOnClickListener(this);
	}

	public static ArrayList<String> getmContactIds() {
		return mContactIds;
	}

	public static void setmContactIds(ArrayList<String> mContactIds) {
		ContactsActivity.mContactIds = mContactIds;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contacts,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onItemSelected(int position) {
		// TODO Auto-generated method stub
//		viewItemDetail(position);
//		viewItemDetailSystem(position);

//		callPhone(position);
		viewCallPage(position);
	}
	
	// 显示自定义联系人详情页面
	public void viewItemDetail(int position) {
		// Create fragment and give it an argument for the selected article
        FooterFragment newFragment = new FooterFragment();
        Bundle args = new Bundle();
        args.putInt(FooterFragment.ARG_POSITION, position);
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
	}
	
	// 显示系统联系人详情页面
	public void viewItemDetailSystem(int position) {
		String mCurrentContactId = ContactsActivity.mContactIds.get(position);
		Log.i(TAG, mCurrentContactId);
  
		Uri personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, new Integer(mCurrentContactId)); 
		Intent intent = new Intent(); 
		intent.setAction(Intent.ACTION_VIEW); 
		intent.setData(personUri); 
		startActivity(intent);
	}
	
	// 拨打电话
	public void callPhone(int position) {
		String mCurrentContactId = ContactsActivity.mContactIds.get(position);
		String phoneNumber = mContactInfo.getPhoneNumber(this, mCurrentContactId);
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
		startActivity(intent);
	}
	
	// 选择联系人
	public void viewCallPage(int position) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setData(Contacts.CONTENT_URI); 
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
			case R.id.contact_search_btn:
				
		}
	}
}
