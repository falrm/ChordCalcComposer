package com.jonlatane.composer.graphics;

import com.jonlatane.composer.music.*;
import android.graphics.*;
import java.util.SortedSet;
import java.util.NavigableSet;
import java.util.Vector;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Set;
import java.util.Map;

import android.util.Log;

public class MainStavesArea extends AbstractStaffContainer implements StaffContainer {
	private Vector<Vector<StaffElement>> staves;
	private TreeSet<Rational> overallRhythm;
	
	private TreeMap<Rational,RhythmElement> rhythmElementMap;
	private Renderer _r;
	
	MainStavesArea(Renderer parent, int x, int y, int width, int height, float scalingFactor, Rational currentBeat) {
		super(x, y, width, height, scalingFactor, currentBeat);
		
		this._r = parent;
		this.staves = new Vector<Vector<StaffElement>>();
		this.overallRhythm = new TreeSet<Rational>();
		this.rhythmElementMap = new TreeMap<Rational,RhythmElement>();
		staves.add(new Vector<StaffElement>());
		for(Part p : _r._work) {
			staves.firstElement().add(new StaffElement(this, p));
		}
		Log.i("Composer","StavesAreaInit");
	}
	
	public void updateOverallRhythm() {
		overallRhythm.clear();
		for(StaffElement s : staves.firstElement()) {
			s.getPart().updateOverallRhythm();
			overallRhythm.addAll(s.getPart().getOverallRhythm());
		}
	}
	
	public NavigableSet<Rational> getOverallRhythm() {
		return this.overallRhythm;
	}
	
	public void hidePart(Part p) {
		
		updateOverallRhythm();
	}
	
	public void showPart(Part p) {
		
		updateOverallRhythm();
	}
	
	@Override
	public void render(Canvas c) {
		// The abstract class handles the hard stuff for us!
		super.render(c);
		Rational firstBeatOfNextSystem =  getCurrentBeat();
		
		for(int currentSystem = 0; currentSystem < staves.size(); currentSystem++)  {
			Vector<StaffElement> v = staves.get(currentSystem);
			Rational firstBeatOfThisSystem = firstBeatOfNextSystem;
			for( StaffElement s : v ) {
				s.setScalingFactor(getScalingFactor());
				s.setStartPosition(firstBeatOfThisSystem);
				try {
				firstBeatOfNextSystem = s.render(c);
				}catch(Throwable e){
					Log.e("Composer",Log.getStackTraceString(e));
				}
			}
			
			if( firstBeatOfNextSystem == null ) {
				// Clear out excess staves if we've rendered everything.
				while(currentSystem + 1 < staves.size()) {
					currentSystem = currentSystem + 1;
					staves.remove(currentSystem);
				}
			} else {
				if(staves.size() > currentSystem+1) {
					
				} else {
					Vector<StaffElement> nextSystem = new Vector<StaffElement>();
					for(StaffElement s : staves.firstElement()) {
						nextSystem.add(new StaffElement(this, s.getPart(), 0, 160, new Paint()));
					}
					staves.add(nextSystem);
				}
			}
		}
	}
	
	public Map<Rational,RhythmElement> getRhythmElementMap() {
		return rhythmElementMap;
	}
}
