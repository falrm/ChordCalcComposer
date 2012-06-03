package com.jonlatane.composer.music;
import java.util.Vector;
import java.util.TreeSet;
import java.util.Set;
import java.util.SortedSet;

public class Work {
	private Set<Voice> _voices;
	
	public Work() {
		_voices = new TreeSet<Voice>();
	}
	
	public Work( Work w ) {
		_voices = w.getVoices();
	}
	
	private TreeSet<Rational> _rhythm =  new TreeSet<Rational>();
	public SortedSet<Rational> getRhythm() {
		return _rhythm;
	}
	void updateRhythm() {
		_rhythm.clear();
		for( Voice v : _voices ) {
			_rhythm.addAll(v.getRhythm());
		}
	}
	
	public Set<Voice> getVoices() {
		return _voices;
	}
}
