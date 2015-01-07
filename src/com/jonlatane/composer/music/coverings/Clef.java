package com.jonlatane.composer.music.coverings;

import android.util.Log;
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
	private static final String TAG = "Clef";
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
	 * Get the number of steps from the center of the staff to the note with the given name
	 * @param noteName
	 * @return
	 */
	public int getHeptatonicStepsFromCenter(String noteName) {
		int distFromTrebleCenter;
		switch(noteName.charAt(0)) {
			case 'B': distFromTrebleCenter = 0; break;
			case 'C': distFromTrebleCenter = -6; break;
			case 'D': distFromTrebleCenter = -5; break;
			case 'E': distFromTrebleCenter = -4; break;
			case 'F': distFromTrebleCenter = -3; break;
			case 'G': distFromTrebleCenter = -2; break;
			case 'A': distFromTrebleCenter = -1; break;
			default: throw new Error();
		}
		int octave = Character.getNumericValue(noteName.charAt(noteName.length()-1));
		distFromTrebleCenter += 7 * ( octave - 4);
		
		distFromTrebleCenter += TYPE;
		if(noteName.length() > 2)
			Log.i(TAG, noteName + "evaluated to " + distFromTrebleCenter);
		
		return distFromTrebleCenter;
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
		
		return new Pair<Integer, Integer>(getHeptatonicStepsFromCenter(bottomNoteName), getHeptatonicStepsFromCenter(topNoteName));
	}

    public String toString() {
        switch(TYPE) {
            case TREBLE: return "Treble";
            case BASS: return "Bass";
            case TREBLETENOR: return "Tenor Treble";
            case ALTO: return "Alto";
            default: return Integer.toString(TYPE);
        }
    }
}
