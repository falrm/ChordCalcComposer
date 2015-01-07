package com.jonlatane.composer.scoredisplay3;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by jonlatane on 1/1/15.
 */
public class SystemHeaderView extends LinearLayout {
    public SystemHeaderView(Context context) {
        super(context);
        init();
    }

    public SystemHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SystemHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {

    }

    public static List<ScoreDeltaViewInterface> getVisibleScoreDeltaViews(RecyclerView recyclerView) {
        return null;
    }
}
