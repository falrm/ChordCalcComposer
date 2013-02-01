package com.jonlatane.composer.music;

import java.util.*;

import android.util.Pair;

public class Staff {
	private Vector<Voice> _voices;
	private Meter _meter;
	public Staff(Voice v) {
		_voices = new Vector<Voice>();
		_voices.add(v);
	}
	public Staff(Vector<Voice> v) {
		_voices = v;
	}
	
	private TreeMap<Rational, Map<Voice,Collection<Object>>> __cache;
	private Rational __previousResult = null;
	public Pair< Rational, Map<Voice,Collection<Object>> > nextChangeAfter(Rational r) {
		if ((__previousResult == null) || (!r.equals(__previousResult))) {
			// Clear and rebuild the cache
			__cache = new TreeMap<Rational, Map<Voice,Collection<Object>>>();
			for(Voice v : _voices) {
				Pair<Rational, Collection<Object>> next = v.nextChangeAfter(r);
				if( next != null ) {
					if (__cache.containsKey(next.first)) {
						Map<Voice, Collection<Object>> cacheContent = __cache
								.get(next.first);
						cacheContent.put(v, next.second);
					} else {
						HashMap<Voice, Collection<Object>> h = new HashMap<Voice, Collection<Object>>();
						h.put(v, next.second);
						__cache.put(next.first, h);
					}
				}
			}
		}
		
		Map.Entry<Rational, Map<Voice,Collection<Object>>> firstCacheResult = __cache.tailMap(r, false).firstEntry();
		Pair< Rational, Map<Voice,Collection<Object>> > result = null;
		if( firstCacheResult != null ) {
			// Get result
			result = new Pair< Rational, Map<Voice,Collection<Object>> >(firstCacheResult.getKey(), firstCacheResult.getValue());
			
			// Update the cache
			for(Map.Entry<Voice,Collection<Object>> e : firstCacheResult.getValue().entrySet()) {
				Voice v = e.getKey();
				Pair<Rational, Collection<Object>> next = v.nextChangeAfter(r);
				if(next != null ) {
					if (__cache.containsKey(next.first)) {
						Map<Voice, Collection<Object>> cacheContent = __cache
								.get(next.first);
						cacheContent.put(v, next.second);
					} else {
						HashMap<Voice, Collection<Object>> h = new HashMap<Voice, Collection<Object>>();
						h.put(v, next.second);
						__cache.put(next.first, h);
					}
				}
			}
		}
		
		return result;
	}
}
