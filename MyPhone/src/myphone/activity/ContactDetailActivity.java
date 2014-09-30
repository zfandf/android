package myphone.activity;

import android.app.Activity;
import android.os.Bundle;

public class ContactDetailActivity extends Activity {

	private static final String TAG = "ContactDetailActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Long contact_id = getIntent().getLongExtra("contact_id", 0);
		
//		TextVi
//		Bundle data = getIntent().getExtras();
		setContentView(R.layout.activity_contact_detail);
		
		
	}
}
