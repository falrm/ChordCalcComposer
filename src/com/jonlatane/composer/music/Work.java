package com.jonlatane.composer.music;
import java.util.*;

public class Work extends HashSet<Voice> implements Rhythm.RhythmSynchronized {
	private Set<Voice> _voices;
	
	public Work() {
		_voices = new TreeSet<Voice>();
	}
	
	public Work( Work w ) {
		_voices = w.getVoices();
	}
	
	// We'll cache our rhythm for efficiency
	private Rhythm _rhythm =  new Rhythm();
	public synchronized Rhythm getRhythm() {
		return _rhythm;
	}
	public synchronized void updateRhythm() {
		_rhythm.clear();
		for( Voice v : _voices ) {
			_rhythm.addAll(v);
		}
	}
	
	public Set<Voice> getVoices() {
		return _voices;
	}
}
