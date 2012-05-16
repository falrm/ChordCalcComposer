package com.jonlatane.composer.music;

import java.util.Vector;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.TreeMap;

/**
* A PitchSet represents a set of pitches.  We'll assume for the time being middle C (C4) is 0.
* 
* The PitchSet's Modulus (which has a default value of 12) represents the number of values in the octave
*/
public class PitchSet extends TreeSet<Integer> {
	/**
	 * SubPulses indicate points where a note should be tied visually.
	 * More aurally speaking, these may reflect vibrato patterns or ideas
	 * of underlying motion.
	 */
	public TreeSet<Rational> subPulses = new TreeSet<Rational>();
	
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
	
	/* 
	* Methods for constructing pitch from canonical representation
	*/
	private static final TreeMap<Character,Integer> canonical_pitch_map;
	static {
		canonical_pitch_map = new TreeMap<Character,Integer> ();
		canonical_pitch_map.put('C',0);
		canonical_pitch_map.put('D',2);
		canonical_pitch_map.put('E',4);
		canonical_pitch_map.put('F',5);
		canonical_pitch_map.put('G',7);
		canonical_pitch_map.put('A',9);
		canonical_pitch_map.put('B',11);
	}
	public static PitchSet pitch( String canonical_representation ) {
		PitchSet result = new PitchSet();
		char[] chars = canonical_representation.toCharArray();
		int pitch = canonical_pitch_map.get(chars[0]);
		int charIndex = 1;
		if(chars[charIndex] == 'b') {
			pitch = pitch - 1;
			charIndex = charIndex + 1;
		}
		if(chars[charIndex] == '#') {
			pitch = pitch + 1;
			charIndex = charIndex + 1;
		}
		String octaveString = canonical_representation.substring(charIndex);
		int octave = Integer.parseInt(octaveString);
		
		pitch = pitch + 12 * (octave - 4);
		
		result.add(pitch);
		return result;
	}
	
	public static final PitchSet REST = new PitchSet();
}
