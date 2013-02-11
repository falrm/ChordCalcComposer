package com.jonlatane.composer.music;

import java.util.*;
import java.util.Map.Entry;

import android.util.Pair;

public class Score {
	private Vector<Staff> _staves;
	
	public void addStaff(Staff s) {
		_staves.add(s);
	}
	
	public void removeStaff(Staff s) {
		_staves.remove(s);
	}
	
	private TreeMap<Rational, Map<Staff,Map<Voice,Collection<Object>>>> __cache;
	private Rational __previousResult = null;
	public Pair<Rational, Map<Staff,Map<Voice,Collection<Object>>>> nextChangeAfter(Rational r) {
		if ((__previousResult == null) || (!r.equals(__previousResult))) {
			// Clear and rebuild the cache
			__cache = new TreeMap<Rational, Map<Staff,Map<Voice,Collection<Object>>>>();
			for(Staff s : _staves) {
				Pair<Rational, Map<Voice, Collection<Object>>> next = s.nextChangeAfter(r);
				if(next != null) {
					if (__cache.containsKey(next.first)) {
						Map<Staff, Map<Voice, Collection<Object>>> cacheContent = __cache
								.get(next.first);
						cacheContent.put(s, next.second);
					} else {
						HashMap<Staff, Map<Voice, Collection<Object>>> h = new HashMap<Staff, Map<Voice, Collection<Object>>>();
						h.put(s, next.second);
						__cache.put(next.first, h);
					}
				}
			}
		}
		
		Entry<Rational, Map<Staff, Map<Voice, Collection<Object>>>> firstCacheResult = __cache.tailMap(r,false).firstEntry();
		Pair< Rational, Map<Staff, Map<Voice, Collection<Object>>> > result = null;
		
		// Check and see if there is anything after r and update the cache if there is.
		if( firstCacheResult != null ) {
			result = new Pair< Rational, Map<Staff, Map<Voice, Collection<Object>>> >(firstCacheResult.getKey(), firstCacheResult.getValue());
			// Update the cache
			for(Entry<Staff, Map<Voice, Collection<Object>>> e : firstCacheResult.getValue().entrySet()) {
			//for(Voice v : e.getValue().getLeft()) { - old way
				Staff s = e.getKey();
				Pair< Rational, Map<Voice,Collection<Object>> > next = s.nextChangeAfter(r);
				if(next != null) {
					if (__cache.containsKey(next.first)) {
						Map<Staff, Map<Voice, Collection<Object>>> cacheContent = __cache
								.get(next.first);
						cacheContent.put(s, next.second);
					} else {
						HashMap<Staff, Map<Voice, Collection<Object>>> h = new HashMap<Staff, Map<Voice, Collection<Object>>>();
						h.put(s, next.second);
						__cache.put(next.first, h);
					}
				}
			}
		}
		
		
		return result;
	}
}
