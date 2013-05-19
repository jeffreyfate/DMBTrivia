package com.jeffthefate.dmbquiz;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RelativeLayoutEx extends RelativeLayout {
    
    private int width;

    public RelativeLayoutEx(Context context) {
        super(context);
        width = getWidth();
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        width = getWidth();
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        width = getWidth();
    }
    
    public float getXFraction() {
        if (width > 0)
            return getX() / width;
        return 0;
    }

    public void setXFraction(float xFraction) {
        setX((width > 0) ? (xFraction * width) : -9999);
    }

}