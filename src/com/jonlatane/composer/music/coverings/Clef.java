package com.jonlatane.composer.music.coverings;

import android.util.Pair;

import com.jonlatane.composer.music.harmony.PitchSet;

/**
 * A Clef, assuming that any clef *must* define Middle C/white keys in general as being
 * on either a space or a line, may be represented as an integer.  This integer is to be
 * treated as heptatonically.  The integer values of each Clef below represent how many
 * steps middle C is above its "normal" position on the treble clef (the first ledger
 * line below the staff).
 * 
 * @author Jon Latane
 *
 */
public class Clef {
	public static final int TREBLE = 0;
	public static final int TREBLETENOR=7;
	public static final int BASS = 12;
	public static final int ALTO = 6;

	public int TYPE;
	
	public Clef(int type) {
		TYPE = type;
	}
	
	public static Clef treble() {
		return new Clef(TREBLE);
	}
	public static Clef trebleTenor() {
		return new Clef(TREBLETENOR);
	}
	public static Clef bass() {
		return new Clef(BASS);
	}
	public static Clef alto() {
		return new Clef(ALTO);
	}
	
	/**
	 * Returns a pair <lower, upper> representing the total span in Heptatonic steps
	 * from the center of this Clef.  For instance, "A4, C5" on a Treble Clef will return
	 * the pair <-1, 1>, because A4 and C5 are respective one step below and above B4, 
	 * the center of the Treble Clef staff.
	 * 
	 * This requires that the NOTENAMES field of the PitchSet is set.
	 * 
	 * @param ps
	 * @return
	 */
	public Pair<Integer, Integer> getHeptatonicStepsFromCenter(PitchSet ps) {
		String bottomNoteName = ps.NOTENAMES[0];
		String topNoteName = ps.NOTENAMES[ps.NOTENAMES.length - 1];
		
		// To be easy, let's just solve this for the Treble clef.
		int bottomNoteValueFromTreble, topNoteValueFromTreble;
		switch(bottomNoteName.charAt(0)) {
			case 'B': bottomNoteValueFromTreble = 0; break;
			case 'C': bottomNoteValueFromTreble = -6; break;
			case 'D': bottomNoteValueFromTreble = -5; break;
			case 'E': bottomNoteValueFromTreble = -4; break;
			case 'F': bottomNoteValueFromTreble = -3; break;
			case 'G': bottomNoteValueFromTreble = -2; break;
			case 'A': bottomNoteValueFromTreble = -1; break;
			default: throw new Error();
		}
		switch(topNoteName.charAt(0)) {
			case 'B': topNoteValueFromTreble = 0; break;
			case 'C': topNoteValueFromTreble = -6; break;
			case 'D': topNoteValueFromTreble = -5; break;
			case 'E': topNoteValueFromTreble = -4; break;
			case 'F': topNoteValueFromTreble = -3; break;
			case 'G': topNoteValueFromTreble = -2; break;
			case 'A': topNoteValueFromTreble = -1; break;
			default: throw new Error();
		}
		int bottomNoteOctave = Character.getNumericValue( bottomNoteName.charAt(bottomNoteName.length()-1) );
		bottomNoteValueFromTreble += 7 * ( Character.getNumericValue( bottomNoteName.charAt(bottomNoteName.length()-1) ) - 4);
		topNoteValueFromTreble += 7 * ( Character.getNumericValue( topNoteName.charAt(topNoteName.length()-1) ) - 4);
		
		bottomNoteValueFromTreble += TYPE;
		topNoteValueFromTreble += TYPE;
		
		return new Pair<Integer, Integer>(bottomNoteValueFromTreble, topNoteValueFromTreble);
	}
}
