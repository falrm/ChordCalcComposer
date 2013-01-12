package com.jonlatane.composer.music;

import java.util.*;

public class RhythmMap<K> {
	TreeMap<Rational,K> _data;
	
	public RhythmMap() {
		_data = new TreeMap<Rational, K>();
	}
	
	public NavigableSet<Rational> getRhythm() {
		return _data.navigableKeySet();
	}
	
	
	public K getObjectAt(Rational r) {
		Rational l = _data.lowerKey(r);
		
		if( l == null ) {
			return null;
		}
		
		return _data.get(r);
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
}
