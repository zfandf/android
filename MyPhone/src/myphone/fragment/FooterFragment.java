package myphone.fragment;

import myphone.activity.R;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FooterFragment extends Fragment {
	
	public static final String CURRENT_CONTACT_ID = "contact_id";
	
	public long mCurrentContactId = -1;
	
	public static final String TAG = "footer";
	
	static String[] Articles = {
        "Article One\n\nExcepteur pour-over occaecat squid biodiesel umami gastropub, nulla laborum salvia dreamcatcher fanny pack. Ullamco culpa retro ea, trust fund excepteur eiusmod direct trade banksy nisi lo-fi cray messenger bag. Nesciunt esse carles selvage put a bird on it gluten-free, wes anderson ut trust fund twee occupy viral. Laboris small batch scenester pork belly, leggings ut farm-to-table aliquip yr nostrud iphone viral next level. Craft beer dreamcatcher pinterest truffaut ethnic, authentic brunch. Esse single-origin coffee banksy do next level tempor. Velit synth dreamcatcher, magna shoreditch in american apparel messenger bag narwhal PBR ennui farm-to-table.",
        "Article Two\n\nVinyl williamsburg non velit, master cleanse four loko banh mi. Enim kogi keytar trust fund pop-up portland gentrify. Non ea typewriter dolore deserunt Austin. Ad magna ethical kogi mixtape next level. Aliqua pork belly thundercats, ut pop-up tattooed dreamcatcher kogi accusamus photo booth irony portland. Semiotics brunch ut locavore irure, enim etsy laborum stumptown carles gentrify post-ironic cray. Butcher 3 wolf moon blog synth, vegan carles odd future."
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreateView");
		if (savedInstanceState != null) {
			mCurrentContactId = savedInstanceState.getLong(CURRENT_CONTACT_ID);
        }

        // Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_footer, container, false);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateArticleView(args.getLong(CURRENT_CONTACT_ID));
        } else if (mCurrentContactId != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateArticleView(mCurrentContactId);
        }
	}
	
	public void updateArticleView(long contact_id) {
		Log.i(TAG, "onItemSelected");
		
		String[] projection = new String[]{
				Phone.NUMBER
		};
		String selection = Phone.CONTACT_ID + " = " + contact_id;
		
		Cursor c = getActivity().getContentResolver().query(Phone.CONTENT_URI, projection, selection, null, null);
		Log.i(TAG, c.getCount()+"");
		String phoneNumber = "";

		while (c.moveToNext()) {
			phoneNumber = c.getString(0);
		}

        TextView article = (TextView) getActivity().findViewById(R.id.contact_footer);
        article.setText(phoneNumber);
    }
}
