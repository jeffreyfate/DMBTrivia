package com.jeffthefate.dmbquiz;

import java.io.File;


public class Constants {
    
    public static final String LOG_TAG = "DMB Trivia";
    
    public static final String ACTION_CONNECTION = 
            "com.jeffthefate.dmb.ACTION_CONNECTION";
    
    public static final String SCREENS_LOCATION = "Screenshots" +
            File.separator;
    public static final String AUDIO_LOCATION = "Audio" + File.separator;
    public static final String SELECTED_SET_FILE = "selectedSet.ser";
    public static final String LATEST_SET_FILE = "latestSet.ser";
    public static final String SETLIST_MAP_FILE = "setlistMap.ser";
    
    public static final String ACTION_NEW_QUESTIONS =
            "com.jeffthefate.dmb.ACTION_NEW_QUESTIONS";
    public static final String ACTION_NEW_SONG =
            "com.jeffthefate.dmb.ACTION_NEW_SONG";
    public static final String ACTION_UPDATE_SETLIST =
            "com.jeffthefate.dmb.ACTION_UPDATE_SETLIST";
    /*
    public static final String ACTION_UPDATE_AUDIO =
            "com.jeffthefate.dmb.ACTION_UPDATE_AUDIO";
	*/
    
    public static final int NOTIFICATION_NEW_QUESTIONS = 3641;
    public static final int NOTIFICATION_NEW_SONG = 3440;
    
    public static final int QUICK_TIP_TOP = 1;
    public static final int QUICK_TIP_CENTER = 2;
    public static final int QUICK_TIP_BOTTOM = 4;
    public static final int QUICK_TIP_LEFT = 8;
    public static final int QUICK_TIP_RIGHT = 16;
    
    public static final int EASY = 0;
    public static final int MEDIUM = 1;
    public static final int HARD = 2;
    
    public static final String PUSH_WAKE_LOCK = "PushReceiverWakeLock";
    public static final String SETLIST_WAKE_LOCK = "SetlistWakeLock";
    
    public static final String PUSH_WIFI_LOCK = "PushReceiverWifiLock";
    
    public static final int CACHED_QUESTIONS = 10;
    public static final int CACHED_LIMIT = 2;
    
    // Analytics
    public static final String CATEGORY_MENU = "categoryMenu";
    public static final String CATEGORY_ACTION_BAR = "categoryActionBar";
    public static final String CATEGORY_FRAGMENT_UI = "categoryFragmentUi";
    public static final String CATEGORY_HARDWARE = "categoryHardware";
    
    public static final String ACTION_BUTTON_PRESS = "actionButtonPress";
    public static final String ACTION_MENU_OPEN = "actionMenuOpen";
    public static final String ACTION_MENU_CLOSE = "actionMenuClose";
    public static final String ACTION_ROTATE = "actionRotate";
    
}
