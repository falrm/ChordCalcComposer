package com.jonlatane.composer.music;

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
}
