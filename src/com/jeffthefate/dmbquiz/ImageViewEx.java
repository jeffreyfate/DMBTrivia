/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jeffthefate.dmbquiz;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Sub-class of ImageViewEx which automatically notifies the drawable when it is
 * being displayed.
 */
public class ImageViewEx extends ImageView {

    public ImageViewEx(Context context) {
        super(context);
        //colorPaint = new Paint();
    }

    public ImageViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        //colorPaint = new Paint();
    }
    
    private HashMap<View, Integer> viewMap =
    		new HashMap<View, Integer>();
    //private Bitmap bitmap;
    //private Paint colorPaint;
    //private RectF targetRect;

    /**
     * @see android.widget.ImageViewEx#onDetachedFromWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        // This has been detached from Window, so clear the drawable
        setImageDrawable(null);
        super.onDetachedFromWindow();
    }
    /*
    @Override
    protected void onDraw(Canvas canvas) {
    	if (bitmap != null) {
    		scaleCenterCrop();
    		canvas.drawBitmap(bitmap, null, targetRect, null);
    	}
    	View view;
    	for (Entry<View, Integer> entry : viewMap.entrySet()) {
    		colorPaint.setColor(entry.getValue());
    		view = entry.getKey();
    		canvas.drawRect(view.getLeft(), view.getTop(), view.getRight(),
        			view.getBottom(), colorPaint);
    	}
    	//super.onDraw(canvas);
    }
    */
    public void addColoredView(View view, Integer color) {
    	viewMap.put(view, color);
    }
    
    public void setBitmap(Bitmap bitmap) {
    	//this.bitmap = bitmap;
    }
    
    public void scaleCenterCrop() {
    	//int sourceWidth = bitmap.getWidth();
        //int sourceHeight = bitmap.getHeight();
        //int newWidth = getWidth();
        //int newHeight = getHeight();
        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger 
        // of these two.
        //float xScale = (float) newWidth / sourceWidth;
        //float yScale = (float) newHeight / sourceHeight;
        //float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        //float scaledWidth = scale * sourceWidth;
        //float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        //float left = (newWidth - scaledWidth) / 2;
        //float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
		//targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
    }
    
    public void resetColoredViews() {
    	viewMap.clear();
    }

}