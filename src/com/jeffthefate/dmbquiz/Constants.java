package com.jeffthefate.dmbquiz;

import java.io.File;


public class Constants {
    
    public static final String LOG_TAG = "DMB Trivia";
    
    public static final String ACTION_CONNECTION = 
            "com.jeffthefate.dmb.ACTION_CONNECTION";
    
    public static final String SCREENS_LOCATION = "Screenshots" +
            File.separator;
    
    public static final String ACTION_NEW_QUESTIONS =
            "com.jeffthefate.dmb.ACTION_NEW_QUESTIONS";
    
    public static final int NOTIFICATION_NEW_QUESTIONS = 3641;
    
    public static final int MENU_STATS = 0;
    public static final int MENU_BACKGROUND = 1;
    public static final int MENU_SOUND = 2;
    public static final int MENU_NOTIFICATIONS = 3;
    public static final int MENU_QUICKTIPS = 4;
    public static final int MENU_FOLLOW = 5;
    public static final int MENU_LIKE = 6;
    public static final int MENU_REPORT = 7;
    public static final int MENU_SCREEN = 8;
    public static final int MENU_NAME = 9;
    public static final int MENU_LOGOUT = 10;
    public static final int MENU_EXIT = 11;
    
    public enum TextMenus {
    	MENU_STATS, MENU_BACKGROUND, MENU_REPORT, MENU_SCREEN, MENU_NAME,
    	MENU_LOGOUT, MENU_EXIT
    }
    
}
