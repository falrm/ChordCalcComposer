package com.jonlatane.composer.music;

import com.jonlatane.composer.music.coverings.TimeSignature;

import java.util.Map;

/**
 * A Meter keeps track of time signatures.  If it is empty, it is freeform.
 * An entry of 4/4 at 0 implies everything is in 4/4.  An entry of 4/4 at 2 might imply two
 * beats of pickup into 4/4 time.
 */
public class Meter extends RhythmMap<TimeSignature> {
	public Meter() {
		super();
	}
	
	/**
	 * Return the next downbeat (beat 1) after the given point
	 * @param r
	 * @return
	 */
	public Rational nextDownBeat(Rational r) {
		Rational meterEstablishedAt = _data.floorKey(r);
		TimeSignature ts = _data.get(meterEstablishedAt);
		Rational result = meterEstablishedAt;
		Rational inc = Rational.get(ts.TOP);
		while(result.compareTo(r) <= 0) {
			result = result.plus( inc );
		}
		
		// Look ahead - if a new Time Signature overrode our
		// old time signature before it completed a measure
		// we need to compensate for that.
		Rational higher = _data.higherKey(r);
		
		if(higher != null && result.compareTo(higher) > 0)
			result = higher;

		return result;
	}
	
	public Rational getBeatOf(Rational r) {
		TimeSignature ts = getObjectAt(r);
		if(ts == null) {
			return null;
		}
		Rational meterEstablishedAt = _data.floorKey(r);
		if(meterEstablishedAt == null)
			return null;

        Rational newResult = r.minus(meterEstablishedAt).mod(new Rational(ts.TOP)).plus(Rational.ONE);
        return newResult;

		//Rational n = r.minus(meterEstablishedAt);
		//Integer i = Double.valueOf( n.toDouble() ).intValue() % ts.TOP;
		//Rational result = n.minus( new Rational(ts.TOP, 1).times(new Rational(i, 1)) );
		//return result;
	}
	
	public Integer getMeasureOf(Rational r) {
		TimeSignature ts = getObjectAt(r);
		Rational lastTSChange = _data.lowerKey(r);
		
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
