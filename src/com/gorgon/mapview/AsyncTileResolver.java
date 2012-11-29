package com.gorgon.mapview;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.PriorityQueue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.gorgon.mapview.cache.DiscCache;
import com.gorgon.mapview.utils.Helper;
import com.gorgon.mapview.utils.LimitedLinkedList;
import com.gorgon.mapview.utils.Preferences;

public class AsyncTileResolver {
	/**
	 * Async tile request finished listener
	 * 
	 */
	public static interface BackgroundResolverListener {
		void onAsyncTileRequestFinished(Tile tile, Bitmap bmp);
	}

	public void setListener(BackgroundResolverListener listener) {
		mListener = listener;
	}

	public AsyncTileResolver(SimpleMapView view, DiscCache discCache) {
		mDiscCache = discCache;
		// Starting background thread to handle tiles asynchronously
		new Thread(myBkRunnable).start();
	}

	public synchronized void scheduleJobs() {
		tmpTilesQueue = mCurrentTilesQueue;
		mCurrentTilesQueue = mNextTilesQueue;
		mNextTilesQueue = tmpTilesQueue;
		mNextTilesQueue.clear();
		this.notify();
	}
	
	public synchronized void requestTile(Tile tile) {
		if(!mNextTilesQueue.contains(tile))
			mNextTilesQueue.add(tile);
		Helper.LOGD("AsyncTileResolver::requestTile() - added tile " + tile + ", current size: " + mNextTilesQueue.size());
	}

	public synchronized void finish() {
		if (mActive) {
			mActive = false;
			mCurrentTilesQueue.clear();
			mNextTilesQueue.clear();
			this.notify();
		}
	}

	// ///////////////////////// PRIVATE SECTION //////////////////////////////
	private DiscCache mDiscCache;
	private BackgroundResolverListener mListener;
	private volatile boolean mActive = true;
	// Tiles queue to work with
	private PriorityQueue<Tile> mCurrentTilesQueue = new PriorityQueue<Tile>(64);
	// Tiles queue to be handled after the next scheduleJobs() call
	private PriorityQueue<Tile> mNextTilesQueue = new PriorityQueue<Tile>(64);
	private PriorityQueue<Tile> tmpTilesQueue;

	private Runnable myBkRunnable = new Runnable() {
		@Override
		public synchronized void run() {

			while (mActive) {
					Tile tile = null;

					if (mCurrentTilesQueue.size() < 1) {
						try {
							this.wait(100); // check the list later, e.g. if Internet connection was restored
						} catch (InterruptedException e) {
							Helper.LOGE("Waiting for tiles was interrupted");
							e.printStackTrace();
						}
					} else
						tile = mCurrentTilesQueue.peek(); // Getting last tile to handle

					if (tile != null) {
						Helper.LOGD("Handling tile: " + tile);
						mListener.onAsyncTileRequestFinished(tile,
								receiveTile(tile));
					}
				}

			
			mCurrentTilesQueue.clear();
			mNextTilesQueue.clear();
		}
		
		private Bitmap receiveTile(Tile requestedTile) {
			Helper.LOGD("Requesting tile: " + requestedTile);
			Bitmap receivedBitmap = null;

			if (mDiscCache.contains(requestedTile)) {
				Helper.LOGD("Found tile inside DiscCache");

				receivedBitmap = mDiscCache.getBitmap(requestedTile);
			} else {
				receivedBitmap = receiveTileByHttp(requestedTile);

				if (receivedBitmap != null)
					mDiscCache.addTile(requestedTile, receivedBitmap);
			}

			if (receivedBitmap != null) {
				synchronized(this) {
					 // Tile was handled correctly - doesn't need one more try
					//   we need synchronization to avoid mCurrentTilesQueue
					//       modifications at the same time by the main thread 
					mCurrentTilesQueue.remove(requestedTile);
				}
				Helper.LOGD("Tile successfully received: " + requestedTile);
			} else
				Helper.LOGE("AsyncTileResolver::Tile not handled correctly - needs one more try: " + requestedTile);
				

			return receivedBitmap;
		}
				
		private Bitmap receiveTileByHttp(Tile tile) {
			Bitmap bmp = null;
			URLConnection connection = null;

			URL url = getTileUrl(tile);

			try {
				connection = url.openConnection();
			} catch (IOException e) {
				Helper.LOGEx("AsyncTileResolver::IOException", e);
				return null;
			}

			if (connection == null) {
				Helper.LOGE("AsyncTileResolver::Cannot connect!");
			}

			HttpURLConnection HCon = (HttpURLConnection) connection;
			int ResCode = 0;

			try {
				ResCode = HCon.getResponseCode();
			} catch (IOException e1) {
				Helper.LOGEx("AsyncTileResolver::IOException", e1);
				return null;
			}

			Helper.LOGD("Responce Code is = " + ResCode);

			if (ResCode == HttpURLConnection.HTTP_OK) {

				try {
					InputStream ins = ((URLConnection) HCon).getInputStream();
					bmp = BitmapFactory.decodeStream(ins);
				} catch (IOException e) {
					Helper.LOGEx("AsyncTileResolver::IOException", e);
					return null;
				} catch (OutOfMemoryError error) {
					Helper.LOGE("AsyncTileResolver::OutOfMemory - trying to start GC");
					System.gc();
					return null;
				}
			}

			return bmp;
		}

		private URL getTileUrl(Tile tile) {
			String url_text = String.format(Preferences.TILES_URL, tile.getX(),
					tile.getY());
			Helper.LOGD("HTTP request to: " + url_text);
			URL resultUrl = null;

			try {
				resultUrl = new URL(url_text);
			} catch (MalformedURLException e) {
				Helper.LOGEx("AsyncTileResolver::Strange exception", e);
			}

			return resultUrl;
		}
	};
}
