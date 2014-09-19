package cn.m15.app.android.gotransfer.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.utils.images.ImageCache;
import cn.m15.app.android.gotransfer.utils.images.LocalImageFetcher;
import cn.m15.app.android.gotransfer.utils.images.VideoImageFetcher;

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

	public static VideoImageFetcher createVedioImageFetcher(FragmentActivity activity, int imageSize) {
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(activity,
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
												   // app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		VideoImageFetcher imageFetcher = new VideoImageFetcher(activity, imageSize);
		imageFetcher.setLoadingImage(R.drawable.bg_video);
		imageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);
		return imageFetcher;
	}

	public static LocalImageFetcher createLocalImageFetcher(FragmentActivity activity, int imageSize) {
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(activity,
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
												   // app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		LocalImageFetcher imageFetcher = new LocalImageFetcher(activity, imageSize);
		imageFetcher.setLoadingImage(R.drawable.bg_picture);
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
				Log.d("share", "share compress:" + options + ", size:" + imageSize);
				baos.reset();
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);
				imageSize = baos.toByteArray().length / 1024;
			}
			return baos.toByteArray();
		} finally {
			// image.recycle();
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
	}

	public static Bitmap grayScaleImage(Bitmap src) {
		// constant factors
		final double GS_RED = 0.299;
		final double GS_GREEN = 0.587;
		final double GS_BLUE = 0.114;
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		// pixel information
		int A, R, G, B;
		int pixel;
		// get image size
		int width = src.getWidth();
		int height = src.getHeight();
		// scan through every single pixel
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get one pixel color
				pixel = src.getPixel(x, y);
				// retrieve color of all channels
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				// take conversion up to one single value
				R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
				// set new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		// return final image
		return bmOut;
	}

	public static Drawable getApkImage(Context ctx, PackageManager pm, String apkFilePath, boolean needScale) {
		if (!TextUtils.isEmpty(apkFilePath) && new File(apkFilePath).exists()) {
			PackageInfo packagekInfo = pm.getPackageArchiveInfo(apkFilePath,
					PackageManager.GET_ACTIVITIES);
			if (packagekInfo != null) {
				ApplicationInfo appInfo = packagekInfo.applicationInfo;
				appInfo.sourceDir = apkFilePath;
				appInfo.publicSourceDir = apkFilePath;
				try {
					return needScale ? scaleDrawable(ctx, appInfo.loadIcon(pm)) : appInfo.loadIcon(pm);
				} catch (OutOfMemoryError e) {
					Log.e("ApkIconLoader", e.toString());
				}
			}
		}

		return GoTransferApplication.getInstance().getResources()
				.getDrawable(R.drawable.bg_history_app);
	}

	public static Drawable scaleDrawable(Context ctx, Drawable src) {
		Bitmap b = ((BitmapDrawable) src).getBitmap();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, ctx.getResources()
				.getDisplayMetrics());
		Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width, width, false);
		return new BitmapDrawable(ctx.getResources(), bitmapResized);
	}
}
