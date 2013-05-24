package com.jonlatane.composer.scoredisplay;

import java.util.Collection;

import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.Score.Staff.Voice.VoiceDelta;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Enharmonics;
import com.jonlatane.composer.music.harmony.PitchSet;
import com.jonlatane.composer.music.harmony.Chord.Modulus;

/**
 * StaffSpec is a container class for the various specifications we'll keep
 * @author Jon Latane
 *
 */
public class StaffSpec {
	private StaffSpec() {}
	
	// A Heptatonic Third is, visually, the distance between two staff lines.
	public static final int HEPTATONICTHIRD = 10;
	public static final int HEPTATONICSTEP = HEPTATONICTHIRD/2;
	public static final int DEFAULTSTAFFHEIGHT = HEPTATONICTHIRD * 4;
	
	private static final int ACCIDENTAL_WIDTH = 15;
	private static final int NOTEHEAD_WIDTH = 20;
	static final int TIMESIGNATURE_WIDTH = 50;
	static final int CLEF_WIDTH = 50;

	public static class VerticalStaffSpec {
		public static final VerticalStaffSpec DEFAULT = new VerticalStaffSpec(40, 40);
		
		public final int ABOVE_CENTER_PX, BELOW_CENTER_PX;
	
		public VerticalStaffSpec(int above, int below) {
			assert(above > 0 && below > 0);
			ABOVE_CENTER_PX = above;
			BELOW_CENTER_PX = below;
		}
		
		public static VerticalStaffSpec best(VerticalStaffSpec ss1, VerticalStaffSpec ss2) {
			int above, below;
			above = Math.max(ss1.ABOVE_CENTER_PX, ss2.ABOVE_CENTER_PX);
			below = Math.max(ss1.BELOW_CENTER_PX, ss2.BELOW_CENTER_PX);
			return new StaffSpec.VerticalStaffSpec(above, below);
		}
	}

	/**
	 * A HorizontalStaffSpec defines the widths of the areas for drawing accidentals, noteheads,
	 * time signature changes and key signature changes within a StaffDelta.   
	 * @author Jon Latane
	 *
	 */
	public static class HorizontalStaffSpec {
		/**
		 * The default value defines always-present padding between accidentals, noteheads,
		 * time signatures, and key signatures.  Since these margins are to the left, the area
		 * to draw noteheads with nothing in the other areas is a 10 pixel margen on each side.
		 */
		public static final HorizontalStaffSpec DEFAULT = new HorizontalStaffSpec(5, 5, 5, 5);
		
		public final int ACCIDENTAL_AREA_PX, NOTEHEAD_AREA_PX, TIMESIG_PX, KEYSIG_PX;
		
		private HorizontalStaffSpec(int a, int n, int t, int k) {
			ACCIDENTAL_AREA_PX = a;
			NOTEHEAD_AREA_PX = n;
			TIMESIG_PX = t;
			KEYSIG_PX = k;
		}
		
		public HorizontalStaffSpec(ScoreDelta d) {
			int numAccCols = 0;
			for(StaffDelta sd : d.STAVES) {
				for(VoiceDelta vd : sd.VOICES) {
					numAccCols = Math.max(numAccCols, getNumAccidentalColumns(vd.ESTABLISHED.NOTES));
				}
			}
			ACCIDENTAL_AREA_PX = DEFAULT.ACCIDENTAL_AREA_PX + (numAccCols * ACCIDENTAL_WIDTH);
			NOTEHEAD_AREA_PX = DEFAULT.NOTEHEAD_AREA_PX;
			TIMESIG_PX = DEFAULT.TIMESIG_PX;
			KEYSIG_PX = DEFAULT.KEYSIG_PX;
		}
		
		public int getTotalWidth() {
			return ACCIDENTAL_AREA_PX + NOTEHEAD_AREA_PX + TIMESIG_PX + KEYSIG_PX;
		}
		
		public static int getNumAccidentalColumns(PitchSet ps) {
			int result = 0;
			
			// For efficiency, let's assume we'll never need more than 10 "columns" of accidentals.  This allows
			// us to render a double flat on every one of 5 notes within a fifth (e.g., Cbb5, Dbb5, Ebb5, Fbb5, Gbb5).
			// We can reuse a column if it's a heptatonic sixth apart.
			String[] lastAccidentalLocationPerColumn = new String[10];
			for( int j = 0; j < lastAccidentalLocationPerColumn.length; j++) {
				lastAccidentalLocationPerColumn[j] = null;
			}
			
			// Iterate from the top note down.
			for(int i = ps.NOTENAMES.length - 1; i >=0; i--) {
				String name = ps.NOTENAMES[i];
				if(name.charAt(1) == '#' || name.charAt(1) == 'b' || name.charAt(1) == Enharmonics.flat.charAt(0)) {
					// Double flats require two columns to draw properly
					if(name.charAt(2) == 'b' || name.charAt(1) == Enharmonics.flat.charAt(0)) {
						for( int j = 0; j < lastAccidentalLocationPerColumn.length; j++) {
							if( (lastAccidentalLocationPerColumn[j] == null || Math.abs(Modulus.absoluteHeptDistance(name, lastAccidentalLocationPerColumn[j])) >= 5) 
									&& (lastAccidentalLocationPerColumn[j+1] == null || Math.abs(Modulus.absoluteHeptDistance(name, lastAccidentalLocationPerColumn[j+1])) >= 5) ) {
								lastAccidentalLocationPerColumn[j] = name;
								lastAccidentalLocationPerColumn[j+1] = name;
								if( j+1 > result )
									result = j+1;
								break;
							}
						}
					// Everything else (naturals, flats, sharps, double sharps) only needs one column.
					} else {
						for( int j = 0; j < lastAccidentalLocationPerColumn.length; j++) {
							if(lastAccidentalLocationPerColumn[j] == null 
									|| Math.abs(Modulus.absoluteHeptDistance(name, lastAccidentalLocationPerColumn[j])) >= 5) {
								lastAccidentalLocationPerColumn[j] = name;
								if( j > result )
									result = j;
								break;
							}
						}
					}
				}
			}
			
			return result;
		}
		
		public static HorizontalStaffSpec best(HorizontalStaffSpec ss1, HorizontalStaffSpec ss2) {
			int a, n, t, k;
			a = Math.max(ss1.ACCIDENTAL_AREA_PX, ss2.ACCIDENTAL_AREA_PX);
			n = Math.max(ss1.NOTEHEAD_AREA_PX, ss2.NOTEHEAD_AREA_PX);
			t = Math.max(ss1.TIMESIG_PX, ss2.TIMESIG_PX);
			k = Math.max(ss1.KEYSIG_PX, ss2.KEYSIG_PX);

			return new HorizontalStaffSpec(a, n, t, k);
		}
	}
	
	public static void synchronizeHorizontalStaffSpecs(ScoreDelta scd) {
		
	}
	
	public static void synchronizeVerticalStaffSpecs(Collection<ScoreDelta> c) {
		
	}
	
	public static void animateToWidth(Collection<ScoreDelta> c, int width) {
		
	}
}
