package com.jonlatane.composer.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.jonlatane.composer.music.Score;

/**
 * Created by jonlatane on 6/29/15.
 */
public class StaffDeltaView2 extends View {


    // Paint defaults
    private static Paint DEFAULT_PAINT;
    private static Paint SELECTED_PAINT;

    // Height-related defaults
    private static final int BASE_STAFF_LINE_DISTANCE = 20;
    private static final int BASE_NOTEHEAD_PADDING = 3 * BASE_STAFF_LINE_DISTANCE;
    private static final float BASE_STROKE_WIDTH = 1;
    private static final float BASE_FONT_SIZE = 25;
    private static final float MINI_CLEF_FONT_SIZE = 8;
    private static final float MAGIC_LINE_DELTA_DIFFERENCE = -9;
    static {
        DEFAULT_PAINT = new Paint();
        DEFAULT_PAINT.setColor(0xFF000000);
        DEFAULT_PAINT.setFlags(Paint.ANTI_ALIAS_FLAG);
        SELECTED_PAINT = new Paint();
        SELECTED_PAINT.setColor(0xFF004444);
        SELECTED_PAINT.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    // Character defaults
    private static final String NOTEHEDZ_FILLED_HEAD = "%";

    private Score.Staff.StaffDelta staffDelta;

    // The scaling factor drives all other measurements. Requested heights and widths are based on ceil(mScalingFactor * baseValue)
    private float scalingFactor;

    public StaffDeltaView2(Context context) {
        super(context);
        init(context);
    }

    public StaffDeltaView2(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    public StaffDeltaView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    private void init(Context context) {
        scalingFactor = 1;
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaffDeltaView2.this.postInvalidate();
            }
        });
        if(DEFAULT_PAINT.getTypeface() == null)
            DEFAULT_PAINT.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NoteHedz170.ttf"));
        if(SELECTED_PAINT.getTypeface() == null)
            SELECTED_PAINT.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NoteHedz170.ttf"));
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int reqWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int reqHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        View.MeasureSpec.getMode(widthMeasureSpec);
    }
}
