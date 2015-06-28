package com.jonlatane.composer.scoredisplay3;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This View lays out a horizontal list (via a ScoreDataAdapter) of ScoreDeltaViewInterfaces, and adjusts its height to auto-accommodate
 * for the highest view ne
 *
 * Created by jonlatane on 12/30/14.
 */
public class SystemRecyclerView extends LinearLayout {
    public static final String TAG="SystemRecyclerView";
    private static final int DEFAULT_HEADER_DIMEN = 250;

    private SurfaceView header;
    private RecyclerView noteArea;

    private SystemRecyclerView mAfter;
    private SystemRecyclerView mBefore;

    private float heightScalingFactor = 1f;

    private boolean scrolledFromBefore = false;
    private boolean scrolledFromAfter = false;

    // Responsible for telling SystemRecyclerView how visible it should be overall based on how far the view is from its requested position.
    // This is to
    private LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return true;
        }

        @Override
        public void scrollToPositionWithOffset(int position, int offset) {
            super.scrollToPositionWithOffset(position, offset);
            Log.d(TAG, "NEIGHBOR REQUEST--------------------------------");
            Log.d(TAG, "Called to scroll by neighbor; view " + position + " to offset " + offset);
            View requestedView = getViewAtAdapterPosition(position);
            for(int retries = 1; requestedView == null && retries < 5; retries++) {
                requestedView = getViewAtAdapterPosition(position);
            }

            if(requestedView != null) {
                int resultOffset = getItemOffset(requestedView);
                Log.d(TAG, "Found result offset for child of " + resultOffset);
                int difference = Math.abs(resultOffset - offset);
                if (difference > 5) {
                    Log.d(TAG, "Overscroll by " + difference + " pixels");
                    heightScalingFactor = (float) (noteArea.getWidth() - difference) / (float) noteArea.getWidth();
                } else {
                    heightScalingFactor = 1;
                    noteArea.invalidate();
                }
            } else {
                Log.d(TAG, "Couldn't compute overscroll");
            }
            scrollListener.onScrolled(noteArea, 0, 0);
        }

        private int getItemOffset(View v) {
            int[] point = new int[2];
            v.getLocationInWindow(point);
            int xChild = point[0];
            noteArea.getLocationInWindow(point);
            int xNoteArea = point[0];
            Log.d(TAG, "xChild=" + xChild + ", xNoteArea=" + xNoteArea);
            return xChild - xNoteArea;
        }
    };

    /**
     * The ScrollListener is where all the magic happens.  It syncs state with the views above and below (before and after)
     */
    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Log.d(TAG, "onScrolled--------------------------------------");
            List<DummyScoreDeltaView> visibleViews = getVisibleScoreDeltaViews();
            while(visibleViews.isEmpty()) {
                try {
                    visibleViews = getVisibleScoreDeltaViews();
                } catch(Throwable t) {
                    continue;
                }
            }
            float visibilityOfFirstView = getVisibilityOfChildView(visibleViews.get(0));
            float visibilityOfLastView = getVisibilityOfChildView(visibleViews.get(visibleViews.size() - 1));

            Log.d(TAG, "First view visibility:" + visibilityOfFirstView);
            Log.d(TAG, "Last view visibility:" + visibilityOfLastView);

            // Set visibilities so views will update appearance
            for (int i = 1; i < visibleViews.size() - 1; i++) {
                DummyScoreDeltaView scoreDeltaView = visibleViews.get(i);
                scoreDeltaView.setVisibility(1f);
            }
            visibleViews.get(0).setVisibility(visibilityOfFirstView);
            visibleViews.get(visibleViews.size() - 1).setVisibility(visibilityOfLastView);

            int minimumRequiredHeight = getMinimumRequiredHeight(visibleViews, visibilityOfFirstView, visibilityOfLastView);
            Log.d(TAG, "Minimum Required Height: " + minimumRequiredHeight);
            //TODO fix this.  It should scale the height based on the heightScalingFactor
//            if(heightScalingFactor != 1) {
//                minimumRequiredHeight = Math.round(heightScalingFactor * minimumRequiredHeight);
//                Log.d(TAG, "Scaled Minimum Required Height: " + minimumRequiredHeight);
//            }

            //Scale the bookend if this is the last row in the score
            if(mAfter == null) {
                //((ScoreDataAdapter)getAdapter()).setEndingBookendDimensions(noteArea.getWidth() * 2, 150, SystemRecyclerView.this);
            }
            if(mBefore == null) {
                //((ScoreDataAdapter)getAdapter()).setBeginningBookendDimensions(noteArea.getWidth() * 2, 150, SystemRecyclerView.this);

            }

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = minimumRequiredHeight;
            setLayoutParams(layoutParams);

            if (mAfter != null && !scrolledFromAfter) {
                scrollViewBelow(visibleViews, visibilityOfLastView);
            }
            if (mBefore != null && !scrolledFromBefore) {
                scrollViewAbove(visibleViews, visibilityOfFirstView);
            }
            invalidate();
            Log.d(TAG, "END---------------------------------------------");
        }
    };

    public SystemRecyclerView(Context context) {
        super(context);
        header = new SurfaceView(context);
        noteArea = new RecyclerView(context);
        init();
    }

    public SystemRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        header = new SurfaceView(context, attrs);
        noteArea = new RecyclerView(context, attrs);
        init();
    }

    public SystemRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        header = new SurfaceView(context, attrs, defStyle);
        noteArea = new RecyclerView(context, attrs, defStyle);
        init();
    }

    public void init() {
        setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFF00FFFF);
        noteArea.setOnScrollListener(scrollListener);
        ViewGroup.LayoutParams headerParams = new ViewGroup.LayoutParams(DEFAULT_HEADER_DIMEN, DEFAULT_HEADER_DIMEN);
        header.setLayoutParams(headerParams);
        //layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        noteArea.setLayoutManager(layoutManager);

        addView(header);
        addView(noteArea);
    }

    /**
     * Gets the portion of the provided child view's width that is visible
     *
     * @param v
     * @return
     */
    public float getVisibilityOfChildView(View v) {
        if(v == null) {
            Log.d(TAG, "debugger");
        }
        int[] point = new int[2];
        v.getLocationInWindow(point);
        int vWidth = v.getWidth();
        int left = point[0] - header.getWidth();
        int right = left + vWidth;
        int truncLeft = Math.max(left, 0);
        int truncRight = Math.min(right, noteArea.getWidth());
        return (float)(truncRight - truncLeft) / (float)vWidth;
    }

    /**
     * Returns a list of visible views in the scrolling area.  They must all extend View and implement {@link com.jonlatane.composer.scoredisplay3.ScoreDeltaViewInterface}.
     *
     * @param <ChildViewType> the type of views you want to get
     * @return
     */
    public <ChildViewType extends View & ScoreDeltaViewInterface> List<ChildViewType> getVisibleScoreDeltaViews() {
        int first = getLayoutManager().findFirstVisibleItemPosition();
        int last = getLayoutManager().findLastVisibleItemPosition();
        if(first != RecyclerView.NO_POSITION && last != RecyclerView.NO_POSITION) {
            List<ChildViewType> result = new ArrayList<ChildViewType>(last - first + 1);
            for(int i = first; i <= last; i++) {
                ChildViewType scoreDeltaView = (ChildViewType)getViewAtAdapterPosition(i);
                result.add(scoreDeltaView);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private static <ChildViewType extends View & ScoreDeltaViewInterface> int getMinimumRequiredHeight(List<ChildViewType> visibleViews, float visibilityOfFirstView, float visibilityOfLastView) {
        int result = 50;
        int idx = 0;
        for(ChildViewType view : visibleViews) {
            int requiredHeight = view.getHeight();
            if(idx == 0) {
                requiredHeight = Math.round(visibilityOfFirstView * requiredHeight);
            } else if( idx == visibleViews.size() - 1) {
                requiredHeight = Math.round(visibilityOfLastView * requiredHeight);
            }
            result = Math.max(result, requiredHeight);
            idx++;
        }
        return result;
    }

    public View getViewAtAdapterPosition(int position) {
        for(int i = 0; i < noteArea.getChildCount(); i++) {
            ScoreDeltaViewInterface scoreDeltaView = (ScoreDeltaViewInterface)noteArea.getChildAt(i);
            //Log.d(TAG, "Found View with adapter position: " + scoreDeltaView.getAdapterPosition());
            if(scoreDeltaView.getAdapterPosition() == position) {
                //Log.d(TAG, "This is the View we're looking for!");
                return (View)scoreDeltaView;
            }
        }

        return null;
    }

    public SystemRecyclerView getViewAbove() {
        return mBefore;
    }

    public void setViewAbove(SystemRecyclerView mBefore) {
        this.mBefore = mBefore;
    }

    private <ChildViewType extends View & ScoreDeltaViewInterface>
    void scrollViewAbove(List<ChildViewType> visibleViews, float visibilityOfFirstView) {
        // Do the scrolling
        ChildViewType firstVisibleView = visibleViews.get(0);
        int firstVisibleViewWidth = firstVisibleView.getWidth();
        int firstVisibleViewPosition = firstVisibleView.getAdapterPosition();
        int firstVisibleViewOffset = Math.round(firstVisibleView.getWidth() * (1f - visibilityOfFirstView));
        Log.d(TAG, "Scrolling view before to {@" + firstVisibleViewPosition + "+" + firstVisibleViewOffset + "}");
        mBefore.scrolledFromAfter = true;
        ((LinearLayoutManager)(mBefore.noteArea.getLayoutManager())).scrollToPositionWithOffset(firstVisibleViewPosition, mBefore.noteArea.getWidth()-firstVisibleViewOffset);
        mBefore.scrolledFromAfter = false;

        //TODO resize the view above if
    }

    public SystemRecyclerView getViewBelow() {
        return mAfter;
    }

    public void setViewBelow(SystemRecyclerView mAfter) {
        this.mAfter = mAfter;
    }

    private <ChildViewType extends View & ScoreDeltaViewInterface>
    void scrollViewBelow(List<ChildViewType> visibleViews, float visibilityOfLastView) {
        ChildViewType lastVisibleView = visibleViews.get(visibleViews.size() - 1);
        int lastVisibleViewPosition = lastVisibleView.getAdapterPosition();
        int pixelsAfterLastVisibleView = Math.round(lastVisibleView.getWidth() * visibilityOfLastView);
        Log.d(TAG, "Scrolling view after to {@" + lastVisibleViewPosition + "+" + pixelsAfterLastVisibleView + "}");
        mAfter.scrolledFromBefore = true;
        ((LinearLayoutManager)(mAfter.noteArea.getLayoutManager())).scrollToPositionWithOffset(lastVisibleViewPosition, -pixelsAfterLastVisibleView);
        mAfter.scrolledFromBefore = false;

    }

    public void setAdapter(ScoreDataAdapter adapter) {
        noteArea.setAdapter(adapter);
    }

    public ScoreDataAdapter getAdapter() {
        return (ScoreDataAdapter)noteArea.getAdapter();
    }

    private LinearLayoutManager getLayoutManager() {
        return (LinearLayoutManager)noteArea.getLayoutManager();
    }
}
