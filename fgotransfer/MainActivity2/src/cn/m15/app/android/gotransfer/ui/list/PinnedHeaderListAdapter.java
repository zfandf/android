/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.m15.app.android.gotransfer.ui.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;


/**
 * A subclass of {@link CompositeCursorAdapter} that manages pinned partition headers.
 */
public abstract class PinnedHeaderListAdapter extends CompositeCursorAdapter
        implements PinnedHeaderListView.PinnedHeaderAdapter {

    public static final int PARTITION_HEADER_TYPE = 0;

    private boolean mPinnedPartitionHeadersEnabled;
    private boolean mHeaderVisibility[];

    public PinnedHeaderListAdapter(Context context) {
        super(context);
    }

    public PinnedHeaderListAdapter(Context context, int initialCapacity) {
        super(context, initialCapacity);
    }

    public boolean getPinnedPartitionHeadersEnabled() {
        return mPinnedPartitionHeadersEnabled;
    }

    public void setPinnedPartitionHeadersEnabled(boolean flag) {
        this.mPinnedPartitionHeadersEnabled = flag;
    }

    @Override
    public int getPinnedHeaderCount() {
        if (mPinnedPartitionHeadersEnabled) {
            return getPartitionCount();
        } else {
            return 0;
        }
    }

    protected boolean isPinnedPartitionHeaderVisible(int partition) {
        return getPinnedPartitionHeadersEnabled() && hasHeader(partition)
                && !isPartitionEmpty(partition);
    }

    /**
     * The default implementation creates the same type of view as a normal
     * partition header.
     */
    @Override
    public View getPinnedHeaderView(int partition, View convertView, ViewGroup parent) {
        if (hasHeader(partition)) {
            View view = null;
            if (convertView != null) {
                Integer headerType = (Integer)convertView.getTag();
                if (headerType != null && headerType == PARTITION_HEADER_TYPE) {
                    view = convertView;
                }
            }
            if (view == null) {
                view = newHeaderView(getContext(), partition, null, parent);
                view.setTag(PARTITION_HEADER_TYPE);
                // TODO:
                view.setFocusable(true);
                view.setEnabled(true);
            }
            bindHeaderView(view, partition, getCursor(partition));
//            view.setLayoutDirection(parent.getLayoutDirection());
            return view;
        } else {
            return null;
        }
    }

    @Override
    public void configurePinnedHeaders(PinnedHeaderListView listView) {
        if (!getPinnedPartitionHeadersEnabled()) {
            return;
        }

        int size = getPartitionCount();

        // Cache visibility bits, because we will need them several times later on
        if (mHeaderVisibility == null || mHeaderVisibility.length != size) {
            mHeaderVisibility = new boolean[size];
        }
        for (int i = 0; i < size; i++) {
            boolean visible = isPinnedPartitionHeaderVisible(i);
            mHeaderVisibility[i] = visible;
            if (!visible) {
                listView.setHeaderInvisible(i, true);
            }
        }

        int headerViewsCount = listView.getHeaderViewsCount();
        int position = listView.getPositionAt(0) - headerViewsCount;
        int partition = getPartitionForPosition(position);

        // Starting at the top, find and pin headers for partitions preceding the visible one(s)
        int maxTopHeader = -1;
        for (int i = 0; i < size; i++) {
            if (mHeaderVisibility[i]) {

                if (i > partition) {
                    break;
                }

                maxTopHeader = i;
            }
        }

        if (maxTopHeader >= 0 && maxTopHeader < size) {

    		// If last section in partition, fading header, otherwise pinned at top
        	if (isLastSectionInPartition(position)) {
				int listPosition = listView.getPositionAt(listView.getTotalTopPinnedHeaderHeight());        	
				listView.setFadingHeader(maxTopHeader, listPosition, true);
			} else 
				listView.setHeaderPinnedAtTop(maxTopHeader, 0, false);

	        for (int i = 0; i < size; i++) {
	            if (i != maxTopHeader) {
	                listView.setHeaderInvisible(i, false);
	            }
	        }
        }
    }    
    
    @Override
    public int getScrollPositionForHeader(int viewIndex) {
        return getPositionForPartition(viewIndex);
    }
}
