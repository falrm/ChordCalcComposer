package com.jonlatane.composer.music.harmony;

import java.util.Collections;
import java.util.Collection;
/**
 * Scales work like Chords but should contain more notes.
 *
 * This class may construct scales from semantic information
 * in Android Resources
 */
public class Scale extends Chord {
 	public Scale(Modulus m) {
		super(m);
	}

	public Scale(Collection<Integer> c) {
		super(c);
	}

	public Scale(Collection<Integer> c, Modulus m) {
		super(c,m);
	}
	
	public static Scale chromatic(Modulus m) {
		Scale result = new Scale(m);
		for( int i = 0; i < m.OCTAVE_STEPS; i++ ) {
			result.add(i);
		}
		return result;
	}
	
	//returns 0 if the chord is tonic, 2 if it is a ii/II, 4 for a iii/III, 5 for a iv/IV, etc.
	// bounds are constrained by the modulus
	public int rootFunction(Chord c) {
		return MODULUS.getPitchClass(c.getRoot() - getRoot());
	}
}
