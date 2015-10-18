package com.jonlatane.composer.music.harmony;

import com.jonlatane.composer.music.Rational;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
* A PitchSet represents a set of pitches.  In this model, middle C (C4) is 0.
* 
* Of important note are the fields noteNameCache and DURATION.  These provide a caching layer - while a PitchSet
* does not manage this information itself (as context is needed to fill in these fields) the rendering layer
* may store it in these fields and invalidate them by setting them to null.
*/
public class PitchSet extends TreeSet<Integer> {
	private static final String TAG = "PitchSet";
    public static final char FLAT = '\u266D';
    public static final char NATURAL = '\u266E';	/**
	 * It is imperative to use this and NOT a null pointer to represent a rest.
	 */
	public static final PitchSet REST = new PitchSet();

    public class Voicing extends TreeSet<Integer> {
		private static final long serialVersionUID = 7889990288839184253L;};
	private static final long serialVersionUID = -3127526528166358783L;

	/**
	 * noteNameCache is a field to be filled in by the rendering layer.  It determines whether a C chord
	 * is rendered as CEG or B#FbG, essentially.  noteNameCache should be filled to match the notes of
	 * the PitchSet/Chord/Scale in ascending order.  This means, in Chords, the note names are essentially
	 * listed from C on up for each note in the Chord, regardless of its root.
	 */
	public String[] noteNameCache = null;
	
	/**
	 * An array of at minimum one element representing how to draw a note.  PitchSets do not manage these,
	 * they are managed by a caching layer based on context.
	 * <p>
	 * In 4/4 time:
	 * </p>
	 * <ul>
	 * 	<li>[1] is a quarter note</li>
	 *  <li>[1, 1 1/4] is a quarter note tied to a sixteenth</li>
	 *  <li>[1, 1 1/2, 1 3/4] is a quarter note tied to an eighth tied to a sixteenth</li>
	 *  <li>[1 3/4] is a double dotted quarter note, equivalent musically to the previous entry</li>
	 * </ul> 
	 */
	public Rational[] tying = null;
	
	
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
	 * Convert a note name ("C4", "D") to a PitchSet containing that note.  If no octave is specified,
	 * it is assumed to be 4.
	 * 
	 * @param note
	 * @return
	 */
	public static PitchSet toPitchSet(String note) {
		PitchSet result = new PitchSet();
		result.add(Enharmonics.noteNameToInt(note));
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
			result.add(Enharmonics.noteNameToInt(s));
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

    @Override
    public String toString() {
		String result = TAG + "[";
		Iterator<Integer> itr = iterator();
		while(itr.hasNext()) {
			result += itr.next();
			if(itr.hasNext())
				result += ",";
		}
		result += "]";
		if(noteNameCache != null) {
			result += ":" + noteNameCache.toString();
		}
		return result;
    }
}
