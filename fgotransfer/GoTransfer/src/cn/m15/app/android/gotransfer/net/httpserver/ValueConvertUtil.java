package cn.m15.app.android.gotransfer.net.httpserver;

import java.util.concurrent.TimeUnit;

import android.text.format.DateFormat;

public class ValueConvertUtil {

	public static String formateDate(long inTimeInMillis) {
		return DateFormat.format("yyyy-MM-dd", inTimeInMillis).toString();
	}

	public static String formateMil(long inTimeInMillis) {
		return DateFormat.format("yyyy/MM/dd kk:mm", inTimeInMillis).toString();
	}

	public static String formatMilSToHMS(long millisecond) {

		if (millisecond > 0) {
			if (millisecond < 3600000) {
				return String.format(
						"%02d:%02d",
						TimeUnit.MILLISECONDS.toMinutes(millisecond),
						TimeUnit.MILLISECONDS.toSeconds(millisecond)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
										.toMinutes(millisecond)));
			} else {
				return String.format(
						"%02d:%02d:%02d",
						TimeUnit.MILLISECONDS.toHours(millisecond),
						TimeUnit.MILLISECONDS.toMinutes(millisecond)
								- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
										.toHours(millisecond)),
						TimeUnit.MILLISECONDS.toSeconds(millisecond)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
										.toMinutes(millisecond)));
			}
		}
		return "";
	}

}
