package com.jonlatane.composer.music.coverings;

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
}
