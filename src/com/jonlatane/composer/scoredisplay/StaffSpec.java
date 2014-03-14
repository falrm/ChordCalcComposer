package com.jonlatane.composer.scoredisplay;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.TypeEvaluator;
import android.util.Log;
import android.util.Pair;

import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.Score.Staff.Voice.VoiceDelta;
import com.jonlatane.composer.music.coverings.Clef;
import com.jonlatane.composer.music.coverings.TimeSignature;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Enharmonics;
import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.harmony.PitchSet;
import com.jonlatane.composer.music.harmony.Chord.Modulus;
import com.jonlatane.composer.scoredisplay.ScoreDeltaView.StaffDeltaView;

/**
 * StaffSpec is a container class for the various specifications we'll keep
 * @author Jon Latane
 *
 */
public class StaffSpec {
	private static final String TAG = "StaffSpec";
	private static final Map<Key,Integer> KEY_ACCIDENTALS = new HashMap<Key,Integer>();
	static {
		KEY_ACCIDENTALS.put(Key.CMajor, 0);
		KEY_ACCIDENTALS.put(Key.GMajor, 1);
		KEY_ACCIDENTALS.put(Key.DMajor, 2);
		KEY_ACCIDENTALS.put(Key.AMajor, 3);
		KEY_ACCIDENTALS.put(Key.EMajor, 4);
		KEY_ACCIDENTALS.put(Key.BMajor, 5);
		KEY_ACCIDENTALS.put(Key.FsMajor, 6);
		KEY_ACCIDENTALS.put(Key.CsMajor, 7);
		
		KEY_ACCIDENTALS.put(Key.CbMajor, -7);
		KEY_ACCIDENTALS.put(Key.GbMajor, -6);
		KEY_ACCIDENTALS.put(Key.DbMajor, -5);
		KEY_ACCIDENTALS.put(Key.AbMajor, -4);
		KEY_ACCIDENTALS.put(Key.EbMajor, -3);
		KEY_ACCIDENTALS.put(Key.BbMajor, -2);
		KEY_ACCIDENTALS.put(Key.FMajor, -1);
		

		KEY_ACCIDENTALS.put(Key.AMinor, 0);
		KEY_ACCIDENTALS.put(Key.EMinor, 1);
		KEY_ACCIDENTALS.put(Key.BMinor, 2);
		KEY_ACCIDENTALS.put(Key.FsMinor, 3);
		KEY_ACCIDENTALS.put(Key.CsMinor, 4);
		KEY_ACCIDENTALS.put(Key.GsMinor, 5);
		KEY_ACCIDENTALS.put(Key.DsMinor, 6);
		KEY_ACCIDENTALS.put(Key.AsMinor, 7);
		
		KEY_ACCIDENTALS.put(Key.AbMinor, -7);
		KEY_ACCIDENTALS.put(Key.EbMinor, -6);
		KEY_ACCIDENTALS.put(Key.BbMinor, -5);
		KEY_ACCIDENTALS.put(Key.FMinor, -4);
		KEY_ACCIDENTALS.put(Key.CMinor, -3);
		KEY_ACCIDENTALS.put(Key.GMinor, -2);
		KEY_ACCIDENTALS.put(Key.DMinor, -1);
	}
	
	private StaffSpec() {}
	
	// A Heptatonic Third is, visually, the distance between two staff lines.
	public static final int HEPTATONICTHIRD_PX = 10;
	public static final int HEPTATONICSTEP_PX = HEPTATONICTHIRD_PX/2;
	public static final int DEFAULTSTAFFHEIGHT_PX = HEPTATONICTHIRD_PX * 4;
	
	private static final int ACCIDENTAL_WIDTH_PX = 15;
	private static final int NOTEHEAD_WIDTH_PX = 20;
	static final int TIMESIGNATURE_WIDTH_PX = 50;
	static final int CLEF_WIDTH_PX = 50;

	public static class VerticalStaffSpec {
		public static final VerticalStaffSpec DEFAULT = new VerticalStaffSpec(0, 0, 0, 0);
		public static final int DEFAULT_MARGIN_IN_STEPS = 3;
		
		public final int ABOVE_CENTER_PX, BELOW_CENTER_PX;
		public final int UPPER_AREA_PX, LOWER_AREA_PX;
	
		private VerticalStaffSpec(int above, int below, int upper, int lower) {
			assert(above > 0 && below > 0);
			ABOVE_CENTER_PX = above;
			BELOW_CENTER_PX = below;
			UPPER_AREA_PX = upper;
			LOWER_AREA_PX = lower;
		}
		
		public VerticalStaffSpec(StaffDelta d) {
			// F5/E4 are 4 steps above/below B4.
			int neededStepsAboveCenter = 4, neededStepsBelowCenter = 4;
			for(VoiceDelta vd : d.VOICES) {
				PitchSet ps = (vd.CHANGED.NOTES != null) ? vd.CHANGED.NOTES : vd.ESTABLISHED.NOTES;
				if(!ps.equals(PitchSet.REST)) {
					assert(ps.NOTENAMES.length == ps.size());
					Pair<Integer,Integer> p = d.ESTABLISHED.CLEF.getHeptatonicStepsFromCenter(ps);
					neededStepsAboveCenter = Math.max(neededStepsAboveCenter, p.first);
					neededStepsBelowCenter = Math.max(neededStepsBelowCenter, -p.second);
				}
			}
			ABOVE_CENTER_PX = (neededStepsAboveCenter + DEFAULT_MARGIN_IN_STEPS) * HEPTATONICSTEP_PX;
			BELOW_CENTER_PX = (neededStepsBelowCenter + DEFAULT_MARGIN_IN_STEPS) * HEPTATONICSTEP_PX;
			Log.i(TAG,"Created VerticalStaffSpec widht above = " + neededStepsAboveCenter + ", below = " + neededStepsBelowCenter);
			UPPER_AREA_PX = 0;
			LOWER_AREA_PX = 0;
		}
		
		public int getTotalHeight() {
			return ABOVE_CENTER_PX + BELOW_CENTER_PX;
		}
		
		public VerticalStaffSpec adaptToHeight(int targetHeight) {
			//if(targetHeight > getTotalHeight()) {
				double relativeHeightChange = (double)targetHeight/getTotalHeight();
				int newUpper = (int)(relativeHeightChange * UPPER_AREA_PX);
				int newLower = (int)(relativeHeightChange * LOWER_AREA_PX);
				
				int newBelow = (int)(relativeHeightChange * BELOW_CENTER_PX);
				int newAbove = targetHeight - newUpper - newLower - newBelow;
				
				return new VerticalStaffSpec(newAbove, newBelow, newUpper, newLower);
				
				//int addToTop = (targetHeight - getTotalHeight())/2;
				//int addToBottom = targetHeight - getTotalHeight() - addToTop;
				//return new VerticalStaffSpec(ABOVE_CENTER_PX + addToTop, BELOW_CENTER_PX + addToBottom);
			//} else {
			//	int removeFromTop = (getTotalHeight() - targetHeight)/2;
			//	int removeFromBottom = getTotalHeight() - targetHeight - removeFromTop;
			//	return new VerticalStaffSpec(ABOVE_CENTER_PX - removeFromTop, BELOW_CENTER_PX - removeFromBottom);
			//}
		}
		@Override
		public String toString() {
			return "VSS:" + ABOVE_CENTER_PX + ", " + BELOW_CENTER_PX;
		}
		
		public static VerticalStaffSpec best(VerticalStaffSpec ss1, VerticalStaffSpec ss2) {
			int above, below, upper, lower;
			above = Math.max(ss1.ABOVE_CENTER_PX, ss2.ABOVE_CENTER_PX);
			below = Math.max(ss1.BELOW_CENTER_PX, ss2.BELOW_CENTER_PX);
			upper = Math.max(ss1.UPPER_AREA_PX, ss2.UPPER_AREA_PX);
			lower = Math.max(ss1.LOWER_AREA_PX, ss2.LOWER_AREA_PX);
			return new StaffSpec.VerticalStaffSpec(above, below, upper, lower);
		}
		
		public static VerticalStaffSpec[] best(List<ScoreDeltaView> l) {
			VerticalStaffSpec[] result = new VerticalStaffSpec[ l.get(0)._scoreDelta.STAVES.length ];
			for(int i = 0; i < result.length; i++)
				result[i] = VerticalStaffSpec.DEFAULT;
			
			for(ScoreDeltaView sdv : l) {
				for(int i = 0; i < result.length; i++)
					result[i] = best(result[i], ((StaffDeltaView)sdv.getChildAt(i)).getPerfectVerticalStaffSpec());
			}
			return result;
		}
		
		public static VerticalStaffSpec influenceToBest(VerticalStaffSpec influencer, VerticalStaffSpec influencee,
				double influence) {
			VerticalStaffSpec best = best(influencer, influencee);
			int above = influencee.ABOVE_CENTER_PX + (int)( influence * (best.ABOVE_CENTER_PX-influencee.ABOVE_CENTER_PX) );
			int below = influencee.BELOW_CENTER_PX + (int)( influence * (best.BELOW_CENTER_PX-influencee.BELOW_CENTER_PX) );
			int upper = influencee.UPPER_AREA_PX + (int)( influence * (best.UPPER_AREA_PX-influencee.UPPER_AREA_PX) );
			int lower = influencee.LOWER_AREA_PX + (int)( influence * (best.LOWER_AREA_PX-influencee.LOWER_AREA_PX) );
			return new VerticalStaffSpec(above, below, upper, lower);
		}
		
		public static VerticalStaffSpec[] influenceToBest(VerticalStaffSpec[] influencers, VerticalStaffSpec[] influencees, double influence) {
			VerticalStaffSpec[] result = new VerticalStaffSpec[influencees.length];
			for(int i = 0; i < result.length; i++) {
				result[i] = influenceToBest(influencers[i], influencees[i], influence);
			}
			return result;
		}
		
		public static VerticalStaffSpec influenceLeftRight(VerticalStaffSpec left, VerticalStaffSpec right, double leftNess) {
			int above = right.ABOVE_CENTER_PX + (int)(leftNess *(left.ABOVE_CENTER_PX - right.ABOVE_CENTER_PX));
			int below = right.BELOW_CENTER_PX + (int)(leftNess *(left.BELOW_CENTER_PX - right.BELOW_CENTER_PX));
			int upper = right.UPPER_AREA_PX + (int)(leftNess *(left.UPPER_AREA_PX - right.UPPER_AREA_PX));
			int lower = right.LOWER_AREA_PX + (int)(leftNess *(left.LOWER_AREA_PX - right.LOWER_AREA_PX));
			return new VerticalStaffSpec(above, below, upper, lower);
		}
		
		public static VerticalStaffSpec[] influenceLeftRight(VerticalStaffSpec[] lefts, VerticalStaffSpec[] rights, double leftNess) {
			VerticalStaffSpec[] result = new VerticalStaffSpec[rights.length];
			for(int i = 0; i < result.length; i++) {
				result[i] = influenceLeftRight(lefts[i], rights[i], leftNess);
			}
			return result;
		}
		
		public static VerticalStaffSpec scale(VerticalStaffSpec vss, double scale) {
			int a, b, u, l;
			u = (int)(vss.UPPER_AREA_PX * scale);
			l = (int)(vss.UPPER_AREA_PX * scale);
			int remaining = (int)(vss.getTotalHeight() * scale) - u - l;
			a = (int)(((double)vss.ABOVE_CENTER_PX/(double)(vss.ABOVE_CENTER_PX + vss.BELOW_CENTER_PX)) * (double)remaining);
			b = remaining - a;
			return new VerticalStaffSpec(a, b, u, l);
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
		public static final HorizontalStaffSpec DEFAULT = new HorizontalStaffSpec(10, 50, 0, 10, 10);
		
		//TODO use this class!1
		private static class ScDVWidthEvaluator implements TypeEvaluator<Integer> {
			private ScoreDeltaView _sdv;
			public ScDVWidthEvaluator(ScoreDeltaView sd) { _sdv = sd; }
			@Override
	    	public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
		    	Integer targetWidth = startValue + (int)((endValue - startValue) * fraction);
		    	
		    	_sdv.setActualHorizontalStaffSpec(_sdv.getPerfectHorizontalStaffSpec().adaptToWidth(targetWidth));
	    		
	    		return targetWidth;
	    	}
		}
		
		public final int ACCIDENTAL_AREA_PX, NOTEHEAD_AREA_PX, CLEF_PX, KEYSIG_PX, TIMESIG_PX;
		
		private HorizontalStaffSpec(int a, int n, int c, int k, int t) {
			ACCIDENTAL_AREA_PX = a;
			NOTEHEAD_AREA_PX = n;
			CLEF_PX = c;
			KEYSIG_PX = k;
			TIMESIG_PX = t;
		}
		
		public HorizontalStaffSpec(ScoreDelta d) {
			int numAccCols = 0;
			boolean precedesTimeChange = (d.TIME_CHANGE_AFTER != null);
			boolean precedesClefChange = false, precedesKeyChange = false;
			for(StaffDelta sd : d.STAVES) {
				if(sd.CLEF_CHANGE_AFTER != null)
					precedesClefChange = true;
				if(sd.KEY_CHANGE_AFTER != null)
					precedesClefChange = true;
				
				for(VoiceDelta vd : sd.VOICES) {
					numAccCols = Math.max(numAccCols, StaffSpec.getNumAccidentalColumns(vd.ESTABLISHED.NOTES));
				}
			}
			ACCIDENTAL_AREA_PX = DEFAULT.ACCIDENTAL_AREA_PX + (numAccCols * ACCIDENTAL_WIDTH_PX);
			NOTEHEAD_AREA_PX = DEFAULT.NOTEHEAD_AREA_PX;
			CLEF_PX = DEFAULT.CLEF_PX + (precedesClefChange ? CLEF_WIDTH_PX : 0);
			TIMESIG_PX = DEFAULT.TIMESIG_PX + (precedesTimeChange ? TIMESIGNATURE_WIDTH_PX : 0);
			
			KEYSIG_PX = DEFAULT.KEYSIG_PX;
		}
		
		public int getTotalWidth() {
			return ACCIDENTAL_AREA_PX + NOTEHEAD_AREA_PX + TIMESIG_PX + KEYSIG_PX + CLEF_PX;
		}
		
		public HorizontalStaffSpec adaptToWidth(int targetWidth) {
			assert(targetWidth >= 0);
			int a, n, c, k, t;
			
			// Shorten the staffSpec by reducing the above variables to 0 in order.
			if(targetWidth <= getTotalWidth()) {
				/*a = ACCIDENTAL_AREA_PX;
				n = NOTEHEAD_AREA_PX;
				c = CLEF_PX;
				k = KEYSIG_PX;
				t = TIMESIG_PX;*/
				int leftToRemove = getTotalWidth() - targetWidth;
				int[] ancktOut = new int[] {ACCIDENTAL_AREA_PX, NOTEHEAD_AREA_PX, CLEF_PX, KEYSIG_PX, TIMESIG_PX};
				for(int i = 0; i < ancktOut.length; i++) {
					int removable = Math.min(leftToRemove, ancktOut[i]);
					leftToRemove -= removable;
					ancktOut[i] = ancktOut[i] - removable;
				}
				a = ancktOut[0];
				n = ancktOut[1];
				c = ancktOut[2];
				k = ancktOut[3];
				t = ancktOut[4];
				
				/*if(leftToRemove < ACCIDENTAL_AREA_PX) {
					a -= leftToRemove;
				} else {
					a = 0;
					leftToRemove -= ACCIDENTAL_AREA_PX;

					if (leftToRemove < NOTEHEAD_AREA_PX) {
						n -= leftToRemove;
					} else {
						n = 0;
						leftToRemove -= NOTEHEAD_AREA_PX;

						if (leftToRemove < CLEF_PX) {
							c -= leftToRemove;
						} else {
							c = 0;
							leftToRemove -= CLEF_PX;

							if (leftToRemove < KEYSIG_PX) {
								k -= leftToRemove;
							} else {
								k = 0;
								leftToRemove -= KEYSIG_PX;

								if (leftToRemove < TIMESIG_PX) {
									t -= leftToRemove;
								} else {
									t = 0;
									leftToRemove -= TIMESIG_PX;
								}
							}
						}
					}
				}*/
				
			// Lengthen the staffSpec by increasing everything's width as uniformly as possible.
			} else {
				double ratio = (double)targetWidth/(double)getTotalWidth();
				a = (int)(ACCIDENTAL_AREA_PX * ratio);
				n = (int)((ACCIDENTAL_AREA_PX + NOTEHEAD_AREA_PX) * ratio) - a;
				c = (int)((ACCIDENTAL_AREA_PX + NOTEHEAD_AREA_PX + CLEF_PX) * ratio) - a - n;
				t = (int)((ACCIDENTAL_AREA_PX + NOTEHEAD_AREA_PX + TIMESIG_PX) * ratio) - a - n - c;
				k = targetWidth - a - n - c - t;
			}
			
			HorizontalStaffSpec result = new HorizontalStaffSpec(a, n, c, k, t);
			assert(result.getTotalWidth() == targetWidth);
			return result;
		}

		@Override
		public String toString() {
			return "HSS:[" + ACCIDENTAL_AREA_PX + ","
					 + NOTEHEAD_AREA_PX + ","
					  + CLEF_PX + ","
					   + KEYSIG_PX + ","
					    + TIMESIG_PX + "]";
		}
		public static HorizontalStaffSpec best(HorizontalStaffSpec ss1, HorizontalStaffSpec ss2) {
			int a, n, c, k, t;
			a = Math.max(ss1.ACCIDENTAL_AREA_PX, ss2.ACCIDENTAL_AREA_PX);
			n = Math.max(ss1.NOTEHEAD_AREA_PX, ss2.NOTEHEAD_AREA_PX);
			c = Math.max(ss1.CLEF_PX, ss2.CLEF_PX);
			k = Math.max(ss1.KEYSIG_PX, ss2.KEYSIG_PX);
			t = Math.max(ss1.TIMESIG_PX, ss2.TIMESIG_PX);

			return new HorizontalStaffSpec(a, n, c, k, t);
		}
		
		public static HorizontalStaffSpec scale(HorizontalStaffSpec vss, double scale) {
			int a, n, c, k, t;
			if(scale > 1) {
				t = (int)(vss.TIMESIG_PX * scale);
				c = (int)(vss.CLEF_PX * scale);
				int remaining = (int)(vss.getTotalWidth() * scale) - t - c;
				n = (int)(
						(double)vss.NOTEHEAD_AREA_PX/(double)(vss.NOTEHEAD_AREA_PX + vss.ACCIDENTAL_AREA_PX + vss.KEYSIG_PX)
							* (double)remaining);
				a = (int)(
						(double)vss.ACCIDENTAL_AREA_PX/(double)(vss.NOTEHEAD_AREA_PX + vss.ACCIDENTAL_AREA_PX + vss.KEYSIG_PX)
							* (double)remaining);
				k = remaining - n - a;
			} else {
				t = (int)(vss.TIMESIG_PX * scale);
				c = (int)(vss.CLEF_PX * scale);
				n = (int)(vss.NOTEHEAD_AREA_PX * scale);
				a = (int)(vss.ACCIDENTAL_AREA_PX * scale);
				k = (int)(scale * vss.getTotalWidth()) - a - n - c - t;
			}
			
			return new HorizontalStaffSpec(a, n, c, k, t);
		}
	}
	
	
	/*public static class StaffHeaderSpec {
		public static final StaffHeaderSpec DEFAULT = new StaffHeaderSpec(CLEF_WIDTH_PX + 5, 5, 5);
		public final int CLEF_PX, KEYSIG_PX, TIMESIG_PX;
		private StaffHeaderSpec(int c, int k, int t) {
			CLEF_PX = c;
			KEYSIG_PX = k;
			TIMESIG_PX = t;
		}
		
		
		public StaffHeaderSpec(ScoreDelta d) {
			CLEF_PX = DEFAULT.CLEF_PX;
			int widestKeyNumAccidentals = 0;
			
		}
		
		public int getTotalWidth() {
			return CLEF_PX + KEYSIG_PX + TIMESIG_PX;
		}
		public static StaffHeaderSpec best(StaffHeaderSpec shs1, StaffHeaderSpec shs2) {
			int c = Math.max(shs1.CLEF_PX, shs2.CLEF_PX);
			int k = Math.max(shs1.KEYSIG_PX, shs2.KEYSIG_PX);
			int t = Math.max(shs1.TIMESIG_PX, shs2.TIMESIG_PX);
			return new StaffHeaderSpec(c, k, t);
		}
		
		public static StaffHeaderSpec best(Collection<StaffHeaderSpec> c) {
			int c
		}
		
		public static StaffHeaderSpec influenceLeftRight(StaffHeaderSpec left, StaffHeaderSpec right, double leftNess) {
			
		}
	}*/
	
	public static int toStandardKeySignatureWidth(Key k) {
		return Math.abs(KEY_ACCIDENTALS.get(k)) * ACCIDENTAL_WIDTH_PX;
	}

	public static int toStandardSignatureWidth(Clef c, Key k, TimeSignature ts) {
		int result = 0;
		if(c != null)
			result += CLEF_WIDTH_PX;
		else
			result += HorizontalStaffSpec.DEFAULT.CLEF_PX;
		if(k != null)
			result += toStandardKeySignatureWidth(k);
		else
			result += HorizontalStaffSpec.DEFAULT.KEYSIG_PX;
		if(ts != null)
			result += TIMESIGNATURE_WIDTH_PX;
		else
			result += HorizontalStaffSpec.DEFAULT.TIMESIG_PX;
		return result;
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
			if(name.charAt(1) == '#' || name.charAt(1) == 'b' || name.charAt(1) == Enharmonics.FLAT) {
				// Double flats require two columns to draw properly
				if(name.charAt(2) == 'b' || name.charAt(1) == Enharmonics.FLAT) {
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
}
