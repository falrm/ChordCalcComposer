package com.jonlatane.composer.scoredisplay3;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A basic ScoreDeltaView for debugging purposes
 * Created by jonlatane on 12/31/14.
 */
public class DummyScoreDeltaView extends ScrollView implements ScoreDeltaViewInterface {
    private int adapterPosition = RecyclerView.NO_POSITION;
    public DummyScoreDeltaView(Context context) {
        super(context);
        init();
    }

    public DummyScoreDeltaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DummyScoreDeltaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setVerticalScrollBarEnabled(false);
    }


    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    /**
     * Set the visibility of this view within its parent (0-1)
     *
     * @param f
     * @return
     */
    @Override
    public void setVisibility(float f) {
        TextView foreground = (TextView)getChildAt(0);
        int foregroundColor = Color.argb( Math.round(f * 255),
                Color.red(ScoreDataAdapter.FOREGROUND_COLOR_BASE),
                Color.green(ScoreDataAdapter.FOREGROUND_COLOR_BASE),
                Color.blue(ScoreDataAdapter.FOREGROUND_COLOR_BASE));
        foreground.setTextColor(foregroundColor);
//        setBackgroundColor(Color.argb(Math.round(f * 255), 255, 255, 255));
//        child.invalidate();
//        invalidate();
    }
}
