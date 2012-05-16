package com.jonlatane.composer.music;

import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import android.content.res.Resources;

// Our abstract view about what's "Important" on a staff itself.
// This includes time signatures and measurings (all derived from the Work),
// clefs (which may be derived from instrument Part resources),
// transpositions (derived from Part resources),
// and dynamic access to transposed versions of key signatures.
// and Segment pointers to the relevant sections that should be displayed on the staff.
// 
// TODO: actually implement this.
public class Staff
{
	// This is populated at runtime from application resources and used to recognize
	// clefs.
	public static final Map<Clef,Integer> CLEF_OFFSETS = new TreeMap<Clef,Integer>();
	
	public enum Clef {
		TREBLE, BASS
	}
	
	private TreeSet<Segment> segments;
	
}
