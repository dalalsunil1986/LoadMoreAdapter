/*
 * Copyright 2016 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rubensousa.loadmoreadapter;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class LoadMoreAdapter extends RecyclerView.Adapter<LoadMoreAdapter.ViewHolder>
        implements ScrollListener.OnEventListener {

    private static final String STATE_LOADING_ENABLED = "loading_enabled";
    private static final String STATE_LOADING = "loading";

    public static final int VIEW_PROGRESS = 1;
    public static final int VIEW_NORMAL = 0;

    private OnLoadMoreListener mLoadMoreListener;
    private ScrollListener mScrollListener;

    @LayoutRes
    private int mProgressLayout;

    private boolean mLoading = false;
    private boolean mLoadingEnabled = true;
    private int mVisibleThreshold;
    private final boolean mInversed;

    public LoadMoreAdapter(RecyclerView recyclerView) {
        this(recyclerView, R.layout.loadmoreadapter_adapter_progress, 5, false);
    }

    public LoadMoreAdapter(RecyclerView recyclerView, int progressLayout) {
        this(recyclerView, progressLayout, 5, false);
    }

    public LoadMoreAdapter(RecyclerView recyclerView, int progressLayout, int threshold) {
        this(recyclerView, progressLayout, threshold, false);
    }

    public LoadMoreAdapter(RecyclerView recyclerView, int progressLayout,
                           int threshold, boolean inversed) {
        mProgressLayout = progressLayout;
        mInversed = inversed;
        mVisibleThreshold = threshold;
        mScrollListener = new ScrollListener(mInversed, threshold,
                this, recyclerView.getLayoutManager());
        recyclerView.addOnScrollListener(mScrollListener);
    }

    public abstract List getItems();

    public abstract ViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType);

    public void restoreState(Bundle restoreState) {
        if (restoreState != null) {
            mLoadingEnabled = restoreState.getBoolean(STATE_LOADING_ENABLED);
            mLoading = restoreState.getBoolean(STATE_LOADING);
        }
    }

    public void saveState(Bundle outState) {
        outState.putBoolean(STATE_LOADING, mLoading);
        outState.putBoolean(STATE_LOADING_ENABLED, mLoadingEnabled);
    }

    public int getVisibleThreshold() {
        return mVisibleThreshold;
    }

    public boolean isInversed() {
        return mInversed;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    public boolean isLoading() {
        return mLoading;
    }

    @SuppressWarnings("unchecked")
    public void setLoading(boolean loading) {
        List items = getItems();

        // If we were loading and want to stop doing it,
        // we must remove the null items
        if (mLoading && !loading) {
            if (mInversed) {
                if (items.get(0) == null) {
                    items.remove(0);
                    notifyItemRemoved(0);
                }
            } else {
                if (items.get(items.size() - 1) == null) {
                    items.remove(items.size() - 1);
                    notifyItemRemoved(items.size());
                }
            }
            mLoading = false;
        } else if (!mLoading && loading) { // If we're not already loading but want to

            if (mInversed) {
                items.add(0, null);
                notifyItemInserted(0);
            } else {
                items.add(null);
                notifyItemInserted(items.size() - 1);
            }

            mLoading = true;

            if (mLoadMoreListener != null) {
                mLoadMoreListener.onLoadMore(items.size() - 1);
            }
        } else {
            mLoading = loading;
        }
    }

    public boolean isLoadingEnabled() {
        return mLoadingEnabled;
    }

    public void enableLoading(boolean enable) {
        mLoadingEnabled = enable;
    }

    @Override
    public void onScrollForMore() {
        if (mLoading || !mLoadingEnabled) {
            return;
        }

        setLoading(true);
    }


    @Override
    public int getItemViewType(int position) {
        List items = getItems();

        if (items == null) {
            return VIEW_NORMAL;
        }

        return items.get(position) == null ? VIEW_PROGRESS : VIEW_NORMAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROGRESS) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(mProgressLayout, parent, false));
        }

        return onCreateNormalViewHolder(parent, viewType);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int offset);
    }

}
