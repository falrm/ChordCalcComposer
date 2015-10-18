package com.jonlatane.composer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.Score.Staff.Voice.VoiceDelta;
import com.jonlatane.composer.music.coverings.Clef;
import com.jonlatane.composer.music.harmony.Key;

/**
 * This is a primitive view capable of viewing a StaffDelta
 * @author jonlatane
 *
 */
public class StaffDeltaView extends View {

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
	
	private StaffDelta mStaffDelta;
	
	// The scaling factor drives all other measurements. Requested heights and widths are based on ceil(mScalingFactor * baseValue)
	private float scalingFactor;
	
	// Height-related variables
	private int requestedHeightAboveStaffCenter; // Get Only
	private int heightAboveStaffCenter; // Set only
	private int requestedHeightBelowStaffCenter; // Get Only
	private int heightBelowStaffCenter; // Set only
	
	// Width-related variables
	private int requestedWidth; //Get Only
	private int width; // Set only
	

	public StaffDeltaView(Context context) {
		super(context);
		init(context);
	}
	
	public StaffDeltaView(Context context, AttributeSet attrs) {
	  super(context,attrs);
	  init(context);
	}

	public StaffDeltaView(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	  init(context);

	}

    private void init(Context context) {
		scalingFactor = 1;
		requestedHeightBelowStaffCenter
			= requestedHeightBelowStaffCenter
			= heightAboveStaffCenter
			= heightBelowStaffCenter
			= getScaledGlobalMinimumHeightAboveBelow();
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				StaffDeltaView.this.postInvalidate();
				//StaffDeltaView.this.requestD();
			}
		});
		if(DEFAULT_PAINT.getTypeface() == null)
				DEFAULT_PAINT.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NoteHedz170.ttf"));
		if(SELECTED_PAINT.getTypeface() == null)
			SELECTED_PAINT.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NoteHedz170.ttf"));
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//These are what Android is telling us.  We will just ignore this.
		int reqWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		int reqHeight = View.MeasureSpec.getSize(heightMeasureSpec);

		// Make sure all dimensions meet their minimum spec
		width = (requestedWidth > width) ? requestedWidth : width;
		heightAboveStaffCenter = (requestedHeightAboveStaffCenter > heightAboveStaffCenter) ? requestedHeightAboveStaffCenter : heightAboveStaffCenter;
		heightBelowStaffCenter = (requestedHeightBelowStaffCenter > heightBelowStaffCenter) ? requestedHeightBelowStaffCenter : heightBelowStaffCenter;
		
		// Make sure all variables are non-zero, otherwise onDraw() may not be called
		if(width < 500)
			width = 500;
		if(heightAboveStaffCenter == 0)
			heightAboveStaffCenter = 1;
		if(heightBelowStaffCenter == 0)
			heightBelowStaffCenter = 1;

        //TODO: If any child views are created, add measurements here (?)

		// Do our measurement
		setMeasuredDimension(width, heightAboveStaffCenter + heightBelowStaffCenter);
	}

	@Override
	public void onDraw(Canvas c) {
		//Do scaling on rendering matrix, we will calculate how high we had to go
		c.scale(scalingFactor, scalingFactor);

		// Select Paint
		Paint paint = setColors(c);

		c.drawRect(0, 0, 100, 100, paint);
		
		// Draw staff lines
		drawStaffLines(c, paint);
		
		// We will draw right to left
		float x = getWidth() - 50;
		
		// Draw measure line if this is the end of a measure
		if(mStaffDelta.IS_LAST_IN_MEASURE) {
			c.drawLine(x, 0, x, getHeight(), paint);
		}
		
		// Draw upcoming clef change
		if(mStaffDelta.CLEF_CHANGE_AFTER != null) {
			paint.setTextSize(MINI_CLEF_FONT_SIZE);
			//TODO
		}
		
		// Draw notes
		Clef clef = mStaffDelta.ESTABLISHED.CLEF;
        Key key = mStaffDelta.ESTABLISHED.KEY;
		float maxY = 0, minY = 0;
		for(VoiceDelta voiceDelta : mStaffDelta.VOICES) {
			Rational renderedDuration = voiceDelta.getNoteheadAtLocation();
            for(String noteName : voiceDelta.CHANGED.NOTES.noteNameCache) {
                Integer distanceFromCenter = clef.getHeptatonicStepsFromCenter(noteName);
                float y = heightAboveStaffCenter/scalingFactor -
                		(distanceFromCenter * BASE_STAFF_LINE_DISTANCE/2)
                        - (MAGIC_LINE_DELTA_DIFFERENCE);
				updateRequestedHeight(y);
				maxY = Math.max(y, maxY);
				minY = Math.min(y, minY);
                c.drawText(NOTEHEDZ_FILLED_HEAD, x, y, paint);
            }
		}

        //TODO: Draw grace notes
		//request
	}

	private void updateRequestedHeight(float y) {
		if(y < BASE_NOTEHEAD_PADDING/scalingFactor) {
			heightAboveStaffCenter = Math.max(requestedHeightAboveStaffCenter,
					(int)Math.ceil(heightAboveStaffCenter + ((BASE_NOTEHEAD_PADDING-y)/scalingFactor)));
			invalidate();
		} else if( y > (heightBelowStaffCenter + heightAboveStaffCenter + BASE_NOTEHEAD_PADDING)/scalingFactor){
			requestedHeightBelowStaffCenter = Math.max(requestedHeightBelowStaffCenter,
					(int)Math.ceil(heightBelowStaffCenter + ((y - (heightAboveStaffCenter + heightBelowStaffCenter - BASE_NOTEHEAD_PADDING)/scalingFactor)*scalingFactor)));
			invalidate();
		}
	}
	
	protected Paint setColors(Canvas c) {
		c.drawColor(Color.WHITE);
		// Select Paint
		Paint paint = DEFAULT_PAINT;
		paint.setStrokeWidth(BASE_STROKE_WIDTH);
		paint.setTextSize(BASE_FONT_SIZE);
		paint.setColor(Color.BLACK);
		return paint;
	}
	
	protected void drawStaffLines(Canvas c, Paint paint) {
		for(int lineIndex = -2; lineIndex <= 2; lineIndex++ ) {
			float offset = lineIndex * BASE_STAFF_LINE_DISTANCE;
			float lineVerticalPosition = heightAboveStaffCenter + offset;
			c.drawLine(0, lineVerticalPosition, getWidth(), lineVerticalPosition, paint);
		}
	}
	
	public int getScaledGlobalMinimumHeightAboveBelow() {
		return (int) Math.ceil(3 * BASE_STAFF_LINE_DISTANCE * scalingFactor);
	}
	
	public final void setStaffDelta(StaffDelta sd) {
		mStaffDelta = sd;
	}
	
	public final StaffDelta getStaffDelta() {
		return mStaffDelta;
	}
	
	/**
	 * @return the requestedHeightAboveStaffCenter
	 */
	public final int getRequestedHeightAboveStaffCenter() {
		return requestedHeightAboveStaffCenter;
	}

	/**
	 * @return the requestedHeightBelowStaffCenter
	 */
	public final int getRequestedHeightBelowStaffCenter() {
		return requestedHeightBelowStaffCenter;
	}

	/**
	 * @return the requestedWidth
	 */
	public final int getRequestedWidth() {
		return requestedWidth;
	}

	/**
	 * @param heightAboveStaffCenter the heightAboveStaffCenter to set
	 */
	public final void setHeightAboveStaffCenter(int heightAboveStaffCenter) {
		this.heightAboveStaffCenter = heightAboveStaffCenter;
	}

	/**
	 * @param heightBelowStaffCenter the heightBelowStaffCenter to set
	 */
	public final void setHeightBelowStaffCenter(int heightBelowStaffCenter) {
		this.heightBelowStaffCenter = heightBelowStaffCenter;
	}

	/**
	 * Set the width of this View.  Note that onMeasure requires this to be a minimum
	 * specified by the value of getRequestedWidth()
	 * 
	 * @param width the width to set
	 */
	public final void setWidth(int width) {
		this.width = width;
	}
}
