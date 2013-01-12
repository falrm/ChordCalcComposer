package com.jonlatane.composer.music;

import java.util.*;

import com.jonlatane.composer.music.coverings.TimeSignature;

/**
 * A Meter keeps track of time signatures.  If it is empty, it is freeform.
 * An entry of 4/4 at 0 implies everything is in 4/4.  An entry of 4/4 at 2 might imply two
 * beats of pickup into 4/4 time.
 */
public class Meter extends RhythmMap<TimeSignature> {
	public Meter() {
		super();
	}
	
	public Rational getBeatOf(Rational r) {
		TimeSignature ts = getObjectAt(r);
		if(ts == null) {
			return null;
		}
		Rational n = r.minus(getLastAttack(r));
		Integer i = Double.valueOf( n.toDouble() ).intValue() % ts.TOP;
		return n.minus( new Rational(ts.TOP, 1).times(new Rational(i, 1)) );
	}
	
	public Integer getMeasureOf(Rational r) {
		TimeSignature ts = getObjectAt(r);
		Rational lastTSChange = getLastAttack(r);
		
		// Calculate the measures since the last time signature change
		Integer measures = Double.valueOf( Math.ceil( (r.minus(lastTSChange)).times(new Rational(1, ts.TOP)).toDouble()) ).intValue();
		
		// Go backwards through underlying data to count all measures defined in the piece (ideally there are none before 0?)
		for( Map.Entry<Rational,TimeSignature> e : _data.headMap(lastTSChange, false).descendingMap().entrySet() ) {
			measures = measures + Double.valueOf( Math.ceil( (lastTSChange.minus(e.getKey())).times(new Rational(1, e.getValue().TOP)).toDouble()) ).intValue();
			lastTSChange = e.getKey();
		}
		
		return measures;
	}
}
