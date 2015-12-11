package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/9/15.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mRecentItemsPage = 0;
    private FetchItemsTask mFetchItemsTask;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        fetchItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_photo_gallery_recycler_view);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int items = recyclerView.getAdapter().getItemCount();
                int lastVisiblePosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                if ((items != 0) && (lastVisiblePosition == items - 1)) {
                    mRecentItemsPage++;
                    fetchItems();
                }
            }
        });

        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean mProcessed = false;

            @Override
            public void onGlobalLayout() {
                if (mProcessed) {
                    return;
                }

                int pixelWidth = mRecyclerView.getWidth();
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                int dpWidth = Math.round(pixelWidth / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
                final int MINIMUM_COLUMN_DP_WIDTH = 120;
                int columns = dpWidth / MINIMUM_COLUMN_DP_WIDTH;
                gridLayoutManager.setSpanCount(columns);

                mProcessed = true;
            }
        });

        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private void fetchItems() {
        if (mFetchItemsTask != null) {
            mFetchItemsTask.cancel(false);
        }
        mFetchItemsTask = new FetchItemsTask();
        mFetchItemsTask.execute();
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView)itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.getCaption());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> items) {
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            photoHolder.bindGalleryItem(mGalleryItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems(mRecentItemsPage); // sketchy - unprotected multi-threaded access
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if (isCancelled()) {
                return;
            }
            mItems = items;
            setupAdapter();
        }
    }
}
