package com.jonlatane.composer.music;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.util.SparseArray;
import android.util.SparseIntArray;

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
	private Meter _meter;
	private Staff[] _staves = new Staff[0];
	
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
		private RhythmMap<Chord> _chords;
		private RhythmMap<Key> _keys;
		private RhythmMap<Clef> _clefs;
		private RhythmMap<Scale> _scales;
		public int TRANSPOSITION = 0;
		public String TITLE = "";
		private Voice[] _voices = new Voice[0];
		
		/**
		 * As stated in @Staff, a Voice only contains a PitchSet (its NOTES), Articulation and Dynamics, while 
		 * information such as the given Chord, Key and Scale is held to be constant within a single Staff.
		 * 
		 * While a Voice does have VoiceDeltas and voiceDeltaAt(r) method, it is not iterable.  Iterating through
		 * a Voice alone is not particularly useful to us and is identical to iterating through a single-voiced
		 * @Staff.
		 * 
		 * @author Jon Latane
		 *
		 */
		public class Voice {
			private RhythmMap<PitchSet> _realization;
			public class VoiceDelta {
				public Rational LOCATION = null;
				public Rational BEATNUMBER = null;
				/**
				 * A simple class for storing pointers to things in a Voice.
				 * @author Jon Latane
				 *
				 */
				public class Established {
					public PitchSet NOTES = null;
				}
				public Established ESTABLISHED = new Established();
				
				/**
				 * A simple class for storing pointers to things in a Voice.
				 * @author Jon Latane
				 *
				 */
				public class Changed {
					public PitchSet NOTES = null;
				}
				public Changed CHANGED = new Changed();
			}
			
			public VoiceDelta voiceDeltaAt(Rational r) {
				VoiceDelta result = new VoiceDelta();
				result.LOCATION = r;
				result.BEATNUMBER = _meter.getBeatOf(r);
				
				result.ESTABLISHED.NOTES = _realization.getObjectAt(r);
				
				result.CHANGED.NOTES = _realization._data.get(r);
				
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
			public Rational BEATNUMBER = null;
			
			/**
			 * A simple class for storing pointers to things in a Staff.
			 * @author Jon Latane
			 *
			 */
			public class Established {
				public TimeSignature TS = null;
				public Key KEY = null;
				public Chord CHORD = null;
				public Scale SCALE = null;
			}
			
			public Established ESTABLISHED = new Established();
			
			/**
			 * A simple class for storing pointers to things in a Staff.
			 * @author Jon Latane
			 *
			 */
			public class Changed {
				public TimeSignature TS = null;
				public Key KEY = null;
				public Chord CHORD = null;
				public Scale SCALE = null;
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
			result.BEATNUMBER = _meter.getBeatOf(r);
			
			result.ESTABLISHED.TS = _meter.getObjectAt(r);
			result.ESTABLISHED.KEY = _keys.getObjectAt(r);
			result.ESTABLISHED.CHORD = _chords.getObjectAt(r);
			result.ESTABLISHED.SCALE = _scales.getObjectAt(r);
			
			result.CHANGED.TS = _meter._data.get(r);
			result.CHANGED.KEY = _keys._data.get(r);
			result.CHANGED.CHORD = _chords._data.get(r);
			result.CHANGED.SCALE = _scales._data.get(r);
			
			result.VOICES = new Voice.VoiceDelta[_voices.length];
			for(int i = 0; i < _voices.length; i++) {
				Voice v = _voices[i];
				Voice.VoiceDelta vd = v.new VoiceDelta();
				result.VOICES[i] = v.voiceDeltaAt(r);
			}
			
			//TODO finish this guys up
			return result;
		}

		public Voice[] getVoices() {
			return _voices;
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
			//
			while(backwards.hasNext()) {
				StaffDelta d = backwards.next();
			}
		}
		
		public void fillNoteNames(Rational start, Rational end) {
			
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
		
		return result;
	}
	
	/**
	 * Returns a Map that interfaces with the RhythmMap<Chord> of each staff.  Getting returns the first Staff's
	 * Chord at that point, while putting puts the object in all staves.
	 * @return
	 */
	public Map<Rational,Chord> getMonotonalChords() {
		return new Map<Rational, Chord>() {

			@Override
			public void clear() {
				for(Staff s: _staves)
					s._chords.clear();
			}

			@Override
			public boolean containsKey(Object key) {
				for(Staff s: _staves) {
					if(s._chords.getRhythm().contains(key))
						return true;
				}
				return false;
			}

			@Override
			public boolean containsValue(Object value) {
				for(Staff s: _staves) {
					if(s._chords.values().contains(value))
						return true;
				}
				return false;
			}

			@Override
			public Set<java.util.Map.Entry<Rational, Chord>> entrySet() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Chord get(Object key) {
				for(Staff s: _staves) {
					if(s._chords.getRhythm().contains(key))
						return s._chords.getObjectAt((Rational)key);
				}
				return null;
			}

			@Override
			public boolean isEmpty() {
				boolean result = true;
				for(Staff s: _staves) {
					if(!s._chords.isEmpty()) {
						result = false;
						break;
					}
				}
				return result;
			}

			@Override
			public Set<Rational> keySet() {
				Set<Rational> result = new HashSet<Rational>();
				for(Staff s : _staves) {
					result.addAll(s._chords.getRhythm());
				}
				return result;
			}

			@Override
			public Chord put(Rational key, Chord value) {
				Chord result = null;
				for(Staff s: _staves) {
					if (result == null)
						result = s._chords.put(key, value);
					else
						s._chords.put(key, value);
				}
				return null;
				
			}

			@Override
			public void putAll(Map<? extends Rational, ? extends Chord> arg0) {
				for(Staff s: _staves) {
						s._chords.putAll(arg0);
				}
			}

			@Override
			public Chord remove(Object key) {
				Chord result = null;
				if(!(key instanceof Rational))
					return null;
				Rational k = (Rational)key;
				for(Staff s: _staves) {
					if (result == null)
						result = s._chords.remove(k);
					else
						s._chords.remove(k);
				}
				return null;
			}

			@Override
			public int size() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Collection<Chord> values() {
				HashSet<Chord> result = new HashSet<Chord>();
				for(Staff s: _staves) {
					result.addAll(s._chords.values());
				}
				return result;
			}
			
		};
	}
	
	public NavigableSet<Rational> getOverallRhythm() {
		TreeSet<Rational> result = new TreeSet<Rational>();
		result.addAll(_meter.getRhythm());
		for(Staff s : _staves) {
			result.addAll(s._scales.getRhythm());
			result.addAll(s._chords.getRhythm());
			result.addAll(s._keys.getRhythm());
			result.addAll(s._clefs.getRhythm());
			for(Staff.Voice v : s._voices) {
				result.addAll(v._realization.getRhythm());
			}
		}
		return result;
	}
	
	public Staff[] getStaves() {
		return _staves;
	}
	
	/**
	 * Create a new Staff in this Score with its own harmonic information
	 * 
	 * @return the created Staff
	 */
	public Staff newIndependentStaff() {
		Staff[] newStaves = new Staff[_staves.length+1];
		for(int i = 0; i < _staves.length; i++)
			newStaves[i] = _staves[i];
		Staff s = new Staff();
		newStaves[_staves.length] = s;
		_staves = newStaves;
		return s;
	}
	
	/**
	 * If this Score has no Staves, create a blank one.  Otherwise, create a new staff with identical chords, keys
	 * and scales to the first one in the Score.
	 * 
	 * @return the created Staff
	 */
	public Staff newStaff() {
		Staff result = newIndependentStaff();
		if(_staves.length > 1) {
			result._chords = _staves[0]._chords;
			result._keys = _staves[0]._keys;
			result._scales = _staves[0]._scales;
		}
		return result;
	}
	
	public Staff getStaff(int n) {
		return _staves[n];
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
	
	public static Score twinkleTwinkle() {
		Score result = new Score();
		
		Staff s1 = result.newStaff();
		Staff s2 = result.newStaff();
		
		Staff.Voice melody = s1.newVoice();
		Staff.Voice harmony = s2.newVoice();
		
		melody._realization.put(new Rational(1,1), PitchSet.toPitchSet("C4"));
		melody._realization.put(new Rational(2,1), PitchSet.toPitchSet("C4"));
		melody._realization.put(new Rational(3,1), PitchSet.toPitchSet("G4"));
		melody._realization.put(new Rational(4,1), PitchSet.toPitchSet("G4"));
		melody._realization.put(new Rational(5,1), PitchSet.toPitchSet("A4"));
		melody._realization.put(new Rational(6,1), PitchSet.toPitchSet("A4"));
		melody._realization.put(new Rational(7,1), PitchSet.toPitchSet("G4"));
		melody._realization.put(new Rational(9,1), PitchSet.toPitchSet("F4"));
		melody._realization.put(new Rational(10,1), PitchSet.toPitchSet("F4"));
		melody._realization.put(new Rational(11,1), PitchSet.toPitchSet("E4"));
		melody._realization.put(new Rational(12,1), PitchSet.toPitchSet("E4"));
		melody._realization.put(new Rational(13,1), PitchSet.toPitchSet("D4"));
		melody._realization.put(new Rational(14,1), PitchSet.toPitchSet("D4"));
		melody._realization.put(new Rational(15,1), PitchSet.toPitchSet("C4"));
		melody._realization.put(new Rational(17,1), null);
		
		harmony._realization.put(new Rational(1,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		harmony._realization.put(new Rational(5,1), PitchSet.toPitchSet(new String[]{"C3", "F3", "A3"}));
		harmony._realization.put(new Rational(7,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		harmony._realization.put(new Rational(9,1), PitchSet.toPitchSet(new String[]{"C3", "F3", "A3"}));
		harmony._realization.put(new Rational(11,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		harmony._realization.put(new Rational(13,1), PitchSet.toPitchSet(new String[]{"B3", "F3", "G3"}));
		harmony._realization.put(new Rational(15,1), PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"}));
		melody._realization.put(new Rational(17,1), null);
		
		return result;
	}
}
