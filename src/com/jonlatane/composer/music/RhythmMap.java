package com.jonlatane.composer.music;

import java.util.*;

/**
 * A RhythmMap is a mapping from a continuous interval of Real numbers onto the space of possible Objects of type K.
 * It is defined by a set of non-intersecting, half-open intervals with Rational endpoints.  This is implemented with
 * an underlying TreeMap<Rational,K> but that
 * 
 * 
 * @author Jon
 *
 * @param <K>
 */
public class RhythmMap<K> {
	protected TreeMap<Rational,K> _data;
	
	public RhythmMap() {
		_data = new TreeMap<Rational, K>();
	}
	
	public NavigableSet<Rational> getRhythm() {
		return _data.navigableKeySet();
	}
	
	
	public K getObjectAt(Rational r) {
		Rational l = _data.floorKey(r);
		
		if( l == null ) {
			return null;
		}
		
		return _data.get(l);
	}
	
	public Rational getLastAttack(Rational r) {
		return _data.lowerKey(r);
	}
	
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
