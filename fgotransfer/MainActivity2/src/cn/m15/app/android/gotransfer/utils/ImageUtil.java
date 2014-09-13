package cn.m15.app.android.gotransfer.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.utils.images.ImageCache;
import cn.m15.app.android.gotransfer.utils.images.LocalImageFetcher;
import cn.m15.app.android.gotransfer.utils.images.VedioImageFetcher;

public class ImageUtil {
	public static final String IMAGE_CACHE_DIR = "thumbs";
	
	private ImageUtil() {
	}
	
	public static Bitmap getScaleBitmap(Context ctx, int recId) {
		Resources resources = ctx.getResources();
		
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inJustDecodeBounds = true;
		option.inScaled = false;
		BitmapFactory.decodeResource(resources, recId, option);
		
		int width = option.outWidth;
		int height = option.outHeight;
		
		int screenWidth = resources.getDisplayMetrics().widthPixels;
		float scale = (float) screenWidth / width;
		int dstHeight = (int) (height * scale);
		
		option.inJustDecodeBounds = false;
		option.outHeight = dstHeight;
		option.outWidth = screenWidth;

		return BitmapFactory.decodeResource(resources, recId, option);
	}

	public static VedioImageFetcher createVedioImageFetcher(FragmentActivity activity, int imageSize) {
		ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        VedioImageFetcher imageFetcher = new VedioImageFetcher(activity, imageSize);
        imageFetcher.setLoadingImage(R.drawable.img_video_default);
        imageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);
        return imageFetcher;
	}
	
	public static LocalImageFetcher createLocalImageFetcher(FragmentActivity activity, int imageSize) {
		ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        LocalImageFetcher imageFetcher = new LocalImageFetcher(activity, imageSize);
        imageFetcher.setLoadingImage(R.drawable.img_picture_default);
        imageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);
        return imageFetcher;
	}
	
	public static byte[] compressShareImageBytes(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int options = 100;
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
			int imageSize = baos.toByteArray().length / 1024;
			while (imageSize > 32) {
				// options = 32 * 100 / imageSize;
				options -= 10;
				Log.d("share", "share compress:" + options + ", size:"
						+ imageSize);
				baos.reset();
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);
				imageSize = baos.toByteArray().length / 1024;
			}
			return baos.toByteArray();
		} finally {
			//image.recycle();
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static Drawable getApkImage(PackageManager pm, String apkFilePath) {
		if (new File(apkFilePath).exists()) {
			PackageInfo packagekInfo = pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
			if (packagekInfo != null) {
				Drawable iconDrawable = packagekInfo.applicationInfo.loadIcon(pm);
				return iconDrawable;				
			}
		}
		
		return GoTransferApplication.getInstance().getResources()
					.getDrawable(R.drawable.img_unknown_default);
	}
}
