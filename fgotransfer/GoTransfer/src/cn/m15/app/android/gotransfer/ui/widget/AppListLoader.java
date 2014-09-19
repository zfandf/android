package cn.m15.app.android.gotransfer.ui.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.content.AsyncTaskLoader;
import cn.m15.app.android.gotransfer.ui.fragment.AppsChooserFragment.PackageIntentReceiver;
import cn.m15.app.android.gotransfer.utils.AppEntry;
import cn.m15.app.android.gotransfer.utils.LanguageComparatorCN;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
	public final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
	public final PackageManager mPm;

	List<AppEntry> mApps;
	PackageIntentReceiver mPackageObserver;

	public AppListLoader(Context context) {
		super(context);

		// Retrieve the package manager for later use; note we don't
		// use 'context' directly but instead the save global application
		// context returned by getContext().
		mPm = getContext().getPackageManager();
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public List<AppEntry> loadInBackground() {
		// Retrieve all known applications.
		List<ApplicationInfo> apps = mPm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		if (apps == null) {
			apps = new ArrayList<ApplicationInfo>();
		}

		final Context context = getContext();

		// Create corresponding array of entries and load their labels.
		int size = apps.size();
		List<AppEntry> entries = new ArrayList<AppEntry>(size);
		for (int i = 0; i < size; i++) {
			ApplicationInfo applicationInfo = apps.get(i);
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
				continue;
			if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
				continue;
			if (mPm.getLaunchIntentForPackage(applicationInfo.packageName) == null)
				continue;
			AppEntry entry = new AppEntry(this, applicationInfo);
			entry.loadLabel(context);
			entries.add(entry);
		}

		// Sort the list.
		Collections.sort(entries, cn);

		// Done!
		return entries;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(List<AppEntry> apps) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (apps != null) {
				onReleaseResources(apps);
			}
		}
		List<AppEntry> oldApps = mApps;
		mApps = apps;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(apps);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldApps != null) {
			onReleaseResources(oldApps);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mApps != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mApps);
		}

		// Start watching for changes in the app data.
		if (mPackageObserver == null) {
			mPackageObserver = new PackageIntentReceiver(this);
		}

		// Has something interesting in the configuration changed since we
		// last built the app list?
		boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

		if (takeContentChanged() || mApps == null || configChange) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<AppEntry> apps) {
		super.onCanceled(apps);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(apps);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (mApps != null) {
			onReleaseResources(mApps);
			mApps = null;
		}

		// Stop monitoring for changes.
		if (mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	protected void onReleaseResources(List<AppEntry> apps) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	// public static final Comparator<AppEntry> ALPHA_COMPARATOR = new
	// Comparator<AppEntry>() {
	// // private final Collator sCollator = Collator.getInstance();
	//
	// @Override
	// public int compare(AppEntry object1, AppEntry object2) {
	// // return object1.getLabel().compareTo(object2.getLabel());
	// return Collator.getInstance
	// (Locale.CHINESE).compare(object1.getLabel(), object2.getLabel());
	// }
	// };
	LanguageComparatorCN cn = new LanguageComparatorCN();

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}
}