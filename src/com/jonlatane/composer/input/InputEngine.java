package com.jonlatane.composer.input;

import android.view.View;
import android.view.MotionEvent;
import java.util.Iterator;
import java.util.SortedSet;
import com.jonlatane.composer.music.*;
import com.jonlatane.composer.graphics.*;
import android.util.Log;

public class InputEngine implements View.OnTouchListener {
	private Renderer _r;
	private long _t;
	
	public InputEngine(Renderer r) {
		this._r = r;
	}
	
	
	Float _oldX = null;
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if( e.getAction() == MotionEvent.ACTION_DOWN ) {
			Log.e("Composer","DOWN");
			_oldX = e.getX();
			_r.STAVES.setSnapScrolling(false);
			_t = System.currentTimeMillis();
		}
		if( e.getAction() == MotionEvent.ACTION_UP ) {
			Log.e("Composer","UP");
			_r.STAVES.setSnapScrolling(true);
			_oldX = null;
		}
		if( e.getAction() == MotionEvent.ACTION_MOVE ) {
			Log.e("Composer","MOVE");
			Log.e("Composer",Float.toString(e.getX()));
			
			float distance;
			if(_oldX != null) {
				distance = e.getX() - _oldX;
			} else {
				distance = 0;
			}
			long now = System.currentTimeMillis();
			float speed = distance/(now-_t);
			
			_r.STAVES.setVelocity(speed/50);
			_oldX = e.getX();
			_t = now;
		}
		return true;
	}
}
