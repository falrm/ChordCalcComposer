package com.jonlatane.composer.music;

import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.harmony.PitchSet;

import junit.framework.TestCase;

public class ScoreTest extends TestCase {

	public ScoreTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testOdeToJoy() {
		Voice v = new Voice();
		Staff s = new Staff(v);
		Score result = new Score();
		result.addStaff(s);
		
		RhythmMap<PitchSet> realization = v.getRealization();
		realization.put(new Rational(1,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(2,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(3,1), Key.noteNameToPitchSet("F"));
		realization.put(new Rational(4,1), Key.noteNameToPitchSet("G"));
		realization.put(new Rational(5,1), Key.noteNameToPitchSet("G"));
		realization.put(new Rational(6,1), Key.noteNameToPitchSet("F"));
		realization.put(new Rational(7,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(8,1), Key.noteNameToPitchSet("D"));
		realization.put(new Rational(9,1), Key.noteNameToPitchSet("C"));
		realization.put(new Rational(10,1), Key.noteNameToPitchSet("C"));
		realization.put(new Rational(11,1), Key.noteNameToPitchSet("D"));
		realization.put(new Rational(12,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(13,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(29,2), Key.noteNameToPitchSet("D"));
		realization.put(new Rational(15,1), Key.noteNameToPitchSet("D"));
		
		
	}

}
