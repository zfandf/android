package cn.m15.gotransfersimplest.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import cn.m15.gotransfersimplest.MyApplication;
import cn.m15.gotransfersimplest.R;

public class ImageUtil {
	
	public static byte[] getFileThumbail(String filename, int reqWidth, int reqHeight) {
		int lastDot = filename.lastIndexOf(".");
		if (lastDot >= 0) {
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					filename.substring(lastDot + 1).toLowerCase(Locale.getDefault()));
			if (mimeType != null) {
				if (mimeType.matches("image/.+")) {
					return getImageThumbail(filename, reqWidth, reqHeight);
				} else if (mimeType.matches("video/.+")) {
					return getVideoThumbail(filename, reqWidth, reqHeight);
				} else if (mimeType.equals("application/vnd.android.package-archive")) {
					return getApkImage(filename, reqWidth, reqHeight);
				}
			}
		}
		return bitmapToBytes(null);
	}
	
	public static byte[] getApkImage(String apkFilePath, int reqWidth, int height) {
		Drawable drawable = null;
		try {
			PackageManager pm = MyApplication.sInstance.getPackageManager();
			if (!TextUtils.isEmpty(apkFilePath) && new File(apkFilePath).exists()) {
				PackageInfo packagekInfo = pm.getPackageArchiveInfo(apkFilePath,
						PackageManager.GET_ACTIVITIES);
				if (packagekInfo != null) {
					ApplicationInfo appInfo = packagekInfo.applicationInfo;
					appInfo.sourceDir = apkFilePath;
					appInfo.publicSourceDir = apkFilePath;
					drawable = appInfo.loadIcon(pm);
				}
			}
		} catch (Exception e) {
		} catch (OutOfMemoryError e) {
		}
		
		if (drawable instanceof BitmapDrawable) {
			return bitmapToBytes(((BitmapDrawable) drawable).getBitmap());			
		}
		return null;
	}
	
	private static byte[] bitmapToBytes(Bitmap bitmap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (bitmap != null) {
	 	    bitmap.compress(CompressFormat.PNG, 100, bos);
	 	    bitmap.recycle();
		}
		Drawable drawable = MyApplication.sInstance.getResources().getDrawable(R.drawable.ic_launcher);
		if (drawable instanceof BitmapDrawable) {
			Bitmap aBitmap = ((BitmapDrawable) drawable).getBitmap();
			aBitmap.compress(CompressFormat.PNG, 100, bos);
			aBitmap.recycle();
		} 
		return bos.toByteArray();
	}
	
	public static byte[] getVideoThumbail(String filename, int reqWidth, int reqHeight) {
		Bitmap bitmap = null;
		// get image from video
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	    try {
	    	retriever.setDataSource(filename);
	        bitmap = retriever.getFrameAtTime(-1);
	    } catch (IllegalArgumentException ex) {
	    	// Assume this is a corrupt video file
	    } catch (RuntimeException ex) {
	    	// Assume this is a corrupt video file.
	    } finally {
	    	try {
	        	retriever.release();
	    	} catch (RuntimeException ex) {
	        	// Ignore failures while cleaning up.
	    	}
	    }        	
	
	    if (bitmap != null) {
	    	bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);        	
	    }
	    
	    return bitmapToBytes(bitmap);
	}
	
	public static byte[] getImageThumbail(String filename, int reqWidth, int reqHeight) {
		Bitmap bitmap = decodeSampledBitmapFromFile(filename, reqWidth, reqHeight);
		bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
		return bitmapToBytes(bitmap); 
	}
	
	/**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
    
    /**
     * Calculate an inSampleSize for use in a {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link android.graphics.BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
     * having a width and height equal to or larger than the requested width and height.
     *
     * @param options An options object with out* params already populated (run through a decode*
     *            method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
        // END_INCLUDE (calculate_sample_size)
    }

}
