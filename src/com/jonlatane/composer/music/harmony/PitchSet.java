package com.jonlatane.composer.music.harmony;

import java.util.*;

/**
* A PitchSet represents a set of pitches.  We'll assume for the time being middle C (C4) is 0.
* 
* The PitchSet's Modulus (which has a default value of 12) represents the number of values in the octave
*/
public class PitchSet extends TreeSet<Integer> {
	private static final long serialVersionUID = -3127526528166358783L;

	public PitchSet() {
		super();
	}
	
	public PitchSet(int i) {
		super();
		add(i);
	}
	
	public PitchSet(Collection<Integer> c) {
		super(c);
	}
	
	public static final PitchSet REST = new PitchSet();
	
	@Override
	//TODO refine this
	public int hashCode() {
		int result = 2;
		for(Integer i : this ) {
			result = result ^ i;
		}
		return result;
	}
	
	public Chord toChord() {
		return new Chord(this);
	}
	
	public Scale toScale() {
		return new Scale(this);
	}
}
