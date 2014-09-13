package cn.m15.app.android.gotransfer.utils;

import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;
import cn.m15.app.android.gotransfer.enity.AppEntry;

/**
 * Perform alphabetical comparison of mediafile name
 * */
public class LanguageComparatorCN implements Comparator<Object> {

	public int compare(Object obj1, Object obj2) {

		int compareResult = -1;

		// if (obj1 instanceof MediaInfo && obj2 instanceof MediaInfo) {
		// MediaInfo ostr1 = (MediaInfo) obj1;
		// MediaInfo ostr2 = (MediaInfo) obj2;
		// for (int i = 0; i < ostr1.name.length() && i < ostr2.name.length();
		// i++) {
		//
		// int codePoint1 = ostr1.name.charAt(i);
		// int codePoint2 = ostr2.name.charAt(i);
		// if (Character.isSupplementaryCodePoint(codePoint1)
		// || Character.isSupplementaryCodePoint(codePoint2)) {
		// i++;
		// }
		// if (codePoint1 != codePoint2) {
		// if (Character.isSupplementaryCodePoint(codePoint1)
		// || Character.isSupplementaryCodePoint(codePoint2)) {
		// return codePoint1 - codePoint2;
		// }
		// String pinyin1 = pinyin((char) codePoint1);
		// String pinyin2 = pinyin((char) codePoint2);
		//
		// if (pinyin1 != null && pinyin2 != null) { // 两个字符都是汉字
		// if (!pinyin1.equals(pinyin2)) {
		// return pinyin1.compareTo(pinyin2);
		// }
		// } else {
		// return codePoint1 - codePoint2;
		// }
		// }
		// compareResult = ostr1.name.length() - ostr2.name.length();
		// }
		// } else
		if (obj1 instanceof AppEntry && obj2 instanceof AppEntry) {
			AppEntry ostr1 = (AppEntry) obj1;
			AppEntry ostr2 = (AppEntry) obj2;
			for (int i = 0; i < ostr1.getLabel().length() && i < ostr2.getLabel().length(); i++) {

				int codePoint1 = ((AppEntry) ostr1).getLabel().charAt(i);
				int codePoint2 = ((AppEntry) ostr2).getLabel().charAt(i);
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

					if (pinyin1 != null && pinyin2 != null) { // 两个字符都是汉字
						if (!pinyin1.equals(pinyin2)) {
							return pinyin1.compareTo(pinyin2);
						}
					} else {
						return codePoint1 - codePoint2;
					}
				}
				compareResult = ostr1.getLabel().length() - ostr2.getLabel().length();
			}
		}

		return compareResult;

	}

	private String pinyin(char c) {
		String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
		if (pinyins == null) {
			return null;
		}
		return pinyins[0];
	}

}
