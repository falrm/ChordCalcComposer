package com.jonlatane.composer.music;

import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Map;
import java.util.Vector;
import android.util.Log;

// The Part also contains instrument information
public class Part extends Vector<Voice> {
	Work parent;
	
	private TreeSet<Rational> overallRhythm;
	
	// These things will be extended in fun ways later on!
	public TreeMap<Segment, Object> indications;
	public TreeMap<Vector<Segment>, Object> patterns;
	
	public TreeMap<Rational,Object> tempos;
	public TreeMap<Rational,Object> keySignatures;
	
	public Part() {
		super();
		overallRhythm = new TreeSet<Rational>();
	}
	
	public TreeSet<Rational> getOverallRhythm() {
		return overallRhythm;
	}
	
	/*public synchronized void updateOverallRhythm() {
		overallRhythm.clear();
		for( Voice v : this ) {
			for( Map.Entry<Rational,PitchSet> m : v.entrySet() ) {
				overallRhythm.add(m.getKey());
				for( Rational r : m.getValue().subPulses ) {
					overallRhythm.add(r);
				}
			}
		}
	}*/
}
