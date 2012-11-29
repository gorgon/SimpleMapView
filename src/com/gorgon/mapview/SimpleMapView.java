package com.gorgon.mapview;

import com.gorgon.mapview.AsyncTileResolver.BackgroundResolverListener;
import com.gorgon.mapview.cache.DiscCache;
import com.gorgon.mapview.cache.RAMCache;
import com.gorgon.mapview.utils.Helper;
import com.gorgon.mapview.utils.Preferences;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class SimpleMapView extends ViewGroup {
	
	public SimpleMapView(Context context) {
		super(context);
		init();
	}

	public SimpleMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SimpleMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Helper.LOGD("onLayout(): " + l + " x " + r + " " + t + " x " + b);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		Helper.LOGD("onSizeChanged(): " + w + " x " + h);

		if (mMapViewBitmap != null) {
			mMapViewBitmap.recycle();
		}

		// check if the new size is positive
		if (w > 0 && h > 0) {
			mMapViewBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Bitmap.Config.RGB_565);
			mMapViewCanvas = new Canvas(mMapViewBitmap);
			updateTiles();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mMapViewBitmap, 0, 0, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Helper.LOGD("onTouchEvent()");

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Update previous touch positions
			mPrevPosX = event.getX();
			mPrevPosY = event.getY();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			int mTmpUpperLeftX = (int) (mPrevPosX - event.getX() + mUpperLeftX);
			int mTmpUpperLeftY = (int) (mPrevPosY - event.getY() + mUpperLeftY);

			// Don't scroll more far then MAX_TILES from CENTER
			if (Math.abs(getTileIndexX(mTmpUpperLeftX)
					- Preferences.CENTER_TILE_X) >= Preferences.MAX_TILES
					|| Math.abs(getTileIndexY(mTmpUpperLeftY)
							- Preferences.CENTER_TILE_Y) >= Preferences.MAX_TILES)
				return true;

			// Calculate screen move
			mUpperLeftX = mTmpUpperLeftX;
			mUpperLeftY = mTmpUpperLeftY;

			Helper.LOGD("move: " + mUpperLeftX + " " + mUpperLeftY);

			// Update previous touch positions
			mPrevPosX = event.getX();
			mPrevPosY = event.getY();

			updateTiles();

			return true;
		}

		// the event was not handled
		return false;
	}

	/**
	 * Draw to display bitmap
	 */
	public void updateBitmap(Tile tile, Bitmap bmp) {
		if(mActive) {
			int left = getScreenX(tile.getX());
			int top = getScreenY(tile.getY());
	
			Helper.LOGD("SimpleMapView::updateBitmap() at: " + left + ", " + top
					+ " for tile: " + tile);
	
			mMapViewCanvas.drawBitmap(bmp, left, top, null);
			
			if(Preferences.DEBUG_MODE) {
				Paint paint = new Paint();
				paint.setTextSize(20);
				mMapViewCanvas.drawText(tile.toString(), left+20, top + 20, paint);
			}
		}
	}

	public void destroy() {
		Helper.LOGD("Destroying view");
		mActive = false;
		mAsyncTileResolver.finish();

		if (mMapViewBitmap != null) {
			mMapViewBitmap.recycle();
		}

		if (mTileLoadingBmp != null) {
			mTileLoadingBmp.recycle();
		}

		if (mTileErrorBmp != null) {
			mTileErrorBmp.recycle();
		}
		
		mRAMCache.destroy();
		mDiscCache.destroy();
	}

	// ///////////////////////// PRIVATE SECTION //////////////////////////////
	private AsyncTileResolver mAsyncTileResolver;
	private Bitmap mMapViewBitmap;
	private Canvas mMapViewCanvas;
	// Current screen upper left position
	private int mUpperLeftX = 0;
	private int mUpperLeftY = 0;
	// Previous touch event screen upper left position
	private float mPrevPosX = 0;
	private float mPrevPosY = 0;
	// Default loading tile bitmap
	private Bitmap mTileLoadingBmp;
	// Default error tile bitmap
	private Bitmap mTileErrorBmp;
	// First tiles update flag
	private RAMCache mRAMCache = new RAMCache();
	// First tiles update flag
	private DiscCache mDiscCache = new DiscCache();;
	// To stop drawing even if background threads are still sending updates
	private boolean mActive = true;

	/**
	 * Initialization
	 */
	private void init() {
		setWillNotDraw(false);

		if (mTileLoadingBmp == null)
			mTileLoadingBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tile_loading);

		Helper.LOGD("BBB bitmap size: " + mTileLoadingBmp.getWidth() + "x"
				+ mTileLoadingBmp.getHeight());

		if (mTileErrorBmp == null)
			mTileErrorBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tile_error);

		mAsyncTileResolver = new AsyncTileResolver(this, mDiscCache);
		mAsyncTileResolver.setListener(new BackgroundResolverListener() {
			// Updating display bitmap when tile was received asynchronously
			//    and adding it to the RAM cache
			public void onAsyncTileRequestFinished(Tile tile, Bitmap bmp) {
				tileRequestFinished(tile, bmp, false);
			}
		});
	}
	
	private void tileRequestFinished (Tile tile, Bitmap bmp, boolean mainThread) {
		synchronized(mRAMCache) {
			if (bmp == null) {
				// Trying to find a bitmap inside RAm cache
				bmp = mRAMCache.getBitmap(tile);
				
				 // If no bitmap got from RAM cache then just show "loading", if no from Internet - "error"
				if (bmp == null) 
					bmp = mainThread ? mTileLoadingBmp : mTileErrorBmp;
			} else if(!mainThread) // adding to RAM cache if tile was received by worker thread
				mRAMCache.addTile(tile, bmp);
		}
		
		updateBitmap(tile, bmp);
		
		if(mainThread)
			invalidate();
		else
			postInvalidate();
	}

	/**
	 * Returns tile's X index by screen's X coordinate
	 */
	private int getTileIndexX(int x) {
		return (Preferences.CENTER_TILE_X + (int) (x / Tile.SIZE));
	}

	/**
	 * Returns tile's Y index by screen's Y coordinate
	 */
	private int getTileIndexY(int y) {
		return (Preferences.CENTER_TILE_Y + (int) (y / Tile.SIZE));
	}

	/**
	 * Returns screen related X coordinate by tile's X index
	 */
	private int getScreenX(int tileXIndex) {
		return ((tileXIndex - Preferences.CENTER_TILE_X) * Tile.SIZE)
				- mUpperLeftX;
	}

	/**
	 * Returns screen related Y coordinate by tile's Y index
	 */
	private int getScreenY(int tileYIndex) {
		return ((tileYIndex - Preferences.CENTER_TILE_Y) * Tile.SIZE)
				- mUpperLeftY;
	}

	/**
	 * Check caches and add tiles to display bitmap
	 */
	private void updateTiles() {
		int rightTileIndex = getTileIndexX(mUpperLeftX + getWidth());
		int bottomTileIndex = getTileIndexY(mUpperLeftY + getHeight());

		Helper.LOGD("We'll update tiles until: " + rightTileIndex + ", "
				+ bottomTileIndex);

		for (int tileIndexX = getTileIndexX(mUpperLeftX) - 1; tileIndexX <= rightTileIndex; tileIndexX++) {
			for (int tileIndexY = getTileIndexY(mUpperLeftY) - 1; tileIndexY <= bottomTileIndex; tileIndexY++) {

				Tile tile = new Tile(tileIndexX, tileIndexY);

				Helper.LOGD("SimpleMapView::updateTiles() - updating tile: "
						+ tile);

				// Checking Tiles RAM cache
				if (!mRAMCache.contains(tile))
					mAsyncTileResolver.requestTile(tile);

				tileRequestFinished(tile, null, true);
			}
		}

		invalidate();
		mAsyncTileResolver.scheduleJobs();
	}
}
