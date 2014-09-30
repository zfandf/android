package myphone.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

@SuppressLint("InlinedApi")
public class ContactInfo {
	
	public static final String TAG = "contactInfo";

	private static ContactInfo mInstance;
	
	public static ContactInfo getIntance() {
		if (mInstance == null) {
			mInstance = new ContactInfo();
		}
		return mInstance;
	} 
	
	private static final String DISPLAY_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME;
	
	private ContactInfo() {}
	
	/*
	 * 获得联系人列表
	 */
	public ArrayList<HashMap<String, Object>> getContactList(Context context) {
	    
	    String[] projection = {
	        Contacts._ID,
	        Contacts.LOOKUP_KEY,
	        DISPLAY_NAME
	    };
	    
	    String selection = null;
	    
		Cursor c = context.getContentResolver().query(Contacts.CONTENT_URI, projection, selection, null, null);
    	
    	ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
    	while (c.moveToNext()) {
    		HashMap<String, Object> one = new HashMap<String, Object>();
    		int index_id = c.getColumnIndex(Contacts._ID);
    		int index_name = c.getColumnIndex(DISPLAY_NAME);
    		one.put("contact_id", c.getString(index_id));
    		one.put("dispaly_name", c.getString(index_name));
    		items.add(one);
    	}
    	return items;
	}
	
	/*
     * 获得所有的联系人分组
     */
    public ArrayList<HashMap<String, Object>> getContactGroups(Context context) {
    	return getContactGroups(context, null);
    }
    
    /*
     * 获得指定帐号类型的分组， 如果帐号类型为null， 则获取所有分组
     */
    public ArrayList<HashMap<String, Object>> getContactGroups(Context context, String account_name) {
    	String[] projection = new String[]{
    			ContactsContract.Groups._ID,
    			ContactsContract.Groups.ACCOUNT_NAME,
    			ContactsContract.Groups.TITLE
    	};
    	String selection = null;
    	if (account_name != null) {
    		selection = ContactsContract.Groups.ACCOUNT_NAME + " = " + account_name;
    	}
    	
    	String[] selectionArgs = null;
    	String sortOrder = null;
    	
    	Cursor c = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    	Log.i(TAG, c.getCount()+"");
    	
    	ArrayList<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();
    	if (c != null) {
    		while (c.moveToNext()) {
    			HashMap<String, Object> group = new HashMap<String, Object>();
    			Long id = c.getLong(c.getColumnIndex(ContactsContract.Groups._ID));
    			String name = c.getString(c.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
    			String title = c.getString(c.getColumnIndex(ContactsContract.Groups.TITLE));
    			group.put("id", id);
    			group.put("title", title);
    			group.put("account_name", name);
    			groups.add(group);
    		}
    	}
    	return groups;
    }
	
	@SuppressLint("NewApi")
	/**
	 * 获得联系人的电话号码
	 * @param context
	 * @param contact_id
	 * @return
	 */
	public static String getPhoneNumber(Context context, String contact_id) {
		String[] projection = new String[]{
				Phone.NUMBER
		};
		String selection = Phone.CONTACT_ID + " = " + contact_id;
		
		Cursor c = context.getContentResolver().query(Phone.CONTENT_URI, projection, selection, null, null);
		String phoneNumber = "";
		
		while (c.moveToNext()) {
			String number = c.getString(c.getColumnIndex(Phone.NUMBER));
			if (!number.isEmpty()) {
				phoneNumber = number;
			}
		}
		return phoneNumber;
	}
	
	/*
	 * 获取联系人头像
	 */
	public static String getContactPhoto(Context context, String contact_id) {
		String[] projection = new String[] {
				Contacts.PHOTO_THUMBNAIL_URI
		};
		
		String selection = Contacts._ID + " = " + contact_id;
		Cursor c = context.getContentResolver().query(Contacts.CONTENT_URI, projection, selection, null, null);
		
		String photoSrc = "";
		while (c.moveToNext()) {
			photoSrc = c.getString(c.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI));
			Log.i(TAG, photoSrc+"");
		}
		return photoSrc;
	}
	
	// 显示系统联系人详情页面
	public static void viewItemDetailSystem(Context context, String contact_id) {
  
		Uri personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Integer.parseInt(contact_id)); 
		Intent intent = new Intent(); 
		intent.setAction(Intent.ACTION_VIEW); 
		intent.setData(personUri); 
		context.startActivity(intent);
	}
	
	// 拨打电话
	public static void callPhone(Context context, String contact_id) {
		String phoneNumber = getPhoneNumber((Activity)context, contact_id);
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
		context.startActivity(intent);
	}
	
	// 选择联系人
	public static void viewCallPage(Context context) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setData(Contacts.CONTENT_URI); 
		context.startActivity(intent);
	}
	
}
