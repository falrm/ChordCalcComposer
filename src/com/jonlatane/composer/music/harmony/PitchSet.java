package com.jonlatane.composer.music.harmony;

import java.util.*;

/**
* A PitchSet represents a set of pitches.  In this model, middle C (C4) is 0.
*/
public class PitchSet extends TreeSet<Integer> {
	private static final long serialVersionUID = -3127526528166358783L;

	/**
	 * NOTENAMES is a field to be filled in by the rendering layer.  It determines whether a C chord
	 * is rendered as CEG or B#FbG, essentially.  NOTENAMES should be filled to match the notes of
	 * the PitchSet/Chord/Scale in ascending order.  This means, in Chords, the note names are essentially
	 * listed from C on up for each note in the chord.
	 */
	public String[] NOTENAMES = null;
	
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
	
	/**
	 * It is imperative to use this and NOT a null pointer to represent a rest.
	 * Null pointers indicate the end of the score.
	 */
	public static final PitchSet REST = new PitchSet();
	
	/**
	 * Convert a note name ("C4", "D") to a PitchSet containing that note.  If no octave is specified,
	 * it is assumed to be 4.
	 * 
	 * @param note
	 * @return
	 */
	public static PitchSet toPitchSet(String note) {
		PitchSet result = new PitchSet();
		result.add(Key.noteNameToInt(note));
		return result;
	}

	/**
	 * Convert an array of note names (C3, G3, E4, B4) to a PitchSet
	 * 
	 * @param notes an array of note names.  If no octave is specified they are assumed to be in the 4th octave
	 * @return
	 */
	public static PitchSet toPitchSet(String[] notes) {
		PitchSet result = new PitchSet();
		for(String s : notes) {
			result.add(Key.noteNameToInt(s));
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		int result = 2;
		for(Integer i : this ) {
			result = result ^ i;
		}
		return result;
	}
}
