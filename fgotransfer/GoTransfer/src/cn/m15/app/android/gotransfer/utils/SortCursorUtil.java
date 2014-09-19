package cn.m15.app.android.gotransfer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;
import android.database.Cursor;
import android.database.CursorWrapper;

public class SortCursorUtil extends CursorWrapper {

	Cursor mCursor;
	ArrayList<SortEntry> sortList = new ArrayList<SortEntry>();
	int mPos = 0;

	public class SortEntry {
		public String key;
		public int order;
	}

	public Comparator<SortEntry> comparator = new Comparator<SortEntry>() {
		@Override
		public int compare(SortEntry entry1, SortEntry entry2) {
			for (int i = 0; i < entry1.key.length() && i < entry2.key.length(); i++) {
				int codePoint1 = entry1.key.charAt(i);
				int codePoint2 = entry2.key.charAt(i);
				if (Character.isSupplementaryCodePoint(codePoint1)
						|| Character.isSupplementaryCodePoint(codePoint2)) {
					i++;
				}
				if (codePoint1 != codePoint2) {
					if (Character.isSupplementaryCodePoint(codePoint1)
							|| Character.isSupplementaryCodePoint(codePoint2)) {
						return codePoint1 - codePoint2;
					}
					String pinyin1 = pinyin((char) codePoint1);
					String pinyin2 = pinyin((char) codePoint2);
					if (pinyin1 != null && pinyin2 != null) {
						if (!pinyin1.equals(pinyin2)) {
							return pinyin1.compareTo(pinyin2);
						}
					} else {
						return codePoint1 - codePoint2;
					}
				}
			}
			return entry1.key.length() - entry2.key.length();
		}
	};

	public SortCursorUtil(Cursor cursor, String columnName) {
		super(cursor);
		mCursor = cursor;
		if (mCursor != null && mCursor.getCount() > 0) {
			int i = 0;
			int column = cursor.getColumnIndexOrThrow(columnName);
			for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext(), i++) {
				SortEntry sortKey = new SortEntry();
				sortKey.key = cursor.getString(column);
				sortKey.order = i;
				sortList.add(sortKey);
			}
		}
		Collections.sort(sortList, comparator);
		// StringBuffer stringBuffer = new StringBuffer();
		// for (SortEntry sortKey : sortList) {
		// stringBuffer.append(sortKey.key + ";");
		// }
		// Log.d("SampleActivity--------排序后----", stringBuffer.toString());
	}

	public boolean moveToPosition(int position) {
		if (position >= 0 && position < sortList.size()) {
			mPos = position;
			int order = sortList.get(position).order;
			return mCursor.moveToPosition(order);
		}
		if (position < 0) {
			mPos = -1;
		}
		if (position >= sortList.size()) {
			mPos = sortList.size();
		}
		return mCursor.moveToPosition(position);
	}

	public boolean moveToFirst() {
		return moveToPosition(0);
	}

	public boolean moveToLast() {
		return moveToPosition(getCount() - 1);
	}

	public boolean moveToNext() {
		return moveToPosition(mPos + 1);
	}

	public boolean moveToPrevious() {
		return moveToPosition(mPos - 1);
	}

	public boolean move(int offset) {
		return moveToPosition(mPos + offset);
	}

	public int getPosition() {
		return mPos;
	}

	private String pinyin(char c) {
		String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
		if (pinyins == null) {
			return null;
		}
		return pinyins[0];
	}

}
