package com.jeffthefate.dmbquiz_dev;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;

public abstract class VersionedActionBar {
    
    public abstract VersionedActionBar create(Activity activity);
    public abstract void setDisplayHome();
    public abstract void setDisplayHomeAsUp();
    
    public static VersionedActionBar newInstance() {
        final int sdkVersion = Build.VERSION.SDK_INT;
        VersionedActionBar actionBar = null;
        if (sdkVersion < Build.VERSION_CODES.HONEYCOMB)
            actionBar = new GingerbreadActionBar();
        else
            actionBar = new HoneycombActionBar();

        return actionBar;
    }
    
    private static class GingerbreadActionBar extends
            VersionedActionBar {
        @Override
        public VersionedActionBar create(Activity activity) {
            return this;
        }

        @Override
        public void setDisplayHome() {}
        
        @Override
        public void setDisplayHomeAsUp() {}
    }
    
    private static class HoneycombActionBar extends GingerbreadActionBar {
        ActionBar actionBar;
        @Override
        public VersionedActionBar create(Activity activity) {
            actionBar = activity.getActionBar();
            return this;
        }
        
        @Override
        public void setDisplayHomeAsUp() {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP |
                    ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        }
        
        @Override
        public void setDisplayHome() {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                    ActionBar.DISPLAY_SHOW_TITLE);
        }
    }
    
}
