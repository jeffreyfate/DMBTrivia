package com.jeffthefate.dmbquiz;

import java.io.File;


public class Constants {
    
    public static final String LOG_TAG = "DMB Trivia";
    
    public static final String ACTION_CONNECTION = 
            "com.jeffthefate.dmb.ACTION_CONNECTION";
    
    public static final String SCREENS_LOCATION = "Screenshots" +
            File.separator;
    public static final String AUDIO_LOCATION = "Audio" + File.separator;
    
    public static final String ACTION_NEW_QUESTIONS =
            "com.jeffthefate.dmb.ACTION_NEW_QUESTIONS";
    public static final String ACTION_NEW_SONG =
            "com.jeffthefate.dmb.ACTION_NEW_SONG";
    public static final String ACTION_UPDATE_SETLIST =
            "com.jeffthefate.dmb.ACTION_UPDATE_SETLIST";
    public static final String ACTION_UPDATE_AUDIO =
            "com.jeffthefate.dmb.ACTION_UPDATE_AUDIO";
    
    public static final int NOTIFICATION_NEW_QUESTIONS = 3641;
    
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
    
}
