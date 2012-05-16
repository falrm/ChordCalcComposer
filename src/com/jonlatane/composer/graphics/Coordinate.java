package com.jonlatane.composer.graphics;

public class Coordinate
{
	public int x;
	public int y;
	public Coordinate(int x, int y) {
		this.x = x; this.y = y;
	}
	@Override
	public int hashCode() {
		return x ^ y;
	}
	@Override
	public boolean equals(Object o) {
		if( o == null) return false;
		if(!(o instanceof Coordinate)) return false;
		return ((Coordinate)o).x == x &&
				((Coordinate)o).y == y;
	}
}
