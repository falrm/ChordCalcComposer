package com.jonlatane.composer.music;

public interface HarmonicSpace<K extends Chord>
{
	public K[] findPath( K start, K end );
}
