package com.jonlatane.composer.music;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.jonlatane.composer.music.Score.Staff.Voice;
import com.jonlatane.composer.music.coverings.Clef;
import com.jonlatane.composer.music.coverings.TimeSignature;
import com.jonlatane.composer.music.harmony.*;

/**
 * A Score is an object that is most usefully accessed/rendered via ScoreDeltas through its iterator.  It
 * represents a piece of music as a layered map from Rationals to PitchSets, Articulations, Dynamics,
 * Chords, Scales, Keys, and Meters.  Some of this information is contained in Staves and some in Voices,
 * however the most useful means of accessing it is through the @ScoreDelta class.
 *   
 * @author Jon
 *
 */
public class Score {
	private static final String TAG = "Score";
	private Meter _meter = new Meter();
	private Rational _fine;
	private Staff[] _staves = new Staff[0];
	
	/**
	 * Create a 4-bar score for the given TimeSignature.  The Time Signature is added at Rational point 1,
	 * so the downbeat of the first measure is at Rational.ONE.
	 * 
	 * @param ts
	 */
	public Score(TimeSignature ts) {
		this(ts, 4);
	}
	
	/**
	 * Create a Score containing the requested number of bars for the given time signature. The Time Signature
	 * is added at Rational point 1, so the downbeat of the first measure is at Rational.ONE.
	 * 
	 * @param ts
	 * @param numBars
	 */
	public Score(TimeSignature ts, Integer numBars) {
		this(ts, numBars, Rational.ZERO);
	}
	
	/**
	 * This is not particularly useful except as a boundary case for the model for pickup != 0.  However,
	 * a dotted-sixteenth pickup can be handled by the meter pretty elegantly, though all your downbeats
	 * end up occurring at r = i + 3/8 in the model the Meter finds them at 1, 2, 3, 4 properly.
	 * 
	 * The downbeat of the first measure is at Rational.ONE + pickup, the location of the TimeSignature
	 * within the Meter.
	 * 
	 * @param ts
	 * @param numBars
	 * @param pickup
	 */
	public Score(TimeSignature ts, Integer numBars, Rational pickup) {
		assert(numBars > 0);
		setFine(new Rational(1 + (numBars+1) * ts.TOP, 1));
		_meter.put(pickup.plus(Rational.ONE), ts);
	}
	

	public Rational getFine() {
		return _fine;
	}
	
	/**
	 * Lengthen or shorten your Score in the "best possible" way.
	 * 
	 * @param r
	 */
	public void setFine(Rational r) {
		if(_fine == null)
			_fine = r;
		// Put rests and No Chord if we're increasing the length and nothing is defined there.
		// This way if you accidentally shorten it, you can re-lengthen it to restore data lost,
		// but if you lengthen it you don't get annoying tied whole notes everywhere (though they're
		// still only one PitchSet).
		if(r.compareTo(_fine) > 0) {
			for(Staff s : _staves) {
				if(s._chords._data.navigableKeySet().tailSet(_fine, false).size() == 0) {
					s._chords._data.put(_fine, Chord.NO_CHORD);
				}
				for(Staff.Voice v : s._voices) {
					if(v._notes._data.navigableKeySet().tailSet(_fine, false).size() == 0) {
						v._notes._data.put(_fine, PitchSet.REST);
					}
				}
			}
		}
		_fine = r;
	}
	
	/**
	 * A Staff is in every way like a Score in terms of its mapping behavior, but it does not store a Meter.
	 * That is to say, everything but the time signatures in a piece of music are defined at the Staff level.
	 * 
	 * Moreover, a staff is the most "useful" chunk of information in a score.  It contains harmony.  A Voice
	 * only contains a PitchSet (its NOTES), Articulation and Dynamics, while information such as the given
	 * Chord and Scale is held to be constant within a single Staff.
	 * 
	 * Merging Voices into Staves requires resolution of conflicts that may arise as a result of differences
	 * in Chords, Keys, or Clefs.
	 * 
	 * @author Jon Latane
	 *
	 */
	public class Staff {
		private RhythmMap<Chord> _chords = new RhythmMap<Chord>();
		private RhythmMap<Key> _keys = new RhythmMap<Key>();
		private RhythmMap<Clef> _clefs = new RhythmMap<Clef>();
		private RhythmMap<Scale> _scales = new RhythmMap<Scale>();
		public int TRANSPOSITION = 0;
		public String TITLE = "";
		private Voice[] _voices = new Voice[0];
		
		/**
		 * Access Staves through the newStaff method of Score.
		 */
		private Staff() {
			this(Clef.treble());
		}
		
		private Staff(Clef c) {
			_clefs.setDefaultValue(c);
			_chords.setDefaultValue(Chord.NO_CHORD);
			_keys.setDefaultValue(Key.CMajor);
		}
		
		/**
		 * As stated in @Staff, a Voice only contains a PitchSet (its NOTES), Articulation and Dynamics, while 
		 * information such as the given Chord, Key and Scale is held to be constant within a single Staff.
		 * 
		 * While a Voice does have VoiceDeltas and voiceDeltaAt(r) method, it is not iterable.  Iterating through
		 * a Voice alone is not particularly useful to us and is identical to iterating through a single-voiced
		 * {@link: Staff}.
		 * 
		 * @author Jon Latane
		 *
		 */
		public class Voice {
			private RhythmMap<PitchSet> _notes = new RhythmMap<PitchSet>();
			
			/**
			 * Access Voices through the newVoice method of Staff.
			 */
			private Voice() {}
			
			public class VoiceDelta {
				public Rational LOCATION = null;
				
				/**
				 * A simple class for storing pointers to things in a Voice.
				 * @author Jon Latane
				 *
				 */
				public class VoiceStuff {
					public PitchSet NOTES = null;
				}
				
				public VoiceStuff ESTABLISHED = new VoiceStuff();
				public VoiceStuff CHANGED = new VoiceStuff();
			}
			
			public VoiceDelta voiceDeltaAt(Rational r) {
				VoiceDelta result = new VoiceDelta();
				result.LOCATION = r;
				result.ESTABLISHED.NOTES = _notes.getObjectAt(r);
				
				result.CHANGED.NOTES = _notes._data.get(r);
				
				return result;
			}
		}
		
		/**
		 * A StaffDelta is a collection of information about what has changed and what has remained constant
		 * at a given point in time.  It is most usefully accessed through StaffDelta iterators which allow
		 * us to iterate through scores.
		 * 
		 * @author Jon
		 */
		public class StaffDelta {
			public Rational LOCATION = null;
			
			/**
			 * A simple class for storing pointers to things in a Staff.
			 * @author Jon Latane
			 *
			 */
			public class Established {
				public Key KEY = null;
				public Chord CHORD = null;
			}
			
			public Established ESTABLISHED = new Established();
			
			/**
			 * A simple class for storing pointers to things in a Staff.
			 * @author Jon Latane
			 *
			 */
			public class Changed {
				public Key KEY = null;
				public Chord CHORD = null;
			}
			public Changed CHANGED = new Changed();
			
			public Voice.VoiceDelta[] VOICES;
		}
		
		/**
		 * The most useful means of accessing a StaffDelta.
		 * 
		 * @param r
		 * @return
		 */
		public StaffDelta staffDeltaAt(Rational r) {
			StaffDelta result = new StaffDelta();
			
			result.LOCATION = r;
			
			result.ESTABLISHED.KEY = _keys.getObjectAt(r);
			result.ESTABLISHED.CHORD = _chords.getObjectAt(r);
			
			result.CHANGED.KEY = _keys._data.get(r);
			result.CHANGED.CHORD = _chords._data.get(r);
			
			result.VOICES = new Voice.VoiceDelta[_voices.length];
			for(int i = 0; i < _voices.length; i++) {
				Voice v = _voices[i];
				result.VOICES[i] = v.voiceDeltaAt(r);
			}
			
			return result;
		}

		public Voice[] getVoices() {
			return _voices;
		}
		

		public Voice getVoice(int n) {
			return _voices[n];
		}
		
		public int getNumVoices() {
			return _voices.length;
		}
		
		/**
		 * Try to pick the best chord name for the given passage.  
		 * 
		 * @param start
		 * @param end
		 * @param howMany
		 * @return
		 */
		public TreeMap<Integer,List<String>> guessChord(Rational start, Rational end, int preferredCharacteristicLength) {
			TreeMap<Integer,List<String>> result = new TreeMap<Integer,List<String>>();
			
			// Track which notes occur most frequently at "felt" moments
			SparseIntArray noteOccurences = new SparseIntArray();
			for(Iterator<StaffDelta> itr = staffIterator(start); itr.hasNext(); ) {
				StaffDelta sd = itr.next();
				if(sd.LOCATION.compareTo(end) > 0)
					break;
				for(Voice.VoiceDelta vd : sd.VOICES) {
					for(int n : vd.ESTABLISHED.NOTES) {
						int nClass = Chord.TWELVETONE.mod(n);
						int occurences = noteOccurences.get(nClass);
						noteOccurences.put(nClass, ++occurences);
					}
				}
			}
			
			// Assume the whole passage is in the starting key, or C Major as a fallback
			Key k = _keys.getObjectAt(start);
			if( k == null )
				k = Key.CMajor;
			
			// Keep track of names given for each chord.  When we find the chord, we will choose the modal name
			// for it.
			int[] rootScores = new int[12];
			SparseArray<LinkedList<String>> namesLists = new SparseArray<LinkedList<String>>();
			for(int i = 0; i < 12; i++) {
				namesLists.put(i, new LinkedList<String>());
				rootScores[i] = 0;
			}
			
			// Analyze by all notes, then remove those less frequently used.  Choose the modal name
			// for the best root found.  This is the "heart" of the method - everything before is setup.
			while(noteOccurences.size() > 0) {
				Chord c = new Chord();
				
				// Add all notes to the Chord.  Eliminate the note with the least presses.
				int currentSize = noteOccurences.size();
				while( noteOccurences.size() == currentSize ) {
					for(int i = 0; i < noteOccurences.size(); i++) {
						int key = noteOccurences.keyAt(i);
						int val = noteOccurences.valueAt(i) - 1;
						
						c.add(key);
						
						if(val < 1)
							noteOccurences.removeAt(i--);
						else
							noteOccurences.put(key,val);
					}
				}
				
				// Analyze the Chord we've created
				TreeMap<Integer,List<String>> thisResult = Key.getRootLikelihoodsAndNames(Key.CChromatic, c, k);
				for(Integer score : thisResult.keySet()) {
					for(String s : thisResult.get(score)) {
						
					}
				}
				for(int i = 0; i < 12; i++) {
					//TODO Chord.
				}
			}
			
			
			return result;
		}

		private Iterator<StaffDelta> staffDeltaIterator(final Iterator<Rational> rhythm) {
			return new Iterator<StaffDelta>() {
				@Override
				public boolean hasNext() {
					return rhythm.hasNext();
				}

				@Override
				public StaffDelta next() {
					Rational r = rhythm.next();
					return staffDeltaAt(r);
				}

				@Override
				public void remove() { }
				
			};
		}
		
		/**
		 * Return a StaffDelta iterator that goes forewards from the supplied point
		 * 
		 * @param start the point to iterate  from
		 * @return
		 */
		public Iterator<StaffDelta> staffIterator(final Rational start) {
			return staffDeltaIterator(getOverallRhythm().tailSet(start).iterator());
		}
		
		/**
		 * Return a StaffDelta iterator that goes backwards from the supplied point
		 * 
		 * @param startEndSayWhat the point to iterate backwards from
		 * @return
		 */
		public Iterator<StaffDelta> reverseStaffIterator(final Rational startEndSayWhat) {
			return staffDeltaIterator(getOverallRhythm().headSet(startEndSayWhat,true).descendingIterator());
		}

		/**
		 * Fill end the NOTENAMES field for every Chord, PitchSet and Scale in the given interval using the Keys
		 * present, or C Major if no key is provided.
		 * 
		 * @param start
		 * @param end
		 */
		public void realizeEnharmonics(Rational start, Rational end) {
			Iterator<StaffDelta> backwards = reverseStaffIterator(end);
			
			// This is set when we should define the notes based on the key rather than working backwards.
			// This is set to true to start, and then again whenever we have a key change (so the last notes
			// in a modulation from one key to another are named according to the previous key and we can have,
			// for instance D# to Eb ties).
			boolean isFirst = true;
			
			// We declare these variables here because we're working backwards
			PitchSet ps2 = null;
			Chord c2 = null;
			
			// Iterate backwards from our end point
			while(backwards.hasNext()) {
				StaffDelta sd = backwards.next();
				
				// Terminate when we've done what was asked
				if(sd.LOCATION.compareTo(start) < 0)
					break;
				
				// Fill in information from the Key
				if(isFirst) {
					
					isFirst = false;
				// Fill in information based on preceding Chords and PitchSets (ps2, c2)
				} else {
					Chord c1 = sd.ESTABLISHED.CHORD;
					for(Voice.VoiceDelta vd : sd.VOICES) {
						
						if(vd.CHANGED.NOTES != null) {
							Enharmonics.fillEnharmonics(vd.CHANGED.NOTES, c1, ps2, c2);
							ps2 = vd.CHANGED.NOTES;
						}
					}
				}
			}
		}
		
		public void fillNoteNames(Rational start, Rational end) {
			//TODO use the Enharmonics class for this
		}
		
		public Voice newVoice() {
			Voice[] newVoices = new Voice[_voices.length+1];
			for(int i = 0; i < _voices.length; i++)
				newVoices[i] = _voices[i];
			Voice v = new Voice();
			newVoices[_voices.length] = v;
			_voices = newVoices;
			return v;
		}
	}
	
	public class ScoreDelta {
		public Rational LOCATION = null;
		public Rational BEATNUMBER = null;
		
		/**
		 * A simple class for storing pointers to things in a Score.
		 * @author Jon Latane
		 *
		 */
		public class Established {
			public TimeSignature TS = null;
		}
		public Established ESTABLISHED = new Established();
		
		/**
		 * A simple class for storing pointers to things in a Score.
		 * @author Jon Latane
		 *
		 */
		public class Changed {
			public TimeSignature TS = null;
		}
		public Changed CHANGED = new Changed();
		
		public Staff.StaffDelta[] STAVES;
	}
	
	public ScoreDelta scoreDeltaAt(Rational r) {
		ScoreDelta result = new ScoreDelta();
		result.LOCATION = r;
		result.BEATNUMBER = _meter.getBeatOf(r);
		
		result.ESTABLISHED.TS = _meter.getObjectAt(r);
		result.CHANGED.TS = _meter._data.get(r);
		
		result.STAVES = new Staff.StaffDelta[_staves.length];
		for(int i = 0; i < _staves.length; i++) {
			Staff s = _staves[i];
			result.STAVES[i] = s.staffDeltaAt(r);
		}
		
		return result;
	}
	
	private Iterator<ScoreDelta> scoreDeltaIterator(final Iterator<Rational> rhythm) {
		return new Iterator<ScoreDelta>() {
			//private boolean encounteredFine = false;
			@Override
			public boolean hasNext() {
				//if(encounteredFine) {
				//	return false;
				//} else {
				//	//boolean result = rhythm.hasNext();
				//	return true;
				//}
				return rhythm.hasNext();
			}

			@Override
			public ScoreDelta next() {
				//if(rhythm.hasNext()) {
					Rational r = rhythm.next();
				//	if(r.equals(getFine())) {
				//		encounteredFine = true;
				//	}
					return scoreDeltaAt(r);
				//} else if(!encounteredFine) {
				//	encounteredFine = true;
				//	return scoreDeltaAt(getFine());
				//} else {
				//	throw new NoSuchElementException();
				//}
			}

			@Override
			public void remove() { 
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	/**
	 * Return a ScoreDelta iterator that goes forewards from the supplied point
	 * 
	 * @param start the point to iterate  from
	 * @return
	 */
	public Iterator<ScoreDelta> scoreIterator(final Rational start) {
		return scoreDeltaIterator(getOverallRhythm().tailSet(start).iterator());
	}
	
	/**
	 * Return a ScoreDelta iterator that goes backwards from the supplied point
	 * 
	 * @param startEndSayWhat the point to iterate backwards from
	 * @return
	 */
	public Iterator<ScoreDelta> reverseScoreIterator(final Rational startEndSayWhat) {
		return reverseScoreIterator(startEndSayWhat, true);
	}
	
	/**
	 * Return a ScoreDelta iterator that goes backwards from the supplied point
	 * 
	 * @param startEndSayWhat the point to iterate backwards from
	 * @param inclusive whether to include that point
	 * @return
	 */
	public Iterator<ScoreDelta> reverseScoreIterator(final Rational startEndSayWhat, boolean inclusive) {
		return scoreDeltaIterator(getOverallRhythm().headSet(startEndSayWhat,inclusive).descendingIterator());
	}
	
	public Rational getBeatOf(Rational r) {
		return _meter.getBeatOf(r);
	}
	
	public NavigableSet<Rational> getOverallRhythm() {
		TreeSet<Rational> result = new TreeSet<Rational>();
		result.add(getFine());
		result.addAll(_meter.getRhythm());
		for(Staff s : _staves) {
			result.addAll(s._scales.getRhythm());
			result.addAll(s._chords.getRhythm());
			result.addAll(s._keys.getRhythm());
			result.addAll(s._clefs.getRhythm());
			for(Staff.Voice v : s._voices) {
				result.addAll(v._notes.getRhythm());
			}
		}
		return result;
	}
	
	/**
	 * Create a new Staff in this Score with its own harmonic information.
	 * For polytonal music like The Doors' "Alabama Song."  Otherwise this
	 * is not particularly useful.
	 * 
	 * @return the created Staff
	 */
	public Staff newIndependentStaff(Clef c) {
		Staff[] newStaves = new Staff[_staves.length+1];
		for(int i = 0; i < _staves.length; i++)
			newStaves[i] = _staves[i];
		Staff s = new Staff(c);
		newStaves[_staves.length] = s;
		_staves = newStaves;
		return s;
	}
	
	public Staff newIndependentStaff() {
		return newIndependentStaff(Clef.treble());
	}
	
	/**
	 * If this Score has no Staves, create a blank one.  Otherwise, create a new Staff
	 * with identical chords, keys and scales to the first one in the Score.
	 * 
	 * @return the created Staff
	 */
	public Staff newStaff() {
		return newStaff(Clef.treble());
	}
	
	public Staff newStaff(Clef c) {
		Staff result = newIndependentStaff(c);
		if(_staves.length > 1) {
			result._chords = _staves[0]._chords;
			result._keys = _staves[0]._keys;
			result._scales = _staves[0]._scales;
		}
		return result;
		
	}

	
	public Staff[] getStaves() {
		return _staves;
	}
	
	public Staff getStaff(int n) {
		return _staves[n];
	}
	
	public int getNumStaves() {
		return _staves.length;
	}
	
	public Staff removeStaff(int n) {
		Staff result = _staves[n];
		Staff[] newStaves = new Staff[_staves.length-1];
		int iNew = 0;
		for(int i = 0; i < _staves.length; i++) {
			if(i != n) {
				newStaves[iNew] = _staves[i];
				iNew++;
			}
		}
		return result;
	}
	
	public void swapStaves(int a, int b) {
		Staff staffA = _staves[a];
		_staves[a] = _staves[b];
		_staves[b] = staffA;
	}
	
	public void stripEnharmonics() {
		for(Staff staff : getStaves()) {
			for(Chord c : staff._chords._data.values()) {
				c.NOTENAMES = null;
			}
			for(Voice v : staff.getVoices()) {
				for(PitchSet ps : v._notes._data.values()) {
					ps.NOTENAMES = null;
				}
			}
		}
	}
	
	public static Score twinkleTwinkle() {
		Score result = new Score(new TimeSignature(2, 4), 8);
		
		Staff s1 = result.newStaff();
		Staff s2 = result.newStaff();
		
		//s1._keys.put(new Rational(1,1), new Key(Key.CMajor));
		
		Staff.Voice melody = s1.newVoice();
		Staff.Voice harmony = s2.newVoice();
		
		melody._notes.put(new Rational(1,1), PitchSet.toPitchSet("C4"));
		melody._notes.put(new Rational(2,1), PitchSet.toPitchSet("C4"));
		melody._notes.put(new Rational(3,1), PitchSet.toPitchSet("G4"));
		melody._notes.put(new Rational(4,1), PitchSet.toPitchSet("G4"));
		melody._notes.put(new Rational(5,1), PitchSet.toPitchSet("A4"));
		melody._notes.put(new Rational(6,1), PitchSet.toPitchSet("A4"));
		melody._notes.put(new Rational(7,1), PitchSet.toPitchSet("G4"));
		melody._notes.put(new Rational(9,1), PitchSet.toPitchSet("F4"));
		melody._notes.put(new Rational(10,1), PitchSet.toPitchSet("F4"));
		melody._notes.put(new Rational(11,1), PitchSet.toPitchSet("E4"));
		melody._notes.put(new Rational(12,1), PitchSet.toPitchSet("E4"));
		melody._notes.put(new Rational(13,1), PitchSet.toPitchSet("D4"));
		melody._notes.put(new Rational(14,1), PitchSet.toPitchSet("D4"));
		melody._notes.put(new Rational(15,1), PitchSet.toPitchSet("C4"));
		
		harmony._notes.put(new Rational(1,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		harmony._notes.put(new Rational(5,1), PitchSet.toPitchSet(new String[]{"C3", "F3", "A3"}));
		harmony._notes.put(new Rational(7,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		harmony._notes.put(new Rational(9,1), PitchSet.toPitchSet(new String[]{"C3", "F3", "A3"}));
		harmony._notes.put(new Rational(11,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		harmony._notes.put(new Rational(13,1), PitchSet.toPitchSet(new String[]{"B2", "F3", "G3"}));
		harmony._notes.put(new Rational(15,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		
		return result;
	}
	
	public static void testTwinkleTwinkle() {
		testScore(twinkleTwinkle());
	}
	
	/**
	 * Run a gamut of tests to make sure the given Score is consistent.
	 * 
	 * @param s
	 */
	public static void testScore(Score s) {
		Iterator<ScoreDelta> itr = s.scoreIterator(Rational.ZERO);
		
		// Iterate through the Score to read its contents
		while(itr.hasNext()) {
			ScoreDelta scd = itr.next();
			assert(scd.STAVES.length == s.getNumStaves());
			
			Rational r = scd.LOCATION;
			Rational beat = scd.BEATNUMBER;
			
			String[] staffRepr = new String[s.getNumStaves()];
			
			for(int i = 0; i < scd.STAVES.length; i++) { //Staff.StaffDelta sd : scd.STAVES ) {
				Staff.StaffDelta sd = scd.STAVES[i];
				assert(r.compareTo(sd.LOCATION) == 0);
				assert(sd.VOICES.length == s.getStaff(i).getNumVoices());
				
				if( sd.CHANGED.CHORD != null ) {
					assert(sd.CHANGED.CHORD.equals(Chord.NO_CHORD) || sd.CHANGED.CHORD.getRoot() != null);
					if( sd.CHANGED.CHORD.equals(Chord.NO_CHORD) ) {
						staffRepr[i] = "N.C.";
					} else {
						staffRepr[i] = "[" + sd.CHANGED.CHORD.getRoot() + "]" + sd.CHANGED.CHORD.getCharacteristic();
					}
				} else {
					staffRepr[i] = "_";
				}
				
				//Add the Voice contents to the staffRepr
				String[] voiceRepr = new String[sd.VOICES.length];
				for(int j = 0; j < sd.VOICES.length; j++) { //Staff.Voice.VoiceDelta vd : sd.VOICES ) {
					Staff.Voice.VoiceDelta vd = sd.VOICES[j];
					if(vd.CHANGED.NOTES != null) {
						voiceRepr[j] = " ";
						for(int n : vd.CHANGED.NOTES) {
							voiceRepr[j] += n + " ";
						}
						if(vd.CHANGED.NOTES.equals(PitchSet.REST)) {
							voiceRepr[j] += "- ";
						}
					} else {
						if(vd.ESTABLISHED.NOTES != null && !vd.ESTABLISHED.NOTES.equals(PitchSet.REST)) {
							voiceRepr[j] = "_";
						} else {
							voiceRepr[j] = "-";
						}
					}
				}
				for(String str : voiceRepr) {
					staffRepr[i] += "|" + str;
				}
				staffRepr[i] += "|";
			}
			
			// Our String representation of the "line of music" (i.e, a vertical line through
			// the Score as on a page) to be spat out
			String lineRepr = r.toMixedString() + ": ";
			
			// Staff and voice information
			for(int i = 0; i < staffRepr.length; i++ ) {
				lineRepr += "|| " + staffRepr[i] + " ";
			}
			lineRepr += "||";
			
			Log.d(TAG, lineRepr);
		}
	}
}
