package myphone.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactInfo {
	
	private static ContactInfo mInstance;
	
	public static ContactInfo getIntance() {
		if (mInstance == null) {
			mInstance = new ContactInfo();
		}
		return mInstance;
	} 
	
	private ContactInfo() {}
	
	@SuppressLint("NewApi")
	/**
	 * 获得联系人的电话号码
	 * @param context
	 * @param contact_id
	 * @return
	 */
	public String getPhoneNumber(Activity activity, String contact_id) {
		String[] projection = new String[]{
				Phone.NUMBER
		};
		String selection = Phone.CONTACT_ID + " = " + contact_id;
		
		Cursor c = activity.getContentResolver().query(Phone.CONTENT_URI, projection, selection, null, null);
		String phoneNumber = "";

		while (c.moveToNext()) {
			String number = c.getString(0);
			if (!number.isEmpty()) {
				phoneNumber = number;
			}
		}
		return phoneNumber;
	}
	
}
