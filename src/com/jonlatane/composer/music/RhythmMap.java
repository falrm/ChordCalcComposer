package com.jonlatane.composer.music;

import java.util.*;

import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.PitchSet;

/**
 * A RhythmMap is a mapping from a continuous interval of Real numbers onto the space of possible Objects of type K.
 * It is defined by a mapping of non-intersecting, half-open intervals with Rational endpoints [q, q') -> k.  The
 * object k at any Real (or Rational) number r is defined as being that set in the mapping by the highest q < r 
 * where [q, q) -> k.
 * 
 * Intuitively, this means a whole note in the first measure might be a RhythmMap<PitchSet> with the interval [1,5) defined
 * as mapping to a PitchSet corresponding to, say, C4.
 * 
 * For efficiency, we actually will not represent the endpoint; in fact we will require a RhythmMap to be continuously
 * defined for all numbers greater than the least number for which it has been defined.  So, to make the above note actually
 * a whole note, we have to know what follows it.  Thus we need two commands:
 * 
 *	pitchSetRhythmMap.put(new Rational(1,1), PitchSet.toPitchSet("C4"));
 *	pitchSetRhythmMap.put(new Rational(5,1), PitchSet.REST);
 * 
 * @author Jon
 *
 * @param <K>
 */
public class RhythmMap<K> {
	protected TreeMap<Rational,K> _data;
	private K _defaultValue = null;
	
	public RhythmMap() {
		_data = new TreeMap<Rational, K>();
	}
	
	public void setDefaultValue(K k) {
		_defaultValue = k;
	}
	
	public NavigableSet<Rational> getRhythm() {
		return _data.navigableKeySet();
	}
	
	
	public K getObjectAt(Rational r) {
		Rational l = _data.floorKey(r);
		
		if( l == null ) {
			return _defaultValue;
		}
		
		K result = _data.get(l);
		if( result == null ) {
			return _defaultValue;
		}
		
		return result;
	}
	
	/*public Rational getLastAttack(Rational r) {
		return _data.lowerKey(r);
	}*/
	
	public K put(Rational r, K obj) {
		return _data.put(r, obj);
	}
	
	public K remove(Rational r) {
		return _data.remove(r);
	}
	
	// Subdivide divides the segment given into n pieces and adds their endpoints to _data.
	public void subdivide(Rational start, Rational end, Integer n) {
		//TODO
	}

	public void putAll(Map<? extends Rational, ? extends K> arg0) {
		_data.putAll(arg0);
	}

	public Collection<K> values() {
		return _data.values();
	}

	public void clear() {
		_data.clear();
		
	}

	public boolean isEmpty() {
		return _data.isEmpty();
	}
}
