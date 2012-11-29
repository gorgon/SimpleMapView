package com.gorgon.mapview;

import com.gorgon.mapview.utils.Preferences;

public class Tile implements Comparable<Tile> {
	/**
	 * We work with 256x256 tiles
	 */
	public final static int SIZE = 256;
	
	public final static int TILE_SIZE = 256;
	public final static int TILE_SIZE_IN_BYTES = 256 * 256 * 4;
	
	public Tile(int x, int y) {
		this.X = x;
		this.Y = y;
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        
        Tile rhs = (Tile) obj;
        
        return (rhs.X == this.X && rhs.Y == this.Y);
	}	
	
	@Override
	public int hashCode() {
		return (this.X * Preferences.MAX_TILES + this.Y);		
	}
	
	public int getX() {
		return X;
	}
	
	public int getY() {
		return Y;
	}
	
	@Override
	public String toString() {		
		return new String(X + "x" + Y);
	}

	public int compareTo(Tile another) {
		if(this.X == another.X && this.Y == another.Y)
			return 0;
		else if(this.X > another.X || this.Y > another.Y)
			return 1;
		else
			return -1;
	}


	// ///////////////////////// PRIVATE SECTION //////////////////////////////
	/**
	 * Tile X index (absolute index from the url with center at CENTER_TILE_X)
	 *     (this value should be never changed because hashCode is based on it)
	 */
	private int X;
	/**
	 * Tile Y index (absolute index from the url with center at CENTER_TILE_Y)
	 *     (this value should be never changed because hashCode is based on it)
	 */
	private int Y;
}
