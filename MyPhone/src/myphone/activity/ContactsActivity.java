package myphone.activity;

import java.util.ArrayList;

import myphone.fragment.ContactsFragment;
import myphone.fragment.FooterFragment;
import myphone.utils.ContactInfo;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContactsActivity extends FragmentActivity implements ContactsFragment.OnContactSelectedListener {

	public static final String TAG = "ContactsActivity";
	
	public ContactInfo mContactInfo = ContactInfo.getIntance();
	
	public static ArrayList<String> mContactIds = new ArrayList<String>();
	
	private static int mCount = 0;
	
	public static ArrayList<String> getmContactIds() {
		return mContactIds;
	}

	public static void setmContactIds(ArrayList<String> mContactIds) {
		ContactsActivity.mContactIds = mContactIds;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		if (savedInstanceState != null) {
			mCount = savedInstanceState.getInt("mCount");
		}
		
		Log.i("savedInstanceState", mCount+"");
		
        if (savedInstanceState != null) {
            return;
        }

        // Create a new Fragment to be placed in the activity layout
        ContactsFragment contactlistFragment = new ContactsFragment();
        
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, contactlistFragment).commit();
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
	public void onItemSelected(int position, View v) {
		// TODO Auto-generated method stub
		Log.i(TAG, position+"");
		TextView contactIdView = (TextView)v.findViewById(R.id.contact_id);
		Long contact_id = Long.parseLong((String) contactIdView.getText());
		
		Log.i(TAG, contact_id+"");
		viewContactDetail(contact_id);
	}
	
	public void viewContactDetail(Long contact_id) {
		Intent intent = new Intent();
		intent.setClass(this, ContactDetailActivity.class);
		// 第一种向intent传值方式
		intent.putExtra("contact_id", contact_id);
		
		// 第二种向intent传值方式
		Bundle bundle = new Bundle();
		bundle.putString("haha", "hahahahah");
		bundle.putLong("contact_id", contact_id);
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// 显示自定义联系人详情页面
	public void viewItemDetail(Long contact_id) {
		// Create fragment and give it an argument for the selected article
        FooterFragment newFragment = new FooterFragment();
        Bundle args = new Bundle();
        args.putLong(FooterFragment.CURRENT_CONTACT_ID, contact_id);
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, new Throwable().getStackTrace()[0].getMethodName());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, new Throwable().getStackTrace()[0].getMethodName());
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(TAG, new Throwable().getStackTrace()[0].getMethodName());
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, new Throwable().getStackTrace()[0].getMethodName());
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, new Throwable().getStackTrace()[0].getMethodName());
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.i(TAG, new Throwable().getStackTrace()[0].getMethodName());
	}
}
