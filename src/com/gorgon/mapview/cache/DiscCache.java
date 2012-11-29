package com.gorgon.mapview.cache;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.os.Environment;

import com.gorgon.mapview.Tile;
import com.gorgon.mapview.utils.Helper;

public class DiscCache {

	public DiscCache() {
		setStoragePath(Environment.getExternalStorageDirectory().getPath());
	}

	public boolean contains(Tile tile) {
		return contains(tile.getX(), tile.getY());
	}

	public void addTile(Tile tile, Bitmap bmp) {
		if (bmp == null) {
			Helper.LOGE("addTile() received empty tile!");
			return;
		}

		bmp.getPixels(mTmpPixels1, 0, Tile.SIZE, 0, 0, Tile.SIZE, Tile.SIZE);
		save(tile.getX(), tile.getY(), intToByteArray(mTmpPixels1));
	}

	public Bitmap getBitmap(Tile tile) {
		byte[] readBytes = read(tile.getX(), tile.getY());
		
		if(readBytes == null) {
			Helper.LOGE("DiscCache::getBitmap() got null byte array");
			return null;
		}

		mTmpBitmap.setPixels(byteToIntArray(readBytes), 0, Tile.SIZE, 0, 0,
				Tile.SIZE, Tile.SIZE);
		return mTmpBitmap;
	}

	public void destroy() {
		if (mTmpBitmap != null) {
			mTmpBitmap.recycle();
		}
	}

	static {
		System.loadLibrary("disccache-jni");
	}

	// ///////////////////////// PRIVATE SECTION //////////////////////////////
	private final static int FILE_LENGTH = Tile.SIZE * Tile.SIZE;

	// Temporary buffers for preventing OutOfMemoryException
	private ByteBuffer mTmpByteBuffer = ByteBuffer.allocate(Tile.SIZE
			* Tile.SIZE * 4);
	private int[] mTmpPixels1 = new int[FILE_LENGTH];
	private int[] mTmpPixels2 = new int[FILE_LENGTH];
	private Bitmap mTmpBitmap = Bitmap.createBitmap(Tile.SIZE, Tile.SIZE,
			Bitmap.Config.RGB_565);

	public native void setStoragePath(String path);

	public native boolean contains(int x, int y);

	public native void save(int x, int y, byte[] bytes);

	public native byte[] read(int x, int y);

	/**
	 * Converting from integer to byte array
	 * 
	 * @param intArray
	 */
	public byte[] intToByteArray(int[] intArray) {
		IntBuffer intBuffer = mTmpByteBuffer.asIntBuffer();
		intBuffer.put(intArray);
		byte[] byteArray = mTmpByteBuffer.array();
		return byteArray;
	}

	/**
	 * Converting from byte to integer array
	 * 
	 * @param byteArray
	 */
	public int[] byteToIntArray(byte[] byteArray) {
		ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
		IntBuffer intBuf = byteBuf.asIntBuffer();
		intBuf.get(mTmpPixels2);
		return mTmpPixels2;
	}
}
