package com.jeffthefate.dmbquiz;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class BitmapDrawableEx extends BitmapDrawable {
    
    public BitmapDrawableEx(Resources res) {
        super(res);
    }
    
    public BitmapDrawableEx(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }
    
    public BitmapDrawableEx(Resources res, String filepath) {
        super(res, filepath);
    }
    
    public BitmapDrawableEx(Resources res, InputStream is) {
        super(res, is);
    }
    
    private int mCacheRefCount = 0;
    private int mDisplayRefCount = 0;
    private boolean mHasBeenDisplayed = false;
    // Notify the drawable that the displayed state has changed.
    // Keep a count to determine when the drawable is no longer displayed.
    public void setIsDisplayed(boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }
        // Check to see if recycle() can be called.
        checkState();
    }

    // Notify the drawable that the cache state has changed.
    // Keep a count to determine when the drawable is no longer being cached.
    public void setIsCached(boolean isCached) {
        synchronized (this) {
            if (isCached) {
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }
        // Check to see if recycle() can be called.
        checkState();
    }

    private synchronized void checkState() {
        // If the drawable cache and display ref counts = 0, and this drawable
        // has been displayed, then recycle.
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
                && hasValidBitmap()) {
            getBitmap().recycle();
        }
    }

    private synchronized boolean hasValidBitmap() {
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }

}
