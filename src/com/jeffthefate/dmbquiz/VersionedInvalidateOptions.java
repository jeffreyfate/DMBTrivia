package com.jeffthefate.dmbquiz;

import android.app.Activity;
import android.os.Build;

import com.jeffthefate.dmbquiz.activity.ActivityMain;

public abstract class VersionedInvalidateOptions {
    
    public abstract VersionedInvalidateOptions create(Activity activity);
    public abstract void invalidateOptionsMenu();
    
    public static VersionedInvalidateOptions newInstance() {
        final int sdkVersion = Build.VERSION.SDK_INT;
        VersionedInvalidateOptions callback = null;
        if (sdkVersion < Build.VERSION_CODES.HONEYCOMB)
            callback = new GingerbreadInvalidateOptions();
        else
            callback = new HoneycombInvalidateOptions();

        return callback;
    }
    
    private static class GingerbreadInvalidateOptions extends
            VersionedInvalidateOptions {
        Activity activity;
        
        @Override
        public VersionedInvalidateOptions create(Activity activity) {
            this.activity = activity;
            return this;
        }

        @Override
        public void invalidateOptionsMenu() {
            ((ActivityMain)activity).refreshMenu();
        }
        
    }
    
    private static class HoneycombInvalidateOptions extends
            GingerbreadInvalidateOptions {
        @Override
        public void invalidateOptionsMenu() {
            activity.invalidateOptionsMenu();
        }
    }
    
}
