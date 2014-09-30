package myphone.activity;

import myphone.utils.ContactInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DialogActivity extends DialogFragment {

	public static int mCurrentPosition;
	public static String mCurrentContactId;
	
	public static final String TAG = "dialog";
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder = onCreateCommentDialog();
		builder.setMessage("联系人详情");

		builder.setPositiveButton(R.string.text_call_phone, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// FIRE ZE MISSILES!
				ContactInfo.callPhone(getActivity(), mCurrentContactId);
			}
		});
		builder.setNegativeButton(R.string.text_contact_detail, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
        	    ContactInfo.viewItemDetailSystem(getActivity(), mCurrentContactId);
            }
        });
        // Create the AlertDialog object and return it
		Log.i(TAG, "onCreateDialog");
        return builder.create();
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, "start");
		ImageView v = (ImageView) getActivity().findViewById(R.id.contact_photo);
	    
	    String uri = ContactInfo.getContactPhoto(getActivity(), mCurrentContactId);
	    Log.i("dialog", "v="+v);
	    try {
	    	Uri u = Uri.parse(uri);
	    	Log.i("dialog", "uri parse: "+u);
	    	v.setImageURI(u);
	    } catch (NullPointerException e) {
	    	e.printStackTrace();
	    	Log.i("dialog error", "uri="+uri);
	    }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View v = super.onCreateView(inflater, container, savedInstanceState);
		Log.i("dialog", "onCreateView");
		return v;
	}
	
	/*
	 * 自定义布局的dialog
	 */
	public AlertDialog.Builder onCreateCommentDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	 
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.activity_dialog, null));
	    return builder;
	}
}
