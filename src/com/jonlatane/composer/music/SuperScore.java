package com.jonlatane.composer.music;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import android.util.Pair;

import com.jonlatane.composer.music.coverings.Clef;
import com.jonlatane.composer.music.harmony.*;

/**
 * This is a fully self-managing Score object.  A Score consists of Staves, and a Staff
 * consists of Voices.
 * @author Jon
 *
 */
public class SuperScore {
	private Meter _meter;
	private Staff[] _staves = new Staff[0];
	
	public class Staff {
		private RhythmMap<Chord> _chords;
		private RhythmMap<Key> _keys;
		private RhythmMap<Clef> _clefs;
		public int TRANSPOSITION = 0;
		public String TITLE = "";
		private Voice[] _voices = new Voice[0];
		
		public class Voice {
			private RhythmMap<Scale> _scales;
			private RhythmMap<PitchSet> _realization;
		}
		
		public Voice[] getVoices() {
			return _voices;
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
	
	public NavigableSet<Rational> getOverallRhythm() {
		TreeSet<Rational> result = new TreeSet<Rational>();
		result.addAll(_meter.getRhythm());
		for(Staff s : _staves) {
			result.addAll(s._chords.getRhythm());
			for(Staff.Voice v : s._voices) {
				result.addAll(v._scales.getRhythm());
				result.addAll(v._realization.getRhythm());
			}
		}
		return result;
	}
	
	public Staff[] getStaves() {
		return _staves;
	}
	
	public Staff newStaff() {
		Staff[] newStaves = new Staff[_staves.length+1];
		for(int i = 0; i < _staves.length; i++)
			newStaves[i] = _staves[i];
		Staff s = new Staff();
		newStaves[_staves.length] = s;
		_staves = newStaves;
		return s;
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

	/**
	 * The StaffDelta is the most useful means of reading through a Score.  A StaffDelta is
	 * essentially designed to encapsulate everything we need to know to draw a specific rhythmic
	 * moment (i.e., a Rational number) to the screen.  It uses null values not to signify lack of
	 * something, but lack of a *change* in something.  The Boolean StaffDelta.NoteHead.TIED is to be used
	 * to tie notes, so a note represented as a length of 5/4 (i.e., quarter tied to a sixteenth note) may
	 * be split into the two separate rhythmic locations that must be drawn to do this.  How this split will
	 * happen is up to the Iterator<StaffDelta[]> which is available below.
	 * 
	 * @author Jon
	 */
	public static class StaffDelta {
		public Rational LOCATION = null;
		public Meter METER = null;
		public Key KEY = null;
		public Integer TRANSPOSITION = null;
		public Scale SCALE = null;
		
		public static class NoteHead {
			PitchSet NOTES;
			String[] NOTENAMES;
			Rational LENGTH;
			Boolean TIED;
		}
		public Map<Voice,NoteHead> NOTES = null;
	}
	
	public Iterator<StaffDelta[]> scoreIterator() {
		return new Iterator<StaffDelta[]>() {

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public StaffDelta[] next() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
}
