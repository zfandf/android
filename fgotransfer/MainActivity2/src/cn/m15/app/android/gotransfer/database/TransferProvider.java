package cn.m15.app.android.gotransfer.database;

import java.util.ArrayList;
import java.util.HashMap;

import cn.m15.app.android.gotransfer.database.Transfer.Conversation;
import cn.m15.app.android.gotransfer.database.Transfer.User;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


/**
 * Provides access to a database of Transfer
 */
public class TransferProvider extends ContentProvider {

	private static final String TAG = "TransferProvider";

	private static final String DATABASE_NAME 				= "transfer.db";
	private static final int DATABASE_VERSION 				= 1;
	private static final String USER_TABLE_NAME 			= "user";
	private static final String CONVERSATION_TABLE_NAME 	= "conversation";

	private static HashMap<String, String> sUserProjectionMap;
	private static HashMap<String, String> sConversationProjectionMap;
	
	private static final int USER 			= 1;
	private static final int USER_ID 		= 2;
	private static final int CONVERSATION 	= 3;
	private static final int CONVERSATION_ID= 4;

	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Transfer.AUTHORITY, "user", USER);
		sUriMatcher.addURI(Transfer.AUTHORITY, "user/#", USER_ID);
		sUriMatcher.addURI(Transfer.AUTHORITY, "conversation", CONVERSATION);
		sUriMatcher.addURI(Transfer.AUTHORITY, "conversation/#", CONVERSATION_ID);

		sUserProjectionMap = new HashMap<String, String>();
		sUserProjectionMap.put(User._ID, User._ID);
		sUserProjectionMap.put(User.NAME, User.NAME);
		sUserProjectionMap.put(User.AVATAR, User.AVATAR);
		sUserProjectionMap.put(User.MAC_ADDRESS, User.MAC_ADDRESS);
		
		sConversationProjectionMap = new HashMap<String, String>();
		sConversationProjectionMap.put(Conversation._ID,			Conversation._ID);
		sConversationProjectionMap.put(Conversation.PACKET_ID,		Conversation.PACKET_ID);
		sConversationProjectionMap.put(Conversation.FILENAME,		Conversation.FILENAME);
		sConversationProjectionMap.put(Conversation.SRCPATH,		Conversation.SRCPATH);
		sConversationProjectionMap.put(Conversation.LOCALPATH,		Conversation.LOCALPATH);
		sConversationProjectionMap.put(Conversation.IS_SEND,		Conversation.IS_SEND);
		sConversationProjectionMap.put(Conversation.FRIEND,			Conversation.FRIEND);
		sConversationProjectionMap.put(Conversation.MAC_ADDRESS,	Conversation.MAC_ADDRESS);
		sConversationProjectionMap.put(Conversation.FILETYPE,		Conversation.FILETYPE);
		sConversationProjectionMap.put(Conversation.STATUS,			Conversation.STATUS);
		sConversationProjectionMap.put(Conversation.WHOLE_STATUS,	Conversation.WHOLE_STATUS);
		sConversationProjectionMap.put(Conversation.FILESIZE,		Conversation.FILESIZE);
		sConversationProjectionMap.put(Conversation.CREATED,		Conversation.CREATED);
		sConversationProjectionMap.put(Conversation.POSITION,		Conversation.POSITION);
		sConversationProjectionMap.put(Conversation.REMAINING_PATHS,Conversation.REMAINING_PATHS);
		sConversationProjectionMap.put(Conversation.LAST_MODIFIED,	Conversation.LAST_MODIFIED);
	}
	
	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + USER_TABLE_NAME + " (" 
					+ User._ID 			+ " INTEGER PRIMARY KEY," 
					+ User.NAME 		+ " TEXT," 
					+ User.AVATAR 		+ " TEXT," 
					+ User.MAC_ADDRESS	+ " TEXT" 
					+ ");");
			
			db.execSQL("CREATE TABLE " 	+ CONVERSATION_TABLE_NAME + " (" 
					+ Conversation._ID 				+ " INTEGER PRIMARY KEY," 
					+ Conversation.PACKET_ID 		+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.FILENAME 		+ " TEXT," 
					+ Conversation.SRCPATH	 		+ " TEXT,"
					+ Conversation.LOCALPATH		+ " TEXT,"
					+ Conversation.IS_SEND 			+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.FRIEND 			+ " TEXT," 
					+ Conversation.MAC_ADDRESS 		+ " TEXT," 
					+ Conversation.FILETYPE			+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.STATUS 			+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.WHOLE_STATUS		+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.FILESIZE 		+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.CREATED 			+ " INTEGER UNSIGNED DEFAULT 0," 
					+ Conversation.POSITION 		+ " INTEGER UNSIGNED DEFAULT 0,"
					+ Conversation.REMAINING_PATHS	+ " TEXT,"
					+ Conversation.LAST_MODIFIED	+ " INTEGER UNSIGNED DEFAULT 0"
					+ ");");

			// create index 
			db.execSQL("CREATE INDEX idx_user_mac ON " + USER_TABLE_NAME + "(" + User.MAC_ADDRESS + ");" );
			db.execSQL("CREATE INDEX idx_conversation_mac ON " + CONVERSATION_TABLE_NAME + "(" + Conversation.MAC_ADDRESS + ");" );
			db.execSQL("CREATE INDEX idx_packet_id ON " + CONVERSATION_TABLE_NAME + "(" + Conversation.PACKET_ID + ");" );
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
		}
	}

	private DatabaseHelper mOpenHelper;
	private boolean isNotifyChange = true;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query >>> " + selection);
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = null;

		switch (sUriMatcher.match(uri)) {
		case USER:
			qb.setTables(USER_TABLE_NAME);
			qb.setProjectionMap(sUserProjectionMap);
			orderBy = Transfer.User.DEFAULT_SORT_ORDER;
			break;
		case USER_ID:
			qb.setTables(USER_TABLE_NAME);
			qb.setProjectionMap(sUserProjectionMap);
			qb.appendWhere(User._ID + "=" + uri.getPathSegments().get(1));
			orderBy = Transfer.User.DEFAULT_SORT_ORDER;
			break;

		case CONVERSATION:
			qb.setTables(CONVERSATION_TABLE_NAME);
			qb.setProjectionMap(sConversationProjectionMap);
			orderBy = Transfer.Conversation.DEFAULT_SORT_ORDER;
			break;
		case CONVERSATION_ID:
			qb.setTables(CONVERSATION_TABLE_NAME);
			qb.setProjectionMap(sConversationProjectionMap);
			qb.appendWhere(Transfer.Conversation._ID + "=" + uri.getPathSegments().get(1));
			orderBy = Transfer.Conversation.DEFAULT_SORT_ORDER;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (!TextUtils.isEmpty(sortOrder)) {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		if (isNotifyChange) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case USER:
			return User.CONTENT_TYPE;

		case USER_ID:
			return User.CONTENT_ITEM_TYPE;

		case CONVERSATION:
			return Conversation.CONTENT_TYPE;

		case CONVERSATION_ID:
			return Conversation.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Log.d(TAG, "insert >>> " + initialValues);
		
		// Validate ContentValues
		if (initialValues == null) {
			throw new IllegalArgumentException("Content values can not be empty");
		}

		// Validate the requested uri and NOT NULL value
		String exceptionMsg = null;
		switch (sUriMatcher.match(uri)) {
		case USER:
			if (!initialValues.containsKey(User.NAME)) {
				exceptionMsg = "name in user table can not be empty";
			}
			break;
		case CONVERSATION:
			if (!initialValues.containsKey(Conversation.FILENAME)) {
				exceptionMsg = "filename in conversation table can not be empty";
			}
			break;

		default:
			exceptionMsg = "Unknown URI " + uri;
		}

		if (exceptionMsg != null) {
			throw new IllegalArgumentException(exceptionMsg);
		}

		// Set some default values
		ContentValues values = new ContentValues(initialValues);

		String table = "";
		Uri contentUri = null;

		switch (sUriMatcher.match(uri)) {
		case USER:
			table = USER_TABLE_NAME;
			contentUri = User.CONTENT_URI;
			break;

		case CONVERSATION:
			table = CONVERSATION_TABLE_NAME;
			contentUri = Conversation.CONTENT_URI;
			break;
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(table, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(contentUri, rowId);
			if (isNotifyChange) {
				getContext().getContentResolver().notifyChange(noteUri, null);
			}
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		Log.d(TAG, "delete >>> " + where);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String id;

		switch (sUriMatcher.match(uri)) {
		case USER:
			count = db.delete(USER_TABLE_NAME, where, whereArgs);
			break;
		case USER_ID:
			id = uri.getPathSegments().get(1);
			count = db.delete(USER_TABLE_NAME, User._ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		case CONVERSATION:
			count = db.delete(CONVERSATION_TABLE_NAME, where, whereArgs);
			break;
		case CONVERSATION_ID:
			id = uri.getPathSegments().get(1);
			count = db.delete(CONVERSATION_TABLE_NAME, Conversation._ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (isNotifyChange) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		Log.d(TAG, "update >>> " + where);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String id;
		switch (sUriMatcher.match(uri)) {
		case USER:
			count = db.update(USER_TABLE_NAME, values, where, whereArgs);
			break;
		case USER_ID:
			id = uri.getPathSegments().get(1);
			count = db.update(USER_TABLE_NAME, values,
					User._ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case CONVERSATION:
			count = db.update(CONVERSATION_TABLE_NAME, values, where, whereArgs);
			break;
		case CONVERSATION_ID:
			id = uri.getPathSegments().get(1);
			count = db.update(CONVERSATION_TABLE_NAME, values,
					Conversation._ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
            
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (isNotifyChange) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}
	
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		Log.e("Conversation", "applyBatch >>> " + operations.size());
		
		isNotifyChange = false;
		ContentProviderResult[] result = new ContentProviderResult[operations.size()];
	    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	    db.beginTransaction();
	    int i = 0;
	    try {
	        for (ContentProviderOperation operation : operations) {
	            result[i++] = operation.apply(this, result, i);
	        }
	        db.setTransactionSuccessful();
	        getContext().getContentResolver().notifyChange(operations.get(0).getUri(), null);
	    } catch (OperationApplicationException e) {
	        Log.e(TAG, "batch failed: " + e.getLocalizedMessage());
	    } finally {
	        db.endTransaction();
	        isNotifyChange = true;
	    }
	    return result;
	}

}
