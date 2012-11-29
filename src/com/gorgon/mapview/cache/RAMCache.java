package com.gorgon.mapview.cache;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.gorgon.mapview.Tile;
import com.gorgon.mapview.utils.Helper;
import com.gorgon.mapview.utils.Preferences;

import android.graphics.Bitmap;

public class RAMCache {

	public RAMCache() {
		// Filling our bitmap list with bitmaps to reuse them in future
		for (int i = 0; i < Preferences.RAM_CACHE_MAX_RECORDS; ++i)
			mBitmapList.add(Bitmap.createBitmap(Tile.SIZE, Tile.SIZE,
					Bitmap.Config.RGB_565));
	}

	public boolean contains(Tile tile) {
		return mCache.containsKey(tile);
	}

	public Bitmap getBitmap(Tile tile) {
		return mCache.get(tile);
	}

	public void addTile(Tile tile, Bitmap bmp) {
		if (mCache.containsKey(tile)) {
			Helper.LOGE("RAMCache::addTile() - the tile is already in cache: "
					+ tile.getX() + "x" + tile.getY());
			return;
		}
		// Copying pixels from the bitmap to temporary buffer
        bmp.getPixels(mTmpPixels, 0, Tile.SIZE, 0, 0, Tile.SIZE, Tile.SIZE);
		// Get the latest reusable bitmap from mBitmapList
		Bitmap reusedBitmap = mBitmapList.remove();
		// Set pixels to this bitmap
		reusedBitmap.setPixels(mTmpPixels, 0, Tile.SIZE, 0, 0, Tile.SIZE, Tile.SIZE);
		// Move this reusable bitmap to our cache
		mCache.put(tile, reusedBitmap);
        
		Helper.LOGD("RAMCache::addTile(): " + tile + " " + mCache.size() + "; "
				+ mBitmapList.size());
	}

	public void destroy() {
		for (Bitmap bitmap : mCache.values())
            bitmap.recycle();		

		mBitmapList.clear();		
	}
	// ///////////////////////// PRIVATE SECTION //////////////////////////////
	// Reusable int[] buffer for copying bitmaps
	private int[] mTmpPixels = new int[Tile.SIZE * Tile.SIZE];
	// List of free bitmaps ready to be moved to mCache
	private LinkedList<Bitmap> mBitmapList = new LinkedList<Bitmap>();
	/**
	 * The HashMap where our cache is stored
	 * 
	 * @NOTE We move the eldest bitmap from mCache to mBitmapList to avoid using
	 *       extra memory
	 */
	private LinkedHashMap<Tile, Bitmap> mCache = new LinkedHashMap<Tile, Bitmap>(
			(int) (Preferences.RAM_CACHE_MAX_RECORDS / Preferences.RAM_CACHE_LOAD_FACTOR) + 2,
			Preferences.RAM_CACHE_LOAD_FACTOR, true) {
		// A member of serializable class
		private static final long serialVersionUID = 777;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Tile, Bitmap> eldest) {

			if (size() >= Preferences.RAM_CACHE_MAX_RECORDS) {
				mBitmapList.add(eldest.getValue());

				Helper.LOGD("RAMCache::removeEldestEntry(): " + eldest.getKey() + " " + size() + "; "
						+ mBitmapList.size());
				return true; // Remove this element from our LinkedHashMap
			}
			return false;
		}
	};

}
