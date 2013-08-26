/**
 * 
 */
package com.jonlatane.composer.scoredisplay;

import java.util.HashMap;
import java.util.Map;

import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.coverings.Clef;
import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.harmony.PitchSet;
import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.Score.Staff.Voice.VoiceDelta;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.scoredisplay.ScoreDeltaView.StaffDeltaView;
import com.jonlatane.composer.scoredisplay.ScoreDrawingSurface.SystemHeaderView.StaffHeaderView;
import com.jonlatane.composer.scoredisplay.StaffSpec.VerticalStaffSpec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * This class is a SurfaceView representing a system in a musical score.  It scrolls horizontally
 * and adjusts its height based on its contents (i.e., hides empty staves and expands for high/low notes).
 * 
 * SystemSurfaceViews with their scrolling chained together may be displayed in a LinearLayout to represent
 * a full score with multiple lines.
 * 
 * @author Jon
 *
 */
public class ScoreDrawingSurface extends ViewGroup implements SurfaceHolder.Callback {
	private static final String TAG = "ScoreDrawer";
	private static final int BRACES_AREA_PX = 10;

	private static final Paint NORMALPAINT = new Paint();
	private static final Paint SELECTEDPAINT = new Paint();
		
	private final ScoreLayout _parent;
	final SurfaceView _surface;
	private final SurfaceHolder _holder;
	
	static {
		NORMALPAINT.setARGB(255, 0, 0, 0);
		NORMALPAINT.setStyle(Paint.Style.STROKE);
		NORMALPAINT.setStrokeWidth(2);
		SELECTEDPAINT.setARGB(255, 0, 0, 255);
		SELECTEDPAINT.setStyle(Paint.Style.STROKE);
		SELECTEDPAINT.setStrokeWidth(2);
	}
	
	public static enum StaffNames { Full, Partial, None }
	
	/**
	 * A SystemHeader defines where the ScoreDrawingSurface will draw the staves on
	 * its canvas and how much space is needed for clefs, key signatures, and time signatures.
	 * 
	 * It may be transitioned towards a new
	 * 
	 * @author Jon Latane
	 *
	 */
	class SystemHeaderView extends ViewGroup {
		private double _partialVisibilityRatio = 0d;
		private ScoreDeltaView _partialScoreDelta = null;
		private ScoreDeltaView _completeScoreDelta = null;
		
		class StaffHeaderView extends LinearLayout {
			private View _clefArea, _keySigArea, _timeSigArea;
			private VerticalStaffSpec staffSpec;
			private int _staffNumber;
			
			public StaffHeaderView(Context context, int staffNumber) {
				super(context);
				setOrientation(HORIZONTAL);
				_staffNumber = staffNumber;
				_clefArea = new View(context) {
					@Override 
					public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
						setMeasuredDimension((int) (StaffSpec.CLEF_WIDTH_PX), 50);
					}
				};
				_keySigArea = new View(context) {
					@Override 
					public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
						setMeasuredDimension((int) (StaffSpec.CLEF_WIDTH_PX), 50);
					}
				};
				_timeSigArea = new View(context) {
					@Override 
					public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
						setMeasuredDimension((int) (StaffSpec.CLEF_WIDTH_PX), 50);
					}
				};
			}
			
			
			
			@Override
			public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				
				_clefArea.measure(widthMeasureSpec,heightMeasureSpec);
				_keySigArea.measure(widthMeasureSpec,heightMeasureSpec);
				_timeSigArea.measure(widthMeasureSpec,heightMeasureSpec);
				//int width = _clefArea.getMeasuredWidth() + _keySigArea.getMeasuredWidth() + _timeSigArea.getMeasuredWidth();
				
				//int height = staffSpec.getTotalHeight();
				int height = staffSpec.ABOVE_CENTER_PX + staffSpec.BELOW_CENTER_PX;
				int width = deriveLayoutWidth() - (int)(BRACES_AREA_PX * _parent.getScalingFactor());
				setMeasuredDimension(width, height);
			}
		}
		
		public SystemHeaderView(Context context) {
			super(context);
		}
		
		
		
		@Override 
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(deriveLayoutWidth(), deriveLayoutHeight());
			ScoreDeltaView scoreDV = (_completeScoreDelta == null) 
					? _partialScoreDelta : _completeScoreDelta;
			for(int i = 0; i < scoreDV.getChildCount(); i++) {
				StaffDeltaView staffDV = (StaffDeltaView) scoreDV.getChildAt(i);
				StaffHeaderView header = (StaffHeaderView) getChildAt(i);
				if(header == null) {
					header = this.new StaffHeaderView(getContext(), i);
					addView(header, i);
				}
				header.staffSpec = staffDV.getActualVerticalStaffSpec();
				header.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
		
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			ScoreDeltaView scoreDV = (_completeScoreDelta == null) 
					? _partialScoreDelta : _completeScoreDelta;
			for(int i = 0; i < scoreDV.getChildCount(); i++) {
				StaffDeltaView staffDV = (StaffDeltaView) scoreDV.getChildAt(i);
				StaffHeaderView header = (StaffHeaderView) getChildAt(i);
				Rect staffDVRect = new Rect();
				staffDV.getHitRect(staffDVRect);
				Rect staffAreaRect = new Rect();
				staffDV._staffArea.getHitRect(staffAreaRect);
				
				header.layout((int)(BRACES_AREA_PX * _parent.getScalingFactor()), staffDVRect.top + staffAreaRect.top,
						r, staffDVRect.top + staffAreaRect.top + header.getMeasuredHeight() );
			}
		}

		public ScoreDeltaView getPartialDelta() {
			return _partialScoreDelta;
		}
		public void setPartialDelta(ScoreDeltaView _partialScoreDelta) {
			this._partialScoreDelta = _partialScoreDelta;
		}



		public ScoreDeltaView getCompleteDelta() {
			return _completeScoreDelta;
		}



		public void setCompleteDelta(ScoreDeltaView _completeScoreDelta) {
			this._completeScoreDelta = _completeScoreDelta;
		}
		public double getPartialVisibilityRatio() {
			return _partialVisibilityRatio;
		}
		
		/**
		 * Visibili
		 * @return
		 */
		public int visibilityToAlpha() {
			int a;
			if(_completeScoreDelta == null)
				a = (int)(255*getPartialVisibilityRatio());
			else
				a = 255;
			return a;
		}
		public void setPartialVisibilityRatio(double _partialVisibilityRatio) {
			this._partialVisibilityRatio = _partialVisibilityRatio;
		}
	
		private int deriveLayoutHeight() {
			if(_completeScoreDelta != null)
				return  _completeScoreDelta.getMeasuredHeight();
			else
				return _partialScoreDelta.getMeasuredHeight();
		}
		public int deriveLayoutWidth() {
			
			//Get the width of each StaffHeaderView
			/*if(_completeScoreDelta != null) {
				c = _completeScoreDelta._scoreDelta.ESTABLISHED.
			} else {
				
			}*/
			return deriveLayoutWidthFor(_partialScoreDelta, _completeScoreDelta, _partialVisibilityRatio);
		}
		
		public int deriveLayoutWidthFor(ScoreDeltaView left, ScoreDeltaView right, double leftNess) {
			int w = BRACES_AREA_PX;
			//Remove excess StaffHeaderViews
			int numStaves = _parent._score.getNumStaves();
			while(numStaves < getChildCount())
				removeViewAt(numStaves);
			//Add new StaffHeaderViews as needed
			while(getChildCount() < numStaves) {
				StaffHeaderView sh = new StaffHeaderView(getContext(), getChildCount());
				addView(sh, getChildCount());
			}
			
			//TODO
			int result = (int)(BRACES_AREA_PX * _parent.getScalingFactor());
			
			return (int)(result + 70*_parent.getScalingFactor());
		}
	}
	
	public ScoreDrawingSurface(Context context, ScoreLayout parent) {
		super(context);
		_parent = parent;
		_surface = new SurfaceView(getContext());
		_holder = _surface.getHolder();
		_holder.addCallback(this);
		addView(_surface,0);
		
		// Set up notehead Paint
		__noteheadPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NoteHedz170.ttf"));
		__orange.setColor(Color.argb(255, 255, 182, 10));
		__orange.setStyle(Style.STROKE);
		__black.setStyle(Style.STROKE);
		__blue.setColor(Color.BLUE);
		__blue.setStyle(Style.STROKE);
	}
	
	public int systemHeaderWidth(ScoreDelta scoreD) {
		int result = StaffSpec.CLEF_WIDTH_PX + StaffSpec.TIMESIGNATURE_WIDTH_PX; 
		int maxNumAccidentals = 0;
		for(StaffDelta staffD : scoreD.STAVES) {
			Key k = staffD.ESTABLISHED.KEY;
		}
		return result;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		setWillNotDraw(false);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		_surface.measure(widthMeasureSpec,heightMeasureSpec);
		
		for(int i = 0; i < getChildCount(); i++) {
			
		}

        setMeasuredDimension(resolveSize(Integer.MAX_VALUE, widthMeasureSpec),
                resolveSize(Integer.MAX_VALUE, heightMeasureSpec));
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		_surface.layout(l, t, r, b);
	}
	
	private final Paint __orange = new Paint();
	private final Paint __black = new Paint();
	private final Paint __blue = new Paint();
	@Override 
    protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
        Log.w(TAG, "onDraw Called in ViewGroup");
		Canvas c = _holder.lockCanvas();
		if(c != null) {
			c.drawColor(Color.WHITE);
			
			// Iterate through ScoreHeaderViews
			for(int i = 1; i < getChildCount(); i++) {
				SystemHeaderView header = (SystemHeaderView) getChildAt(i);
				Rect headerRect = new Rect();
				header.getHitRect(headerRect);
				
				// TODO Draw header and staff lines
				//__orange.setColor(header.adjustAlphaToVisibility(__orange.getColor()));
				__orange.setAlpha(header.visibilityToAlpha());
				__black.setAlpha(header.visibilityToAlpha());
				c.drawRect(headerRect, __orange);
				for(int j = 0; j < header.getChildCount(); j++) {
					StaffHeaderView staffHeader = (StaffHeaderView) header.getChildAt(j);
					Rect staffHeaderRect = new Rect();
					staffHeader.getHitRect(staffHeaderRect);
					int staffAreaTop = headerRect.top + staffHeaderRect.top;
					int middleStaffLine = staffAreaTop + staffHeader.staffSpec.ABOVE_CENTER_PX;
					
					// Draw the middle staff line
					c.drawLine((float) (BRACES_AREA_PX * _parent.getScalingFactor()), middleStaffLine, 
							c.getWidth(), middleStaffLine, __black);
					// Draw the other staff lines
					for(int whichLineFromCenter : new int[] {-2, -1, 1, 2}) {
						c.drawLine((float) (BRACES_AREA_PX * _parent.getScalingFactor()), middleStaffLine + (int)(whichLineFromCenter * StaffSpec.HEPTATONICTHIRD_PX * _parent.getScalingFactor()), 
								c.getWidth(), middleStaffLine + (int)(whichLineFromCenter * StaffSpec.HEPTATONICTHIRD_PX * _parent.getScalingFactor()), __black);
					}
				}
			}
			
			// Iterate through the ScoreDeltaViews
			for(int i = 1; i < _parent.getChildCount(); i++) {
				ScoreDeltaView scoreDV = (ScoreDeltaView) _parent.getChildAt(i);
				Rect scoreDVRect = new Rect();
				scoreDV.getHitRect(scoreDVRect);
				Log.i(TAG,"ScoreRect:" + scoreDVRect);
				
				c.drawRect(scoreDVRect, __blue);
				for(int j = 0; j < scoreDV.getChildCount(); j++) {
					StaffDeltaView staffDV = (StaffDeltaView) scoreDV.getChildAt(j);
					Rect staffDVRect = new Rect();
					staffDV.getHitRect(staffDVRect);
					staffDVRect.offsetTo(scoreDVRect.left, scoreDVRect.top + staffDVRect.top);
					staffDVRect.intersect(scoreDVRect);
					Log.i(TAG,"StaffRect:" + staffDVRect);

					c.drawRect(staffDVRect, __blue);
					
					Rect staffAreaRect = new Rect();
					staffDV._staffArea.getHitRect(staffAreaRect);
					staffAreaRect.offsetTo(staffDVRect.left, staffDVRect.top + staffAreaRect.top);
					staffAreaRect.intersect(staffDVRect);
					c.drawRect(staffAreaRect, __blue);
					
					int staffCenterOffset = staffDV.getActualVerticalStaffSpec().ABOVE_CENTER_PX;
					
					c.drawLine(staffAreaRect.left, staffAreaRect.top + staffCenterOffset,
							staffAreaRect.right, staffAreaRect.top + staffCenterOffset, __blue);
				}
			}
			_holder.unlockCanvasAndPost(c);
		}
    }
	
	private final Paint __noteheadPaint = new Paint();
	private void drawNoteHeads( StaffDeltaView v, Rect rect ) {
		StaffDelta d = v.getStaffDelta();
		Clef c = d.ESTABLISHED.CLEF;
		for(VoiceDelta vd : d.VOICES) {
			PitchSet pitchSetToDraw = null;
			Rational durationToDraw = null;
			if(vd.CHANGED.NOTES != null) {
				pitchSetToDraw = vd.CHANGED.NOTES;
				durationToDraw = pitchSetToDraw.NOTEHEADLOCS[1].minus(pitchSetToDraw.NOTEHEADLOCS[0]);
				for(String noteName : vd.CHANGED.NOTES.NOTENAMES) {
					int stepsFromCenter = c.getHeptatonicStepsFromCenter(noteName);
				}
			} else if(vd.ESTABLISHED.NOTES.NOTEHEADLOCS.length > 2) {
				for(int r = 1; r < vd.ESTABLISHED.NOTES.NOTEHEADLOCS.length - 1; r++) {
					if(vd.ESTABLISHED.NOTES.NOTEHEADLOCS[r].equals(d.LOCATION)) {
						pitchSetToDraw = vd.ESTABLISHED.NOTES;
						durationToDraw = pitchSetToDraw.NOTEHEADLOCS[r + 1].minus(pitchSetToDraw.NOTEHEADLOCS[r]);
					}
				}
			}
			
			if(pitchSetToDraw != null) {
				for(String noteName : pitchSetToDraw.NOTENAMES) {
					int stepsFromCenter = c.getHeptatonicStepsFromCenter(noteName);
				}
			}
		}
	}
}
