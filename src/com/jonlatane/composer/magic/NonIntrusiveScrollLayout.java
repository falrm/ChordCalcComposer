package com.jonlatane.composer.magic;

import com.jonlatane.composer.NonDelayedHorizontalScrollView;
import java.util.Vector;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * This Layout works like a ListAdapter-backed layout but provides means of stretching
 * the views within it.  Essentially, something good for scrolling left-to-right through
 * music or anything else rendered temporally.
 * @author Jon
 *
 */
public class NonIntrusiveScrollLayout extends RelativeLayout {
	public NonIntrusiveScrollLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public interface StretchyView {
		public int getWidth();
		public int getRequestedWidth();
	}
	private Vector<NonDelayedHorizontalScrollView> _scrollViews;
}
