package com.gorgon.mapview.utils;

public class Preferences {
	/**
	 * Turns on/off Java debug logs
	 */
	public static boolean DEBUG_MODE = false;
	/**
	 * The http url of vector tiles
	 */
	public static final String TILES_URL = "http://vec.maps.yandex.net/tiles?l=map&v=2.21.0&x=%1$s&y=%2$s&z=10";
	/**
	 * Tiles indexes to start from
	 */
	public static final int CENTER_TILE_X = 617;
	public static final int CENTER_TILE_Y = 319;
	/**
	 * Maximum/2 scroll distance from start tiles
	 *    to have 100x100 field
	 */
	public static final int MAX_TILES = 50;

	///////////////////////// Memory optimizations //////////////////////
	/**
	 * Maximum size of RAM cache LinkedHashMap
	 */
	public static final int RAM_CACHE_MAX_RECORDS = 40;
	/**
	 * RAM cache's LinkedHashMap loading factor
	 */
	public static final float RAM_CACHE_LOAD_FACTOR = 0.7f;
	/**
	 * Maximum size of list which prevents AsyncTileResolver
	 *        from requesting the same tile several times
	 *        
	 *        Be carefull!! If this value is not much less then
	 *         RAM_CACHE_MAX_RECORDS - some tiles could be missed on the screen,
	 *          because they could still present inside last requested tiles list,
	 *           but will be already removed from the RAM cache 
	 */
	public static final int MAX_REQUESTED_TILES_LIST_SIZE = 20;
}
