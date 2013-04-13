package com.jonlatane.composer.music.harmony;

public class Resolver {
	private final int _rootNameMotion;
	private final int[] _motion;
	
	private static Resolver CircleResolver = new Resolver(-5, 0, +1, +2);
	
	/**
	 * Creates a Resolver that resolves chords using the given half-step motion of
	 * each chord degree.
	 * 
	 * @param motion up to 7 modifications, in root, 3, 5, 7, 9, 11, 13 order
	 */
	private Resolver(int rootNameMotion, int... motion) {
		_rootNameMotion = rootNameMotion;
		_motion = motion;
	}
	
	public int[] resolutionOf(Chord first, Chord second) {
		int[] result = new int[7];
		
		return result;
	}
	
	public Chord resolve(Chord c) {
		Chord result = new Chord();
		if(c.getRoot() != null) {
			result.add(c.getRoot() + _motion[0]);
			result.setRoot(c.getRoot() + _motion[0]);
		}
		for(int i : c) {
			
			//result.add(i+_motion[i])
		}
		
		return result;
	}

}
