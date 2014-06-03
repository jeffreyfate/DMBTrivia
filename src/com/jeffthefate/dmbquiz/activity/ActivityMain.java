package com.jeffthefate.dmbquiz.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.OnButtonListener;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.SetlistAdapter;
import com.jeffthefate.dmbquiz.fragment.FragmentBase;
import com.jeffthefate.dmbquiz.fragment.FragmentFaq;
import com.jeffthefate.dmbquiz.fragment.FragmentInfo;
import com.jeffthefate.dmbquiz.fragment.FragmentLeaders;
import com.jeffthefate.dmbquiz.fragment.FragmentLoad;
import com.jeffthefate.dmbquiz.fragment.FragmentLogin;
import com.jeffthefate.dmbquiz.fragment.FragmentNameDialog;
import com.jeffthefate.dmbquiz.fragment.FragmentPager;
import com.jeffthefate.dmbquiz.fragment.FragmentScoreDialog;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class ActivityMain extends SlidingFragmentActivity implements
		OnButtonListener {
    
    private ParseUser user;
    private String userId;
    
    private TextView noConnection;
    
    private FragmentManager fMan;
    private FragmentBase currFrag;
    
    private int rawIndex = -1;
    private Field[] fields;
    private ArrayList<Integer> fieldsList;
    private String portBackground = null;
    private String landBackground = null;
    //private String splashBackground = null;
    //private String quizBackground = null;
    //private String leadersBackground = null;
    
    private boolean loggedIn = false;
    private boolean isLogging = false;
    private boolean loggingOut = false;
    private boolean inLoad = false;
    private boolean inStats = false;
    private boolean inInfo = false;
    private boolean inFaq = false;
    private boolean inSetlist = false;
    
    private ArrayList<String> questionIds = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    private ArrayList<String> questions = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    private ArrayList<String> questionAnswers = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    private ArrayList<String> questionCategories = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    private ArrayList<String> questionScores = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    private ArrayList<String> questionHints = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    private ArrayList<String> questionSkips = new ArrayList<String>(Constants.CACHED_QUESTIONS);
    
    private boolean newQuestion = false;
    
    private GetStatsTask getStatsTask;
    private GetScoreTask getScoreTask;
    private GetNextQuestionsTask getNextQuestionsTask;
    private GetStageTask getStageTask;
    private ParseTask parseTask;
    
    private Bundle leadersBundle;
    private ArrayList<String> rankList = new ArrayList<String>();
    private ArrayList<String> userList = new ArrayList<String>();
    private ArrayList<String> scoreList = new ArrayList<String>();
    private ArrayList<String> userIdList = new ArrayList<String>();
    
    private String displayName;
    private int currScore = -1;
    
    private UserTask userTask;
    
    private ArrayList<String> correctAnswers;
    
    private boolean networkProblem = false;
    private boolean facebookLogin = false;
    private boolean newUser = false;
    
    private TreeMap<String, TreeMap<String, String>> setlistMap =
    		new TreeMap<String, TreeMap<String, String>>(
    		String.CASE_INSENSITIVE_ORDER);
    private int firstVisiblePosition = 0;
    private ArrayList<Integer> expandedGroups = new ArrayList<Integer>(0);
    
    public interface UiCallback {
        public void showNetworkProblem();
        public void showLoading(String message);
        public void showNoMoreQuestions(int level);
        public void resumeQuestion();
        public void updateScoreText();
        public void updateSetText();
        public void resetHint();
        public void disableButton(boolean isRetry);
        public void enableButton(boolean isRetry);
        public void setDisplayName(String displayName);
        public Drawable getBackground();
        //public void setBackground(Bitmap background);
        public void showRetry();
        public int getPage();
        public void setPage(int page);
        public void setBackground(Bitmap newBackground);
        public void showResizedSetlist();
        public void hideResizedSetlist();
        public void setSetlistText(String setlistText);
        public void setSetlistStampVisible(boolean isVisible);
    }
    
    private NotificationManager nManager;
    
    private ConnectionReceiver connReceiver;
    
    //private Bitmap tempBitmap;
    //private BitmapDrawableEx[] arrayDrawable = new BitmapDrawableEx[2];
    //private BitmapDrawableEx oldBitmapDrawable = null;
    
    //private int width = 0;
    //private int height = 0;
    
    private int lowest = 0;
    private int highest = 0;
    
    private RelativeLayout statsButton;
    private RelativeLayout switchButton;
    private RelativeLayout infoButton;
    private RelativeLayout reportButton;
    private RelativeLayout shareButton;
    private RelativeLayout nameButton;
    private RelativeLayout exitButton;
    private RelativeLayout logoutButton;
    private ExpandableListView setlistListView;
    private ProgressBar setlistProgress;
    //private TextView logoutText;
    // TODO Re-add levels
    //protected RelativeLayout levelButton;
    //protected ImageViewEx levelImage;
    //protected TextView levelText;
    private RelativeLayout soundsButton;
    private CheckedTextView soundsText;
    private RelativeLayout notificationsButton;
    private CheckedTextView notificationsText;
    private RelativeLayout notificationSoundButton;
    private TextView notificationSoundText;
    private ImageViewEx notificationSoundImage;
    private RelativeLayout notificationAlbumButton;
    private TextView notificationAlbumText;
    private ImageViewEx notificationAlbumImage;
    //protected RelativeLayout tipsButton;
    //protected CheckedTextView tipsText;
    
    private RelativeLayout followButton;
    private RelativeLayout likeButton;
    
    //protected ViewGroup quickTipView;
    //protected ViewGroup quickTipMenuView;
    //protected ViewGroup quickTipLeftView;
    //protected ViewGroup quickTipRightView;
    //protected ViewGroup quickTipTopView;
    //protected ViewGroup quickTipBottomView;
    
    private boolean goToSetlist = false;
    
    private Menu mMenu;
    
    private MenuItem shareItem;
    private MenuItem setlistItem;
    
    private ScreenshotTask screenshotTask;
    private LogoutWaitTask logoutWaitTask;
    private SetBackgroundTask setBackgroundTask;
    private SetBackgroundWaitTask setBackgroundWaitTask;
    //private SetlistBackgroundWaitTask setlistBackgroundWaitTask;
    
    //private int count = -1;
    //private boolean checkCount = true;
    
    private GoogleAnalytics googleAnalytics;
    private Tracker tracker;
    
    /**
     * Activity lifecycle methods
     */
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyDialog()
                .penaltyLog()
                .permitDiskReads()
                .permitDiskWrites()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        */
        super.onCreate(null);
        /*
        Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
        	width = display.getWidth();
        	height = display.getHeight();
        }
        else {
        	Point size = new Point();
        	display.getSize(size);
        	width = size.x;
        	height = size.y;
        }
        */
        setContentView(R.layout.main);
        fields = R.drawable.class.getFields();
        fieldsList = new ArrayList<Integer>();
        for (Field field : fields) {
            if (field.getName().contains("splash")) {
                try {
                    fieldsList.add(field.getInt(null));
                } catch (IllegalArgumentException e1) {e1.printStackTrace();
                } catch (IllegalAccessException e1) {e1.printStackTrace();}
            }
        }
        noConnection = (TextView) findViewById(R.id.NoConnection);
        /*
        TEST
        Parse.initialize(this, "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
                "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
        */
        /*
        Parse.initialize(this, "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
                "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
        */
        //PushService.subscribe(this, "", ActivityMain.class);
        // TODO Add these to update to the setlist channel and remove old
        //PushService.unsubscribe(app, "");
        PushService.subscribe(ApplicationEx.getApp(), "setlist",
        		ActivityMain.class);
        PushService.setDefaultPushCallback(this, ActivityMain.class);
        fMan = getSupportFragmentManager();
        if (savedInstanceState != null) {
            loggedIn = savedInstanceState.getBoolean("loggedIn");
            isLogging = savedInstanceState.getBoolean("isLogging");
            inLoad = savedInstanceState.getBoolean("inLoad");
            inStats = savedInstanceState.getBoolean("inStats");
            inInfo = savedInstanceState.getBoolean("inInfo");
            inFaq = savedInstanceState.getBoolean("inFaq");
            inSetlist = savedInstanceState.getBoolean("inSetlist");
            /*
            splashBackground = savedInstanceState.getString(
                    "splashBackground");
            quizBackground = savedInstanceState.getString(
                    "quizBackground");
            Log.i(Constants.LOG_TAG, "ActivityMain savedInstanceState background: " + quizBackground);
            leadersBackground = savedInstanceState.getString(
                    "leadersBackground");
            */
            portBackground = savedInstanceState.getString("portBackground");
            landBackground = savedInstanceState.getString("landBackground");
            userId = savedInstanceState.getString("userId");
            newQuestion = savedInstanceState.getBoolean("newQuestion");
            displayName = savedInstanceState.getString("displayName");
            currScore = savedInstanceState.getInt("currScore");
            questionIds = savedInstanceState.getStringArrayList("questionIds");
            if (questionIds == null)
            	questionIds = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            questions = savedInstanceState.getStringArrayList("questions");
            if (questions == null)
            	questions = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            questionAnswers = savedInstanceState.getStringArrayList("questionAnswers");
            if (questionAnswers == null)
            	questionAnswers = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            questionCategories = savedInstanceState.getStringArrayList("questionCategories");
            if (questionCategories == null)
            	questionCategories = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            questionScores = savedInstanceState.getStringArrayList("questionScores");
            if (questionScores == null)
            	questionScores = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            questionHints = savedInstanceState.getStringArrayList("questionHints");
            if (questionHints == null)
            	questionHints = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            questionSkips = savedInstanceState.getStringArrayList("questionSkips");
            if (questionSkips == null)
            	questionSkips = new ArrayList<String>(Constants.CACHED_QUESTIONS);
            correctAnswers = savedInstanceState.getStringArrayList(
                    "correctAnswers");
            newUser = savedInstanceState.getBoolean("newUser");
            lowest = savedInstanceState.getInt("lowest");
            highest = savedInstanceState.getInt("highest");
            networkProblem = savedInstanceState.getBoolean("networkProblem");
            DatabaseHelperSingleton.instance().setUserValue(networkProblem ? 1 : 0,
                    DatabaseHelper.COL_NETWORK_PROBLEM, userId);
            //count = savedInstanceState.getInt("count");
            //checkCount = savedInstanceState.getBoolean("checkCount");
            setlistMap = (TreeMap<String, TreeMap<String, String>>)
            		savedInstanceState.getSerializable("setlists");
            firstVisiblePosition = savedInstanceState.getInt("firstVisible");
            expandedGroups = savedInstanceState.getIntegerArrayList(
            		"expandedGroups");
        }
        else {
            userId = DatabaseHelperSingleton.instance().getCurrUser();
            if (userId != null)
                getPersistedData(userId);
        }
        if (userId == null) {
            userId = DatabaseHelperSingleton.instance().getCurrUser();
            if (userId != null)
                getUserData(userId);
        }
        /*
        if (userId != null) {
            if (splashBackground == null)
                splashBackground =
                        DatabaseHelperSingleton.instance().getSplashBackground(userId);
            if (quizBackground == null)
                quizBackground =
                        DatabaseHelperSingleton.instance().getQuizBackground(userId);
            if (leadersBackground == null)
                leadersBackground =
                        DatabaseHelperSingleton.instance().getLeadersBackground(userId);
        }
        */
        if (userId != null) {
            if (portBackground == null)
                portBackground = DatabaseHelperSingleton.instance().getPortBackground(userId);
            if (landBackground == null)
                landBackground = DatabaseHelperSingleton.instance().getLandBackground(userId);
        }
        nManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        connReceiver = new ConnectionReceiver();
        getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
        getSlidingMenu().setFadeDegree(0.35f);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.notification_key))) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                SharedPreferencesSingleton.instance().edit().putBoolean(
                		ResourcesSingleton.instance().getString(R.string.notification_key), true).commit();
            else
                SharedPreferencesSingleton.instance().edit().putBoolean(
                		ResourcesSingleton.instance().getString(R.string.notification_key), true).apply();
        }
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.level_key))) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                SharedPreferencesSingleton.instance().edit().putInt(
                		ResourcesSingleton.instance().getString(R.string.level_key),
                        Constants.HARD).commit();
            else
                SharedPreferencesSingleton.instance().edit().putInt(
                		ResourcesSingleton.instance().getString(R.string.level_key),
                        Constants.HARD).apply();
        }
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.notificationsound_key))) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                SharedPreferencesSingleton.instance().edit().putInt(
                		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0).commit();
            else
                SharedPreferencesSingleton.instance().edit().putInt(
                		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0).apply();
        }
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.notificationtype_key))) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                SharedPreferencesSingleton.instance().edit().putBoolean(
                		ResourcesSingleton.instance().getString(R.string.notificationtype_key), true).commit();
            else
                SharedPreferencesSingleton.instance().edit().putBoolean(
                		ResourcesSingleton.instance().getString(R.string.notificationtype_key), true).apply();
        }
        Log.d(Constants.LOG_TAG, "ActivityMain onCreate");
        refreshSlidingMenu();
        getWindow().setBackgroundDrawable(null);
        googleAnalytics = GoogleAnalytics.getInstance(this);
    	tracker = googleAnalytics.getTracker("UA-41733963-1");
    }
    
    private void getPersistedData(String userId) {
        loggedIn = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_LOGGED_IN, userId) == 1 ? true : false;
        isLogging = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_LOGGING, userId) == 1 ? true : false;
        inLoad = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_IN_LOAD, userId) == 1 ? true : false;
        inStats = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_IN_STATS, userId) == 1 ? true : false;
        inInfo = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_IN_INFO, userId) == 1 ? true : false;
        inFaq = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_IN_FAQ, userId) == 1 ? true : false;
        inSetlist = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_IN_SETLIST, userId) == 1 ? true : false;
        getUserData(userId);
        networkProblem = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_NETWORK_PROBLEM, userId) == 1 ? true : false;
    }
    
    private void getUserData(String userId) {
        /*
        splashBackground = DatabaseHelperSingleton.instance().getUserStringValue(
                DatabaseHelper.COL_SPLASH_BACKGROUND, userId);
        quizBackground = DatabaseHelperSingleton.instance().getUserStringValue(
                DatabaseHelper.COL_QUIZ_BACKGROUND, userId);
        Log.i(Constants.LOG_TAG, "getUserData background: " + quizBackground);
        leadersBackground = DatabaseHelperSingleton.instance().getUserStringValue(
                DatabaseHelper.COL_LEADERS_BACKGROUND, userId);
        */
        portBackground = DatabaseHelperSingleton.instance().getPortBackground(
        		userId);
        landBackground = DatabaseHelperSingleton.instance().getLandBackground(
        		userId);
        newQuestion = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_NEW_QUESTION, userId) == 1 ? true : false;
        displayName = DatabaseHelperSingleton.instance().getUserStringValue(
                DatabaseHelper.COL_DISPLAY_NAME, userId);
        currScore = DatabaseHelperSingleton.instance().getUserIntValue(
                DatabaseHelper.COL_SCORE, userId);
        questionHints = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.questionhints_key));
        if (questionHints == null)
        	questionHints = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        questionSkips = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.questionskips_key));
        if (questionSkips == null)
        	questionSkips = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        questionIds = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questionids_key));
        if (questionIds == null)
        	questionIds = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        if (questionIds.isEmpty()) {
        	String questionId = DatabaseHelperSingleton.instance()
        			.getCurrQuestionId(userId);
        	boolean questionHint = DatabaseHelperSingleton.instance()
        			.getCurrQuestionHint(userId);
        	if (questionId != null) {
        		addQuestionId(questionId);
        		addQuestionHint(questionHint);
        		addQuestionSkip(false);
        	}
        	questionId = DatabaseHelperSingleton.instance()
        			.getNextQuestionId(userId);
        	questionHint = DatabaseHelperSingleton.instance()
        			.getNextQuestionHint(userId);
        	if (questionId != null) {
        		addQuestionId(questionId);
        		addQuestionHint(questionHint);
        		addQuestionSkip(false);
        	}
        	questionId = DatabaseHelperSingleton.instance()
        			.getThirdQuestionId(userId);
        	questionHint = DatabaseHelperSingleton.instance()
        			.getThirdQuestionHint(userId);
        	if (questionId != null) {
        		addQuestionId(questionId);
        		addQuestionHint(questionHint);
        		addQuestionSkip(false);
        	}
        }
        questions = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questions_key));
        if (questions == null)
        	questions = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        if (questions.isEmpty()) {
        	String question = DatabaseHelperSingleton.instance()
        			.getCurrQuestionQuestion(userId);
        	if (question != null)
        		addQuestion(question);
        	question = DatabaseHelperSingleton.instance()
        			.getNextQuestionQuestion(userId);
        	if (question != null)
        		addQuestion(question);
        	question = DatabaseHelperSingleton.instance()
        			.getThirdQuestionQuestion(userId);
        	if (question != null)
        		addQuestion(question);
        }
        questionAnswers = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.questionanswers_key));
        if (questionAnswers == null)
        	questionAnswers = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        if (questionAnswers.isEmpty()) {
        	String questionAnswer = DatabaseHelperSingleton.instance()
        			.getCurrQuestionAnswer(userId);
        	if (questionAnswer != null)
        		addQuestionAnswer(questionAnswer);
        	questionAnswer = DatabaseHelperSingleton.instance()
        			.getNextQuestionAnswer(userId);
        	if (questionAnswer != null)
        		addQuestionAnswer(questionAnswer);
        	questionAnswer = DatabaseHelperSingleton.instance()
        			.getThirdQuestionAnswer(userId);
        	if (questionAnswer != null)
        		addQuestionAnswer(questionAnswer);
        }
        questionCategories = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.questioncategories_key));
        if (questionCategories == null)
        	questionCategories = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        if (questionCategories.isEmpty()) {
        	String questionCategory = DatabaseHelperSingleton.instance()
        			.getCurrQuestionCategory(userId);
        	if (questionCategory != null)
        		addQuestionCategory(questionCategory);
        	questionCategory = DatabaseHelperSingleton.instance()
        			.getNextQuestionCategory(userId);
        	if (questionCategory != null)
        		addQuestionCategory(questionCategory);
        	questionCategory = DatabaseHelperSingleton.instance()
        			.getThirdQuestionCategory(userId);
        	if (questionCategory != null)
        		addQuestionCategory(questionCategory);
        }
        questionScores = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.questionscores_key));
        if (questionScores == null)
        	questionScores = new ArrayList<String>(Constants.CACHED_QUESTIONS);
        if (questionScores.isEmpty()) {
        	String questionScore = DatabaseHelperSingleton.instance()
        			.getCurrQuestionScore(userId);
        	if (questionScore != null)
        		addQuestionScore(questionScore);
        	questionScore = DatabaseHelperSingleton.instance()
        			.getNextQuestionScore(userId);
        	if (questionScore != null)
        		addQuestionScore(questionScore);
        	questionScore = DatabaseHelperSingleton.instance()
        			.getThirdQuestionScore(userId);
        	if (questionScore != null)
        		addQuestionScore(questionScore);
        }
        correctAnswers = ApplicationEx.getStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.correct_key));
        //count = DatabaseHelperSingleton.instance().getUserIntValue(DatabaseHelper.COL_COUNT, userId);
        //checkCount = DatabaseHelperSingleton.instance().getUserIntValue(DatabaseHelper.COL_CHECK_COUNT, userId) == 1 ? true : false;
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("setlist", false)) {
        	tracker.sendView("ActivityMain/SetlistNotificationClicked");
            goToSetlist = true;
        }
        else
            goToSetlist = false;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("loggedIn", loggedIn);
        outState.putBoolean("isLogging", isLogging);
        outState.putBoolean("inLoad", inLoad);
        outState.putBoolean("inStats", inStats);
        outState.putBoolean("inInfo", inInfo);
        outState.putBoolean("inFaq", inFaq);
        outState.putBoolean("inSetlist", inSetlist);
        /*
        outState.putString("splashBackground", splashBackground);
        outState.putString("quizBackground", quizBackground);
        outState.putString("leadersBackground", leadersBackground);
        */
        outState.putString("portBackground", portBackground);
        outState.putString("landBackground", landBackground);
        outState.putString("userId", userId);
        outState.putBoolean("newQuestion", newQuestion);
        outState.putString("displayName", displayName);
        outState.putInt("currScore", currScore);
        outState.putStringArrayList("questionIds", questionIds);
        outState.putStringArrayList("questions", questions);
        outState.putStringArrayList("questionAnswers", questionAnswers);
        outState.putStringArrayList("questionCategories", questionCategories);
        outState.putStringArrayList("questionScores", questionScores);
        outState.putStringArrayList("questionHints", questionHints);
        outState.putStringArrayList("questionSkips", questionSkips);
        outState.putStringArrayList("correctAnswers", correctAnswers);
        outState.putBoolean("newUser", newUser);
        outState.putInt("lowest", lowest);
        outState.putInt("highest", highest);
        outState.putBoolean("networkProblem", networkProblem);
        //outState.putInt("count", count);
        //outState.putBoolean("checkCount", true);
        outState.putSerializable("setlists", setlistMap);
        outState.putInt("firstVisible",
        		setlistListView.getFirstVisiblePosition());
        outState.putIntegerArrayList("expandedGroups", expandedGroups);
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (parseTask != null) {
        	parseTask.cancel(true);
        }
        if (getScoreTask != null) {
            getScoreTask.cancel(true);
        }
        if (user == null) {
            user = ParseUser.getCurrentUser();
        }
        if (userId == null && user != null) {
            userId = user.getObjectId();
            if (userId != null) {
                getUserData(userId);
            }
        }
        if (user != null) {
        	getFacebookDisplayName(user);
        }
        if (!isLogging) {
            if (userId == null) {
                if (!loggedIn) {
                    logOut();
                }
                else {
                    checkUser();
                }
            }
            else {
                if (correctAnswers == null) {
                    isLogging = true;
                    showLogin();
                    if (!DatabaseHelperSingleton.instance().isAnonUser(
                    		userId)) {
                    	getScore(false, true, userId, false);
                    }
                    else {
                    	correctAnswers = new ArrayList<String>();
                    	if (questionIds.size() >= 1 && questionIds.get(0) != null) {
                            try {
                                goToQuiz();
                            } catch (IllegalStateException exception) {}
                        }
                        else {
                            getNextQuestions(false, SharedPreferencesSingleton.instance().getInt(
                            		ResourcesSingleton.instance().getString(R.string.level_key),
            						Constants.HARD));
                        }
                    }
                }
                else {
                    showLoggedInFragment();
                }
            }
        }
        else {
            if (userId != null) {
                setupUser(newUser);
            }
            else if (!facebookLogin) {
                checkUser();
            }
        }
        if (inInfo) {
            onInfoPressed();
        }
        if (inFaq) {
        	onFaqPressed();
        }
        /*
        if (inSetlist)
            showSetlist(false);
        */
        ApplicationEx.setActive();
        nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
        registerReceiver(connReceiver,
                new IntentFilter(Constants.ACTION_CONNECTION));
        /*
        if (!inSetlist) {
            if (invalidateWaitTask != null)
                invalidateWaitTask.cancel(true);
            invalidateWaitTask = new InvalidateWaitTask(this);
            if (Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.HONEYCOMB)
                invalidateWaitTask.execute();
            else
                invalidateWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        */
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	switch (newConfig.orientation) {
    	case Configuration.ORIENTATION_LANDSCAPE:
    		tracker.sendEvent(Constants.CATEGORY_HARDWARE,
        			Constants.ACTION_ROTATE, "landscape", 1l);
    		if (inSetlist) {
    			getSlidingMenu().setBehindOffsetRes(R.dimen.menu_setlist_width_land);
    		}
    		else {
    			getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
    		}
    		break;
    	case Configuration.ORIENTATION_PORTRAIT:
    		tracker.sendEvent(Constants.CATEGORY_HARDWARE,
        			Constants.ACTION_ROTATE, "portrait", 1l);
    		if (inSetlist) {
    			getSlidingMenu().setBehindOffsetRes(R.dimen.menu_setlist_width_port);
    		}
    		else {
    			getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
    		}
    		break;
    	}
    }
    
    private void showLogin(/*boolean loggingIn*/) {
        try {
            FragmentLogin fLogin = new FragmentLogin();
            currFrag = fLogin;
            FragmentTransaction ft = fMan.beginTransaction();
            if (currFrag != null && currFrag instanceof FragmentPager &&
                    currFrag.isVisible())
                ((FragmentPager)currFrag).removeChildren(ft);
            /*
            if (loggingIn)
                ft.setCustomAnimations(R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom);
            else
                ft.setCustomAnimations(R.anim.slide_in_top,
                        R.anim.slide_out_top);
            */
            ft.replace(android.R.id.content, fLogin, "fLogin")
                    .commitAllowingStateLoss();
            fMan.executePendingTransactions();
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        } catch (IllegalStateException e) {}
    }
    
    private void showLoggedInFragment() {
        //fetchDisplayName();
        if (user == null)
            user = ParseUser.getCurrentUser();
        if (user != null && displayName == null) {
            displayName = user.getString("displayName");
            DatabaseHelperSingleton.instance().setUserValue(displayName, 
                    DatabaseHelper.COL_DISPLAY_NAME, userId);
        }
        if (inStats)
            showLeaders();
        else if (inLoad)
            onStatsPressed();
        /*
        else if (inSetlist)
            showSetlist(false);
        */
        else {
            isLogging = false;
            DatabaseHelperSingleton.instance().setUserValue(isLogging ? 1 : 0,
                    DatabaseHelper.COL_LOGGING, userId);
            loggedIn = true;
            DatabaseHelperSingleton.instance().setUserValue(loggedIn ? 1 : 0,
                    DatabaseHelper.COL_LOGGED_IN, userId);
            showQuiz(false, false);
        }
    }
    
    private void fetchDisplayName() {
        if (userId == null)
            return;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        query.getInBackground("displayName", new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null)
                    displayName = user.getString("displayName");
                if (getStatsTask != null)
                    getStatsTask.cancel(true);
                getStatsTask = new GetStatsTask(
                        DatabaseHelperSingleton.instance().getScore(userId));
                if (Build.VERSION.SDK_INT <
                        Build.VERSION_CODES.HONEYCOMB)
                    getStatsTask.execute();
                else
                    getStatsTask.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }
    
    private void showLeaders() {
        try {
            FragmentLeaders fLeaders = new FragmentLeaders();
            currFrag = fLeaders;
            fMan.beginTransaction().replace(android.R.id.content, fLeaders,
                    "fLeaders").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            refreshSlidingMenu();
        } catch (IllegalStateException e) {}
    }
    
    private void checkUser() {
        noConnection.setVisibility(View.INVISIBLE);
        user = ParseUser.getCurrentUser();
        if (user == null)
            userId = null;
        else {
            userId = user.getObjectId();
            if (userId != null)
                getUserData(userId);
        }
        if (!loggedIn && !isLogging && (user == null || userId == null))
            logOut();
        else
            setupUser(newUser);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
    
    @Override
    public void onBackPressed() {
        if (!inStats && !inLoad && !isLogging && !inInfo && !inFaq)
            moveTaskToBack(true);
        else {
            if (inLoad) {
                if (getStatsTask != null)
                    getStatsTask.cancel(true);
                showQuiz(true, false);
                inLoad = false;
                DatabaseHelperSingleton.instance().setUserValue(inLoad ? 1 : 0,
                        DatabaseHelper.COL_IN_LOAD, userId);
            }
            else if (inFaq) {
                showLeaders();
                inFaq = false;
                DatabaseHelperSingleton.instance().setUserValue(inFaq ? 1 : 0,
                        DatabaseHelper.COL_IN_FAQ, userId);
            }
            else if (inStats) {
                inStats = false;
                DatabaseHelperSingleton.instance().setUserValue(inStats ? 1 : 0,
                        DatabaseHelper.COL_IN_STATS, userId);
                showQuiz(true, false);
            }
            else if (inInfo) {
                showSplash(true, false);
                inInfo = false;
                DatabaseHelperSingleton.instance().setUserValue(inInfo ? 1 : 0,
                        DatabaseHelper.COL_IN_INFO, userId);
            }
            else if (isLogging) {
                if (userTask != null)
                    userTask.cancel(true);
                if (getScoreTask != null)
                    getScoreTask.cancel(true);
                logOut();
            }
        }
    }
    
    private void showQuiz(boolean fromStats, boolean fromSetlist) {
        newUser = false;
        try {
        	if (fMan.findFragmentByTag("fQuiz") == null) {
        	    FragmentPager fQuiz = new FragmentPager();
        	    currFrag = fQuiz;
	            FragmentTransaction ft = fMan.beginTransaction();
	            /*
	            if (fromStats)
	                ft.setCustomAnimations(R.anim.slide_in_top,
	                        R.anim.slide_out_top);
	            else if (fromSetlist)
	                ft.setCustomAnimations(R.anim.slide_in_left,
	                        R.anim.slide_out_right);
                */
	            ft.replace(android.R.id.content, fQuiz, "fQuiz")
	                    .commitAllowingStateLoss();
	            fMan.executePendingTransactions();
        	}
        	else {
        		currFrag = (FragmentBase) fMan.findFragmentByTag("fQuiz");
        	}
        	Log.d(Constants.LOG_TAG, "ActivityMain showQuiz");
        	refreshSlidingMenu();
        	currFrag.updateSetText();
        } catch (IllegalStateException e) {}
    }
    
    private void showSplash(boolean fromInfo, boolean fromSetlist) {
        try {
            FragmentPager fSplash = new FragmentPager();
            currFrag = fSplash;
            FragmentTransaction ft = fMan.beginTransaction();
            /*
            if (fromInfo)
                ft.setCustomAnimations(R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom);
            else if (fromSetlist)
                ft.setCustomAnimations(R.anim.slide_in_left,
                        R.anim.slide_out_right);
            */
            ft.replace(android.R.id.content, fSplash, "fSplash")
                    .commitAllowingStateLoss();
            fMan.executePendingTransactions();
            Log.d(Constants.LOG_TAG, "ActivityMain showSplash");
            refreshSlidingMenu();
            currFrag.updateSetText();
        } catch (IllegalStateException e) {}
    }
    
    @Override
    public void onPause() {
        try {
            unregisterReceiver(connReceiver);
        } catch (IllegalArgumentException e) {}
        if (getStageTask != null)
            getStageTask.cancel(true);
        if (getNextQuestionsTask != null)
            getNextQuestionsTask.cancel(true);
        if (userTask != null)
            userTask.cancel(true);
        if (getStatsTask != null)
            getStatsTask.cancel(true);
        if (!isLogging && userId != null &&
        		!DatabaseHelperSingleton.instance().isAnonUser(userId))
            getScore(false, false, userId, false);
        ApplicationEx.setInactive();
        DatabaseHelperSingleton.instance().setCheckCount(userId, true);
        super.onPause();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	EasyTracker.getInstance().activityStop(this);
    }
    
    private void getScore(boolean show, boolean restore, String userId,
            boolean newUser) {
        if (getScoreTask != null)
            getScoreTask.cancel(true);
        getScoreTask = new GetScoreTask(show, restore, userId, newUser);
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
            getScoreTask.execute();
        else
            getScoreTask.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private class GetScoreTask extends AsyncTask<Void, Void, Void> {
        private boolean show;
        private boolean restore;
        private String userId;
        private boolean newUser;
        private List<ParseObject> scoreList;
        private List<ParseObject> deleteList;
        private Number number;
        private ArrayList<String> tempAnswers;
        private int tempScore = 0;
        
        private GetScoreTask(boolean show, boolean restore, String userId,
                boolean newUser) {
            this.show = show;
            this.restore = restore;
            this.userId = userId;
            this.newUser = newUser;
        }
        
        @Override
        protected void onPreExecute() {
        	if (tempAnswers != null)
                tempAnswers.clear();
            else
                tempAnswers = new ArrayList<String>();
            if (!newUser && currFrag != null)
                currFrag.showLoading("Calculating score...");
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            int questionScore = -1;
            // Find correct answers for current user
            ParseQuery<ParseObject> correctQuery = new ParseQuery<ParseObject>("CorrectAnswers");
            correctQuery.whereEqualTo("userId", userId);
            correctQuery.setLimit(1000);
            // Find the questions of the correct answers
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Question");
            query.setLimit(1000);
            // Delete dupes
            ParseQuery<ParseObject> deleteQuery = new ParseQuery<ParseObject>("CorrectAnswers");
            deleteQuery.whereEqualTo("userId", userId);
            deleteQuery.setLimit(1000);
            do {
            	// Find hinted correct answers
                correctQuery.whereEqualTo("hint", true);
                // Skip correct answers already found
                correctQuery.whereNotContainedIn("questionId", tempAnswers);
                // Get the question object of these correct answers
                query.whereMatchesKeyInQuery("objectId", "questionId",
                        correctQuery);
                try {
                    scoreList = query.find();
                    if (scoreList.size() == 0)
                        break;
                    for (ParseObject score : scoreList) {
                        if (isCancelled())
                            return null;
                        // Delete this answer if we already have it (not the
                        // question)
                        // Likely not found too often
                        if (tempAnswers.contains(score.getObjectId()) &&
                                userId != null) {
                            deleteQuery.whereEqualTo("questionId",
                                    score.getObjectId());
                            deleteList = deleteQuery.find();
                            for (int i = 1; i < deleteList.size(); i++) {
                                try {
                                    deleteList.get(i).deleteEventually();
                                } catch (RuntimeException exception) {}
                            }
                        }
                        else {
                        	// Get this score and add it, reducing it first
                            number = score.getNumber("score");
                            questionScore = number == null ? 1000 :
                                    number.intValue();
                            tempScore += questionScore / 2;
                            tempAnswers.add(score.getObjectId());
                        }
                    }
                } catch (OutOfMemoryError memErr) {
                	Log.e(Constants.LOG_TAG, "Error: " + memErr.getMessage());
                	if (userTask != null)
                        userTask.cancel(true);
                    if (getScoreTask != null)
                        getScoreTask.cancel(true);
                    getScore(show, restore, userId, newUser);
                } catch (ParseException e) {
                    Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                    if (userTask != null)
                        userTask.cancel(true);
                    if (getScoreTask != null)
                        getScoreTask.cancel(true);
                    if (show && currFrag != null)
                        currFrag.showNetworkProblem();
                }
            // Keep going until no more are found
            } while (!isCancelled());
            // Find the non-hinted ones and add their score, deleting dupes also
            do {
                correctQuery.whereEqualTo("hint", false);
                correctQuery.whereNotContainedIn("questionId", tempAnswers);
                query.whereMatchesKeyInQuery("objectId", "questionId",
                        correctQuery);
                try {
                    scoreList = query.find();
                    if (scoreList.size() == 0)
                        break;
                    for (ParseObject score : scoreList) {
                        if (isCancelled())
                            return null;
                        if (tempAnswers.contains(score.getObjectId()) &&
                                userId != null) {
                            deleteQuery.whereEqualTo("questionId",
                                    score.getObjectId());
                            deleteList = deleteQuery.find();
                            for (int i = 1; i < deleteList.size(); i++) {
                                try {
                                    deleteList.get(i).deleteEventually();
                                } catch (RuntimeException exception) {}
                            }
                        }
                        else {
                            number = score.getNumber("score");
                            questionScore = number == null ? 1000 :
                                    number.intValue();
                            tempScore += questionScore;
                            tempAnswers.add(score.getObjectId());
                        }
                    }
                } catch (OutOfMemoryError memErr) {
                	Log.e(Constants.LOG_TAG, "Error: " + memErr.getMessage());
                	if (userTask != null)
                        userTask.cancel(true);
                    if (getScoreTask != null)
                        getScoreTask.cancel(true);
                    getScore(show, restore, userId, newUser);
                } catch (ParseException e) {
                    Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                    if (userTask != null)
                        userTask.cancel(true);
                    if (getScoreTask != null)
                        getScoreTask.cancel(true);
                    if (show && currFrag != null)
                        currFrag.showNetworkProblem();
                }
            } while (!isCancelled());
            // TODO Revisit when adding levels back
            /*
            ParseQuery scoreQuery = new ParseQuery("Question");
            try {
            	scoreQuery.orderByAscending("score");
				lowest = scoreQuery.getFirst().getInt("score");
				ApplicationEx.addParseQuery();
				scoreQuery.orderByDescending("score");
				highest = scoreQuery.getFirst().getInt("score");
				if (highest == 0)
					highest = 1000;
				int easy = ((highest-lowest) / 3) + lowest;
				int med = ((easy-lowest) * 2) + lowest;				
				DatabaseHelperSingleton.instance().setUserValue(easy,
						DatabaseHelper.COL_EASY, userId);
				DatabaseHelperSingleton.instance().setUserValue(med,
						DatabaseHelper.COL_MEDIUM, userId);
			} catch (ParseException e) {
				Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                if (userTask != null)
                    userTask.cancel(true);
                if (getScoreTask != null)
                    getScoreTask.cancel(true);
                if (show && currFrag != null)
                    currFrag.showNetworkProblem();
			}
			*/
            if (!isCancelled()) {
                currScore = tempScore;
                tempScore = 0;
                saveUserScore(currScore);
                correctAnswers = new ArrayList<String>(tempAnswers);
                ApplicationEx.setStringArrayPref(
                		ResourcesSingleton.instance().getString(R.string.correct_key), correctAnswers);
                publishProgress();
            }
            tempScore = 0;
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        	tempScore = 0;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (show) {
                if (questionIds.size() >= 1 && questionIds.get(0) != null) {
                    try {
                        goToQuiz();
                    } catch (IllegalStateException exception) {}
                }
                else
                    getNextQuestions(false, SharedPreferencesSingleton.instance().getInt(
                    		ResourcesSingleton.instance().getString(R.string.level_key),
    						Constants.HARD));
            }
            if (restore)
                showLoggedInFragment();
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
        }
    }
    
    private void goToQuiz() {
        isLogging = false;
        DatabaseHelperSingleton.instance().setUserValue(isLogging ? 1 : 0,
                DatabaseHelper.COL_LOGGING, userId);
        loggedIn = true;
        DatabaseHelperSingleton.instance().setUserValue(loggedIn ? 1 : 0,
                DatabaseHelper.COL_LOGGED_IN, userId);
        showQuiz(false, false);
    }
    
    @Override
    public void onDestroy() {
    	/*
    	if (backgroundDrawable != null &&
    			backgroundDrawable instanceof BitmapDrawable)
    	    ((BitmapDrawable) backgroundDrawable).getBitmap().recycle();
    	if (oldBitmapDrawable != null)
    		oldBitmapDrawable.getBitmap().recycle();
    	if (currFrag != null) {
	    	Drawable drawable = currFrag.getBackground();
	    	if (drawable != null && drawable instanceof TransitionDrawable) {
	            ((BitmapDrawable)(((TransitionDrawable) drawable).getDrawable(0)))
	            		.getBitmap().recycle();
	            ((BitmapDrawable)(((TransitionDrawable) drawable).getDrawable(1)))
	    				.getBitmap().recycle();
	        }
	        else if (drawable != null && drawable instanceof BitmapDrawable)
	            ((BitmapDrawable) drawable).getBitmap().recycle();
    	}
    	*/
        if (setBackgroundTask != null)
            setBackgroundTask.cancel(true);
        if (setBackgroundWaitTask != null)
            setBackgroundWaitTask.cancel(true);
        /*
        if (setlistBackgroundWaitTask != null)
            setlistBackgroundWaitTask.cancel(true);
        */
        if (getScoreTask != null)
            getScoreTask.cancel(true);
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_setlist, menu);
        mMenu = menu;
        refreshMenu();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    
        switch (item.getItemId()) {        
        case android.R.id.home:
        	tracker.sendEvent(Constants.CATEGORY_ACTION_BAR,
        			Constants.ACTION_BUTTON_PRESS, "home", 1l);
        	if (getSlidingMenu().isMenuShowing()) {
	    		tracker.sendEvent(Constants.CATEGORY_MENU,
            			Constants.ACTION_MENU_CLOSE, "hardwareMenu", 1l);
        	}
	    	else {
	    		tracker.sendEvent(Constants.CATEGORY_MENU,
            			Constants.ACTION_MENU_OPEN, "hardwareMenu", 1l);
	    	}
            if (currFrag != null) {
                if (currFrag instanceof FragmentPager) {
                    if (currFrag.getPage() == 1)
                        currFrag.setPage(0);
                    else
                        toggle();
                }
                else
                    toggle();
            }
            return true;
        case R.id.ShareMenu:
        	tracker.sendEvent(Constants.CATEGORY_ACTION_BAR,
        			Constants.ACTION_BUTTON_PRESS, "share", 1l);
            ApplicationEx.showShortToast("Capturing screen");
            if (screenshotTask != null)
                screenshotTask.cancel(true);
            screenshotTask = new ScreenshotTask(true);
            if (Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.HONEYCOMB)
                screenshotTask.execute();
            else
                screenshotTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return true;
        case R.id.SetlistMenu:
        	tracker.sendEvent(Constants.CATEGORY_ACTION_BAR,
        			Constants.ACTION_BUTTON_PRESS, "setlist", 1l);
            if (currFrag instanceof FragmentPager)
                ((FragmentPager)currFrag).setPage(1);
            return true;
        default:            
            return super.onOptionsItemSelected(item);    
        }
    }
    
    private class ScreenshotTask extends AsyncTask<Void, Void, Void> {
    	private boolean isSetlist = false;
    	
    	private ScreenshotTask(boolean isSetlist) {
    		this.isSetlist = isSetlist;
    	}
    	
        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            shareScreenshot(isSetlist);
        }
    }
    
    private class LogoutWaitTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            DatabaseHelperSingleton.instance().setOffset(0, getUserId());
            setLoggingOut(true);
            clearQuestionIds();
            clearQuestions();
            clearQuestionAnswers();
            clearQuestionCategories();
            clearQuestionScores();
            clearQuestionHints();
            clearQuestionSkips();
            updatePersistedLists();
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            showLogin();
            //getSlidingMenu().showContent();
        }
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	tracker.sendEvent(Constants.CATEGORY_HARDWARE,
        			Constants.ACTION_BUTTON_PRESS, "menu", 1l);
	    	if (getSlidingMenu().isMenuShowing()) {
	    		tracker.sendEvent(Constants.CATEGORY_MENU,
            			Constants.ACTION_MENU_CLOSE, "hardwareMenu", 1l);
	    	}
	    	else {
	    		tracker.sendEvent(Constants.CATEGORY_MENU,
            			Constants.ACTION_MENU_OPEN, "hardwareMenu", 1l);
	    	}
	        if (currFrag != null) {
	            if (currFrag instanceof FragmentPager) {
                    if (currFrag.getPage() == 1)
                        currFrag.setPage(0);
                    else
                        toggle();
	            }
	            else
	                toggle();
            }
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
    /**
     * Sliding menu methods and associated methods/classes
     */
    private void refreshSlidingMenu() {
        if (!loggedIn) {
            setBehindContentView(R.layout.menu_splash);
            getSlidingMenu().setMode(SlidingMenu.LEFT);
        	switch(ResourcesSingleton.instance().getConfiguration().orientation) {
	        case Configuration.ORIENTATION_LANDSCAPE:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
	            break;
	        default:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
	            break;
	        }
            infoButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.InfoButton);
            infoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "splashInfo", 1l);
                    if (getSlidingMenu().isMenuShowing()) {
                    	getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                            	getSlidingMenu().setOnClosedListener(null);
                                Thread infoThread = new Thread() {
                                    public void run() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {}
                                        onInfoPressed();
                                    }
                                };
                                infoThread.start();
                            }
                        });
                    	getSlidingMenu().showContent();
                    }
                }
            });
            /*
            switchButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.SwitchButton);
            switchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    switchButton.setEnabled(false);
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                setBackground(getSplashBackground(), true, "splash");
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            switchButton.setEnabled(true);
            */
            exitButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.ExitButton);
            exitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "splashExit", 1l);
                    if (getSlidingMenu().isMenuShowing())
                        moveTaskToBack(true);
                }
            });
            notificationSoundButton = (RelativeLayout) getSlidingMenu().findViewById(
                    R.id.NotificationSoundsButton);
            notificationSoundImage = (ImageViewEx) getSlidingMenu().findViewById(
                    R.id.NotificationSoundsImage);
            notificationSoundText = (TextView) getSlidingMenu().findViewById(
                    R.id.NotificationSoundsText);
            int soundSetting = SharedPreferencesSingleton.instance().getInt(
            		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0);
            notificationSoundImage.setImageLevel(soundSetting);
            notificationAlbumButton = (RelativeLayout) getSlidingMenu().findViewById(
                    R.id.NotificationTypeButton);
            notificationAlbumImage = (ImageViewEx) getSlidingMenu().findViewById(
                    R.id.NotificationTypeImage);
            notificationAlbumText = (TextView) getSlidingMenu().findViewById(
                    R.id.NotificationTypeText);
            switch (soundSetting) {
            case 0:
                notificationSoundText.setText(R.string.NotificationBothTitle);
                notificationAlbumButton.setEnabled(true);
                notificationAlbumImage.setEnabled(true);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                notificationAlbumText.setEnabled(true);
                break;
            case 1:
                notificationSoundText.setText(R.string.NotificationSoundsTitle);
                notificationAlbumButton.setEnabled(true);
                notificationAlbumImage.setEnabled(true);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                notificationAlbumText.setEnabled(true);
                break;
            case 2:
                notificationSoundText.setText(R.string.NotificationVibrateTitle);
                notificationAlbumButton.setEnabled(false);
                notificationAlbumImage.setEnabled(false);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                notificationAlbumText.setEnabled(false);
                break;
            case 3:
                notificationSoundText.setText(R.string.NotificationSilentTitle);
                notificationAlbumButton.setEnabled(false);
                notificationAlbumImage.setEnabled(false);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                notificationAlbumText.setEnabled(false);
                break;
            }
            notificationSoundButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        int soundSetting = SharedPreferencesSingleton.instance().getInt(
                        		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0);
                        switch (soundSetting) {
                        case 0:
                        	notificationSoundText.setText(R.string.NotificationSilentTitle);
                        	notificationAlbumButton.setEnabled(false);
                            notificationAlbumImage.setEnabled(false);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                            notificationAlbumText.setEnabled(false);
                            soundSetting = 3;
                            break;
                        case 1:
                        	notificationSoundText.setText(R.string.NotificationBothTitle);
                        	notificationAlbumButton.setEnabled(true);
                            notificationAlbumImage.setEnabled(true);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                            notificationAlbumText.setEnabled(true);
                            soundSetting = 0;
                            break;
                        case 2:
                        	notificationSoundText.setText(R.string.NotificationSoundsTitle);
                        	notificationAlbumButton.setEnabled(true);
                            notificationAlbumImage.setEnabled(true);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                            notificationAlbumText.setEnabled(true);
                            soundSetting = 1;
                            break;
                        case 3:
                        	notificationSoundText.setText(R.string.NotificationVibrateTitle);
                        	notificationAlbumButton.setEnabled(false);
                            notificationAlbumImage.setEnabled(false);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                            notificationAlbumText.setEnabled(false);
                            soundSetting = 2;
                            break;
                        }
                        notificationSoundImage.setImageLevel(soundSetting);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationsound_key),
                                    soundSetting)
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationsound_key),
                                    soundSetting)
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS,
                    			"splashNotificationSound", (long) soundSetting);
                    }
                }
            });
            int typeSetting = SharedPreferencesSingleton.instance().getInt(
            		ResourcesSingleton.instance().getString(R.string.notificationtype_key), 0);
            notificationAlbumImage.setImageLevel(typeSetting);
            switch (typeSetting) {
            case 0:
                notificationAlbumText.setText(R.string.NotificationTypeStandardTitle);
                break;
            case 1:
                notificationAlbumText.setText(R.string.NotificationTypeAlbumTitle);
                break;
            /*
            case 2:
                notificationAlbumText.setText(R.string.NotificationTypeSongTitle);
                break;
            */
            }
            notificationAlbumButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        int typeSetting = SharedPreferencesSingleton.instance().getInt(
                        		ResourcesSingleton.instance().getString(R.string.notificationtype_key), 0);
                        switch (typeSetting) {
                        case 0:
                            notificationAlbumText.setText(R.string.NotificationTypeAlbumTitle);
                            typeSetting = 1;
                            break;
                        case 1:
                            notificationAlbumText.setText(R.string.NotificationTypeStandardTitle);
                            typeSetting = 0;
                            /*
                            if (DatabaseHelperSingleton.instance()
                            		.getNotificatationsToDownload() != null)
                            	showDownloadDialog();
                        	*/
                            break;
                        /*
                        case 2:
                            notificationAlbumText.setText(R.string.NotificationTypeStandardTitle);
                            typeSetting = 0;
                            break;
                        */
                        }
                        notificationAlbumImage.setImageLevel(typeSetting);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationtype_key),
                            		typeSetting)
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationtype_key),
                            		typeSetting)
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS,
                    			"splashNotificationType", (long) typeSetting);
                    }
                }
            });
            notificationsButton = (RelativeLayout) getSlidingMenu().findViewById(
                    R.id.NotificationsButton);
            notificationsText = (CheckedTextView) getSlidingMenu().findViewById(
                    R.id.NotificationsText);
            notificationsText.setChecked(SharedPreferencesSingleton.instance().getBoolean(
            		ResourcesSingleton.instance().getString(R.string.notification_key), true));
            if (notificationSoundButton != null &&
                    notificationAlbumButton != null) {
                if (!notificationsText.isChecked()) {
                    notificationSoundButton.setEnabled(false);
                    notificationSoundImage.setEnabled(false);
                    notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                    notificationAlbumButton.setEnabled(false);
                    notificationAlbumImage.setEnabled(false);
                    notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                    notificationAlbumText.setEnabled(false);
                }
                else {
                	notificationSoundButton.setEnabled(true);
                    notificationSoundImage.setEnabled(true);
                    notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                    switch (SharedPreferencesSingleton.instance().getInt(
                		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0)) {
                    case 0:
                    case 1:
                    	notificationAlbumButton.setEnabled(true);
                        notificationAlbumImage.setEnabled(true);
                        notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                        notificationAlbumText.setEnabled(true);
                    	break;
                    case 2:
                    case 3:
                    	notificationAlbumButton.setEnabled(false);
                        notificationAlbumImage.setEnabled(false);
                        notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                        notificationAlbumText.setEnabled(false);
                    	break;
                    }
                }
            }
            notificationsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        notificationsText.toggle();
                        if (notificationSoundButton != null &&
                                notificationAlbumButton != null) {
                            if (!notificationsText.isChecked()) {
                                notificationSoundButton.setEnabled(false);
                                notificationSoundImage.setEnabled(false);
                                notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                                notificationAlbumButton.setEnabled(false);
                                notificationAlbumImage.setEnabled(false);
                                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                                notificationAlbumText.setEnabled(false);
                            }
                            else {
                                notificationSoundButton.setEnabled(true);
                                notificationSoundImage.setEnabled(true);
                                notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                                switch (SharedPreferencesSingleton.instance().getInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0)) {
                                case 0:
                                case 1:
                                	notificationAlbumButton.setEnabled(true);
                                    notificationAlbumImage.setEnabled(true);
                                    notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                                    notificationAlbumText.setEnabled(true);
                                	break;
                                case 2:
                                case 3:
                                	notificationAlbumButton.setEnabled(false);
                                    notificationAlbumImage.setEnabled(false);
                                    notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                                    notificationAlbumText.setEnabled(false);
                                	break;
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putBoolean(
                            		ResourcesSingleton.instance().getString(R.string.notification_key),
                                    notificationsText.isChecked())
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putBoolean(
                            		ResourcesSingleton.instance().getString(R.string.notification_key),
                                    notificationsText.isChecked())
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS,
                    			"splashNotificationToggle",
                    			notificationsText.isChecked() ? 1l : 0l);
                    }
                }
            });
        }
        else if (!inStats) {
            setBehindContentView(R.layout.menu_quiz);
            getSlidingMenu().setMode(SlidingMenu.LEFT);
        	switch(ResourcesSingleton.instance().getConfiguration().orientation) {
	        case Configuration.ORIENTATION_LANDSCAPE:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
	            break;
	        default:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
	            break;
	        }
            statsButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.StatsButton);
            if (DatabaseHelperSingleton.instance().isAnonUser(userId))
            	statsButton.setVisibility(View.GONE);
            else {
            	statsButton.setVisibility(View.VISIBLE);
	            statsButton.setOnClickListener(new OnClickListener() {
	                @Override
	                public void onClick(View arg0) {
	                	tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS, "quizStats", 1l);
	                    openStats();
	                }
	            });
            }
            /*
            switchButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.SwitchButton);
            switchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    switchButton.setEnabled(false);
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                setBackground(getQuizBackground(), true, "quiz");
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            switchButton.setEnabled(true);
            */
            reportButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.ReportButton);
            reportButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizReport", 1l);
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                Thread shareThread = new Thread() {
                                    public void run() {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {}
                                        ApplicationEx.reportQuestion(getQuestionId(0), getQuestion(0),
                                                getQuestionAnswer(0), getQuestionScore(0));
                                    }
                                };
                                shareThread.start();
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            shareButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.ShareButton);
            shareButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizShare", 1l);
                    if (getSlidingMenu().isMenuShowing()) {
                        ApplicationEx.showShortToast("Capturing screen");
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                if (screenshotTask != null)
                                    screenshotTask.cancel(true);
                                screenshotTask = new ScreenshotTask(false);
                                if (Build.VERSION.SDK_INT <
                                        Build.VERSION_CODES.HONEYCOMB)
                                    screenshotTask.execute();
                                else
                                    screenshotTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            logoutButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.LogoutButton);
            //logoutText = (TextView) findViewById(R.id.LogoutText);
            /*
            nameButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.NameButton);
            nameButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing())
                        showNameDialog();
                }
            });
            if (getUserId() != null) {
                if (DatabaseHelperSingleton.instance().hasUser(getUserId()) &&
                        !DatabaseHelperSingleton.instance().isAnonUser(getUserId())) {
                    if (getDisplayName() != null)
                        logoutText.setText("Logout (" + getDisplayName() + ")");
                    statsButton.setVisibility(View.VISIBLE);
                    nameButton.setVisibility(View.VISIBLE);
                }
                else {
                    statsButton.setVisibility(View.GONE);
                    nameButton.setVisibility(View.GONE);
                }
            }
            else {
                statsButton.setVisibility(View.GONE);
                nameButton.setVisibility(View.GONE);
            }
            */
            logoutButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizLogout", 1l);
                	if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                if (logoutWaitTask != null)
                                	logoutWaitTask.cancel(true);
                                logoutWaitTask = new LogoutWaitTask();
                                if (Build.VERSION.SDK_INT <
                                        Build.VERSION_CODES.HONEYCOMB)
                                	logoutWaitTask.execute();
                                else
                                	logoutWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            exitButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.ExitButton);
            exitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizExit", 1l);
                    if (getSlidingMenu().isMenuShowing())
                        moveTaskToBack(true);
                }
            });
            /*
            levelButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.LevelButton);
            levelImage = (ImageViewEx) getSlidingMenu().findViewById(R.id.LevelImage);
            levelText = (TextView) getSlidingMenu().findViewById(R.id.LevelText);
            switch (SharedPreferencesSingleton.instance().getInt(
                    res.getString(R.string.level_key),Constants.HARD)) {
            case Constants.EASY:
                levelText.setText(res.getString(R.string.LevelTitle) + " (Easy)");
                levelImage.setImageResource(R.drawable.ic_level_easy_inverse);
                break;
            case Constants.MEDIUM:
                levelText.setText(res.getString(R.string.LevelTitle) + " (Medium)");
                levelImage.setImageResource(R.drawable.ic_level_med_inverse);
                break;
            case Constants.HARD:
                levelText.setText(res.getString(R.string.LevelTitle) + " (Hard)");
                levelImage.setImageResource(R.drawable.ic_level_hard_inverse);
                break;
            }
            levelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        switch (SharedPreferencesSingleton.instance().getInt(
                            res.getString(R.string.level_key), Constants.HARD)) {
                        case Constants.EASY:
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                                SharedPreferencesSingleton.instance().edit().putInt(
                                        res.getString(R.string.level_key),
                                        Constants.MEDIUM).commit();
                            else
                                SharedPreferencesSingleton.instance().edit().putInt(
                                        res.getString(R.string.level_key),
                                        Constants.MEDIUM).apply();
                            levelText.setText(res.getString(R.string.LevelTitle) +
                                    " (Medium)");
                            levelImage.setImageResource(R.drawable.ic_level_med_inverse);
                            break;
                        case Constants.MEDIUM:
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                                SharedPreferencesSingleton.instance().edit().putInt(
                                        res.getString(R.string.level_key),
                                        Constants.HARD).commit();
                            else
                                SharedPreferencesSingleton.instance().edit().putInt(
                                        res.getString(R.string.level_key),
                                        Constants.HARD).apply();
                            levelText.setText(res.getString(R.string.LevelTitle) +
                                    " (Hard)");
                            levelImage.setImageResource(R.drawable.ic_level_hard_inverse);
                            break;
                        case Constants.HARD:
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                                SharedPreferencesSingleton.instance().edit().putInt(
                                        res.getString(R.string.level_key),
                                        Constants.EASY).commit();
                            else
                                SharedPreferencesSingleton.instance().edit().putInt(
                                        res.getString(R.string.level_key),
                                        Constants.EASY).apply();
                            levelText.setText(res.getString(R.string.LevelTitle) +
                                    " (Easy)");
                            levelImage.setImageResource(R.drawable.ic_level_easy_inverse);
                            break;
                        }
                        if (getQuestion() == null) {
                            getNextQuestions(false,
                                    SharedPreferencesSingleton.instance().getInt(
                                    res.getString(R.string.level_key),
                                    Constants.HARD));
                            if (currFrag != null)
                                currFrag.showRetry();
                        }
                    }
                }
            });
            */
            soundsButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.SoundsButton);
            soundsText = (CheckedTextView) getSlidingMenu().findViewById(R.id.SoundsText);
            soundsText.setChecked(SharedPreferencesSingleton.instance().getBoolean(
            		ResourcesSingleton.instance().getString(R.string.sound_key), true));
            soundsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        soundsText.toggle();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putBoolean(
                            		ResourcesSingleton.instance().getString(R.string.sound_key),
                                    !SharedPreferencesSingleton.instance().getBoolean(
                                    		ResourcesSingleton.instance().getString(R.string.sound_key), true))
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putBoolean(
                            		ResourcesSingleton.instance().getString(R.string.sound_key),
                                    !SharedPreferencesSingleton.instance().getBoolean(
                                    		ResourcesSingleton.instance().getString(R.string.sound_key), true))
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS, "quizSounds",
                    			soundsText.isChecked() ? 1l : 0l);
                    }
                }
            });
            notificationSoundButton = (RelativeLayout) getSlidingMenu().findViewById(
                    R.id.NotificationSoundsButton);
            notificationSoundImage = (ImageViewEx) getSlidingMenu().findViewById(
                    R.id.NotificationSoundsImage);
            notificationSoundText = (TextView) getSlidingMenu().findViewById(
                    R.id.NotificationSoundsText);
            int soundSetting = SharedPreferencesSingleton.instance().getInt(
            		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0);
            notificationSoundImage.setImageLevel(soundSetting);
            notificationAlbumButton = (RelativeLayout) getSlidingMenu().findViewById(
                    R.id.NotificationTypeButton);
            notificationAlbumImage = (ImageViewEx) getSlidingMenu().findViewById(
                    R.id.NotificationTypeImage);
            notificationAlbumText = (TextView) getSlidingMenu().findViewById(
                    R.id.NotificationTypeText);
            switch (soundSetting) {
            case 0:
                notificationSoundText.setText(R.string.NotificationBothTitle);
                notificationAlbumButton.setEnabled(true);
                notificationAlbumImage.setEnabled(true);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                notificationAlbumText.setEnabled(true);
                break;
            case 1:
                notificationSoundText.setText(R.string.NotificationSoundsTitle);
                notificationAlbumButton.setEnabled(true);
                notificationAlbumImage.setEnabled(true);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                notificationAlbumText.setEnabled(true);
                break;
            case 2:
                notificationSoundText.setText(R.string.NotificationVibrateTitle);
                notificationAlbumButton.setEnabled(false);
                notificationAlbumImage.setEnabled(false);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                notificationAlbumText.setEnabled(false);
                break;
            case 3:
                notificationSoundText.setText(R.string.NotificationSilentTitle);
                notificationAlbumButton.setEnabled(false);
                notificationAlbumImage.setEnabled(false);
                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                notificationAlbumText.setEnabled(false);
                break;
            }
            notificationSoundButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        int soundSetting = SharedPreferencesSingleton.instance().getInt(
                        		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0);
                        switch (soundSetting) {
                        case 0:
                        	notificationSoundText.setText(R.string.NotificationSilentTitle);
                        	notificationAlbumButton.setEnabled(false);
                            notificationAlbumImage.setEnabled(false);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                            notificationAlbumText.setEnabled(false);
                            soundSetting = 3;
                            break;
                        case 1:
                        	notificationSoundText.setText(R.string.NotificationBothTitle);
                        	notificationAlbumButton.setEnabled(true);
                            notificationAlbumImage.setEnabled(true);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                            notificationAlbumText.setEnabled(true);
                            soundSetting = 0;
                            break;
                        case 2:
                        	notificationSoundText.setText(R.string.NotificationSoundsTitle);
                        	notificationAlbumButton.setEnabled(true);
                            notificationAlbumImage.setEnabled(true);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                            notificationAlbumText.setEnabled(true);
                            soundSetting = 1;
                            break;
                        case 3:
                        	notificationSoundText.setText(R.string.NotificationVibrateTitle);
                        	notificationAlbumButton.setEnabled(false);
                            notificationAlbumImage.setEnabled(false);
                            notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                            notificationAlbumText.setEnabled(false);
                            soundSetting = 2;
                            break;
                        }
                        notificationSoundImage.setImageLevel(soundSetting);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationsound_key),
                                    soundSetting)
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationsound_key),
                                    soundSetting)
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS,
                    			"quizNotificationSound", (long) soundSetting);
                    }
                }
            });
            int typeSetting = SharedPreferencesSingleton.instance().getInt(
            		ResourcesSingleton.instance().getString(R.string.notificationtype_key), 0);
            notificationAlbumImage.setImageLevel(typeSetting);
            switch (typeSetting) {
            case 0:
                notificationAlbumText.setText(R.string.NotificationTypeStandardTitle);
                break;
            case 1:
                notificationAlbumText.setText(R.string.NotificationTypeAlbumTitle);
                break;
            /*
            case 2:
                notificationAlbumText.setText(R.string.NotificationTypeSongTitle);
                break;
            */
            }
            notificationAlbumButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        int typeSetting = SharedPreferencesSingleton.instance().getInt(
                        		ResourcesSingleton.instance().getString(R.string.notificationtype_key), 0);
                        switch (typeSetting) {
                        case 0:
                            notificationAlbumText.setText(R.string.NotificationTypeAlbumTitle);
                            typeSetting = 1;
                            break;
                        case 1:
                            notificationAlbumText.setText(R.string.NotificationTypeStandardTitle);
                            typeSetting = 0;
                            /*
                            if (DatabaseHelperSingleton.instance()
                            		.getNotificatationsToDownload() != null)
                            	showDownloadDialog();
                        	*/
                            break;
                        /*
                        case 2:
                            notificationAlbumText.setText(R.string.NotificationTypeStandardTitle);
                            typeSetting = 0;
                            break;
                        */
                        }
                        notificationAlbumImage.setImageLevel(typeSetting);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationtype_key),
                            		typeSetting)
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationtype_key),
                            		typeSetting)
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS,
                    			"quizNotificationType", (long) typeSetting);
                    }
                }
            });
            notificationsButton = (RelativeLayout) getSlidingMenu().findViewById(
                    R.id.NotificationsButton);
            notificationsText = (CheckedTextView) getSlidingMenu().findViewById(
                    R.id.NotificationsText);
            notificationsText.setChecked(SharedPreferencesSingleton.instance().getBoolean(
            		ResourcesSingleton.instance().getString(R.string.notification_key), true));
            if (notificationSoundButton != null &&
                    notificationAlbumButton != null) {
                if (!notificationsText.isChecked()) {
                    notificationSoundButton.setEnabled(false);
                    notificationSoundImage.setEnabled(false);
                    notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                    notificationAlbumButton.setEnabled(false);
                    notificationAlbumImage.setEnabled(false);
                    notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                    notificationAlbumText.setEnabled(false);
                }
                else {
                	notificationSoundButton.setEnabled(true);
                    notificationSoundImage.setEnabled(true);
                    notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                    switch (SharedPreferencesSingleton.instance().getInt(
                		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0)) {
                    case 0:
                    case 1:
                    	notificationAlbumButton.setEnabled(true);
                        notificationAlbumImage.setEnabled(true);
                        notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                        notificationAlbumText.setEnabled(true);
                    	break;
                    case 2:
                    case 3:
                    	notificationAlbumButton.setEnabled(false);
                        notificationAlbumImage.setEnabled(false);
                        notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                        notificationAlbumText.setEnabled(false);
                    	break;
                    }
                }
            }
            notificationsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getSlidingMenu().isMenuShowing()) {
                        notificationsText.toggle();
                        if (notificationSoundButton != null &&
                                notificationAlbumButton != null) {
                            if (!notificationsText.isChecked()) {
                                notificationSoundButton.setEnabled(false);
                                notificationSoundImage.setEnabled(false);
                                notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                                notificationAlbumButton.setEnabled(false);
                                notificationAlbumImage.setEnabled(false);
                                notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                                notificationAlbumText.setEnabled(false);
                            }
                            else {
                                notificationSoundButton.setEnabled(true);
                                notificationSoundImage.setEnabled(true);
                                notificationSoundText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                                switch (SharedPreferencesSingleton.instance().getInt(
                            		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0)) {
                                case 0:
                                case 1:
                                	notificationAlbumButton.setEnabled(true);
                                    notificationAlbumImage.setEnabled(true);
                                    notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(android.R.color.white));
                                    notificationAlbumText.setEnabled(true);
                                	break;
                                case 2:
                                case 3:
                                	notificationAlbumButton.setEnabled(false);
                                    notificationAlbumImage.setEnabled(false);
                                    notificationAlbumText.setTextColor(ResourcesSingleton.instance().getColor(R.color.dark_gray));
                                    notificationAlbumText.setEnabled(false);
                                	break;
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                            SharedPreferencesSingleton.instance().edit().putBoolean(
                            		ResourcesSingleton.instance().getString(R.string.notification_key),
                                    notificationsText.isChecked())
                            .commit();
                        else
                            SharedPreferencesSingleton.instance().edit().putBoolean(
                            		ResourcesSingleton.instance().getString(R.string.notification_key),
                                    notificationsText.isChecked())
                            .apply();
                        tracker.sendEvent(Constants.CATEGORY_MENU,
                    			Constants.ACTION_BUTTON_PRESS,
                    			"quizNotificationToggle",
                    			notificationsText.isChecked() ? 1l : 0l);
                    }
                }
            });
        }
        else if (inStats) {
            setBehindContentView(R.layout.menu_leaders);
            getSlidingMenu().setMode(SlidingMenu.LEFT);
        	switch(ResourcesSingleton.instance().getConfiguration().orientation) {
	        case Configuration.ORIENTATION_LANDSCAPE:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
	            break;
	        default:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
	            break;
	        }
            shareButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.ShareButton);
            shareButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsShare", 1l);
                    if (getSlidingMenu().isMenuShowing()) {
                        ApplicationEx.showShortToast("Capturing screen");
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                if (screenshotTask != null)
                                    screenshotTask.cancel(true);
                                screenshotTask = new ScreenshotTask(false);
                                if (Build.VERSION.SDK_INT <
                                        Build.VERSION_CODES.HONEYCOMB)
                                    screenshotTask.execute();
                                else
                                    screenshotTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            nameButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.NameButton);
            nameButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsName", 1l);
                    if (getSlidingMenu().isMenuShowing())
                        showNameDialog();
                }
            });
            infoButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.InfoButton);
            infoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsInfo", 1l);
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                Thread infoThread = new Thread() {
                                    public void run() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {}
                                        onFaqPressed();
                                    }
                                };
                                infoThread.start();
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            exitButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.ExitButton);
            exitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsExit", 1l);
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                inStats = false;
                                DatabaseHelperSingleton.instance().setUserValue(inStats ? 1 : 0,
                                        DatabaseHelper.COL_IN_STATS, userId);
                                showQuiz(true, false);
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            /*
            switchButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.SwitchButton);
            switchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    switchButton.setEnabled(false);
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                            @Override
                            public void onClosed() {
                                getSlidingMenu().setOnClosedListener(null);
                                setBackground(getLeadersBackground(), true, "leaders");
                            }
                        });
                        getSlidingMenu().showContent();
                    }
                }
            });
            switchButton.setEnabled(true);
            */
        }
        /*
        tipsButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.QuickTipsButton);
        tipsText = (CheckedTextView) getSlidingMenu().findViewById(R.id.QuickTipsText);
        tipsText.setChecked(SharedPreferencesSingleton.instance().getBoolean(
                res.getString(R.string.quicktip_key), false));
        tipsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (getSlidingMenu().isMenuShowing()) {
                    tipsText.toggle();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                        SharedPreferencesSingleton.instance().edit().putBoolean(
                                res.getString(R.string.quicktip_key),
                                !SharedPreferencesSingleton.instance().getBoolean(
                                        res.getString(R.string.quicktip_key), true))
                        .commit();
                    else
                        SharedPreferencesSingleton.instance().edit().putBoolean(
                                res.getString(R.string.quicktip_key),
                                !SharedPreferencesSingleton.instance().getBoolean(
                                        res.getString(R.string.quicktip_key), true))
                        .apply();
                }
            }
        });
        */
        followButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.FollowButton);
        followButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	if (!loggedIn)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "splashFollow", 1l);
            	else if (!inStats)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizFollow", 1l);
            	else if (inStats)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsFollow", 1l);
                if (getSlidingMenu().isMenuShowing())
                    startActivity(getOpenTwitterIntent());
            }
        });
        likeButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.LikeButton);
        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	if (!loggedIn)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "splashLike", 1l);
            	else if (!inStats)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizLike", 1l);
            	else if (inStats)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsLike", 1l);
                if (getSlidingMenu().isMenuShowing())
                    startActivity(getOpenFacebookIntent());
            }
        });
        /*
        quickTipView = (ViewGroup) getLayoutInflater().inflate(
                R.layout.quicktip_menu,
                (ViewGroup) findViewById(R.id.ToolTipLayout));
        quickTipMenuView = (ViewGroup) getLayoutInflater().inflate(
                R.layout.quicktip_menu,
                (ViewGroup) findViewById(R.id.ToolTipLayout));
        quickTipLeftView = (ViewGroup) getLayoutInflater().inflate(
                R.layout.quicktip_left,
                (ViewGroup) findViewById(R.id.ToolTipLayout));
        */
        switchButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.SwitchButton);
        switchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	if (!loggedIn)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "splashSwitch", 1l);
            	else if (!inStats)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "quizSwitch", 1l);
            	else if (inStats)
            		tracker.sendEvent(Constants.CATEGORY_MENU,
                			Constants.ACTION_BUTTON_PRESS, "statsSwitch", 1l);
                switchButton.setEnabled(false);
                if (getSlidingMenu().isMenuShowing()) {
                    getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                        @Override
                        public void onClosed() {
                            getSlidingMenu().setOnClosedListener(null);
                            setBackground(getBackground(), true, "load");
                        }
                    });
                    getSlidingMenu().showContent();
                }
            }
        });
        switchButton.setEnabled(true);
        if (inSetlist) {
        	setBehindContentView(R.layout.menu_setlist);
        	getSlidingMenu().setMode(SlidingMenu.RIGHT);
        	switch(ResourcesSingleton.instance().getConfiguration().orientation) {
	        case Configuration.ORIENTATION_LANDSCAPE:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_setlist_width_land);
	            break;
	        default:
	        	getSlidingMenu().setBehindOffsetRes(R.dimen.menu_setlist_width_port);
	            break;
	        }
            setlistListView = (ExpandableListView) getSlidingMenu().findViewById(R.id.SetlistList);
            setlistListView.setOnGroupExpandListener(new OnGroupExpandListener() {
				@Override
				public void onGroupExpand(int groupPosition) {
					if (!expandedGroups.contains(groupPosition)) {
						expandedGroups.add(groupPosition);
					}
				}
            });
            setlistListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
				@Override
				public void onGroupCollapse(int groupPosition) {
					expandedGroups.remove((Integer) groupPosition);
				}
            });
            setlistListView.setOnChildClickListener(
            		new OnChildClickListener() {
    			@Override
    			public boolean onChildClick(ExpandableListView parent, View view,
    					int groupPos, int childPos, long id) {
    				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
    		            SharedPreferencesSingleton.instance().edit().putInt(
    		            		ResourcesSingleton.instance().getString(
    		            				R.string.selected_group_key),
    		                    groupPos)
    		            .commit();
    		            SharedPreferencesSingleton.instance().edit().putInt(
    		            		ResourcesSingleton.instance().getString(
    		            				R.string.selected_child_key),
    		                    childPos)
    		            .commit();
    		    	}
    		        else {
    		        	SharedPreferencesSingleton.instance().edit().putInt(
    		            		ResourcesSingleton.instance().getString(
    		            				R.string.selected_group_key),
    		                    groupPos)
    		            .apply();
    		        	SharedPreferencesSingleton.instance().edit().putInt(
    		            		ResourcesSingleton.instance().getString(
    		            				R.string.selected_child_key),
    		                    childPos)
    		            .apply();
    		        }
    				SetlistAdapter setlistAdapter = 
    						(SetlistAdapter) setlistListView.getExpandableListAdapter();
    				setlistAdapter.notifyDataSetChanged();
    				String setlist = setlistAdapter.getSetlist(groupPos,
    						childPos);
    				currFrag.setSetlistText(setlist);
    				if (setlistAdapter.getGroupCount()-1 == groupPos &&
						setlistAdapter.getChildrenCount(groupPos)-1 == childPos) {
    					//currFrag.updateSetText();
    					currFrag.setSetlistStampVisible(true);
    					saveSet(setlist, false);
    				}
    				else {
    					currFrag.setSetlistStampVisible(false);
    					saveSet(setlist, true);
    				}
    				getSlidingMenu().showContent();
    				return true;
    			}
            });
            setlistProgress = (ProgressBar) getSlidingMenu().findViewById(R.id.SetlistProgress);
            if (setlistMap.isEmpty()) {
	            parseTask = new ParseTask();
	            if (Build.VERSION.SDK_INT <
	                    Build.VERSION_CODES.HONEYCOMB) {
	            	parseTask.execute();
	    		}
	            else {
	            	parseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	            }
            }
            else {
            	String setlistDate = SharedPreferencesSingleton.instance()
            			.getString(ResourcesSingleton.instance().getString(
            					R.string.set_date_key), "");
            	String setlist = SharedPreferencesSingleton.instance()
            			.getString(ResourcesSingleton.instance().getString(
            					R.string.setlist_key), "");
            	if (!setlistDate.isEmpty()) {
            		TreeMap<String, String> tempMap = setlistMap.get(
            				setlistDate.substring(0, 4));
            		tempMap.put(setlistDate, setlist);
            	}
            	SetlistAdapter setlistAdapter = new SetlistAdapter(
            			ApplicationEx.getApp(), setlistListView, setlistMap);
            	setlistListView.setAdapter(setlistAdapter);
            	for (Integer expandedGroup : expandedGroups) {
                	setlistListView.expandGroup(expandedGroup);
                }
            	setlistListView.setSelection(firstVisiblePosition);
            	setlistListView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void saveSet(String set, boolean archive) {
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            SharedPreferencesSingleton.instance().edit().putString(
            		ResourcesSingleton.instance().getString(
            				R.string.setlist_key),
                    set)
            .commit();
            SharedPreferencesSingleton.instance().edit().putBoolean(
            		ResourcesSingleton.instance().getString(
            				R.string.archive_key),
                    archive)
            .commit();
    	}
        else {
            SharedPreferencesSingleton.instance().edit().putString(
            		ResourcesSingleton.instance().getString(
            				R.string.setlist_key),
                    set)
            .apply();
            SharedPreferencesSingleton.instance().edit().putBoolean(
            		ResourcesSingleton.instance().getString(
            				R.string.archive_key),
                    archive)
            .apply();
        }
    }
    
    private class ParseTask extends AsyncTask<Void, Void,
    		TreeMap<String, TreeMap<String, String>>> {
    	@Override
    	protected void onPreExecute() {
    		setlistListView.setVisibility(View.INVISIBLE);
    		setlistProgress.setVisibility(View.VISIBLE);
    	}
    	
        @Override
        protected TreeMap<String, TreeMap<String, String>> doInBackground(Void... nothing) {
        	ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Setlist");
        	query.setLimit(1000);
        	query.addAscendingOrder("setDate");
        	List<ParseObject> setlistList = new ArrayList<ParseObject>(0);
        	List<ParseObject> tempList = new ArrayList<ParseObject>(0);
        	do {
	        	try {
	        		tempList = query.find();
	        		setlistList.addAll(tempList);
	        	} catch (ParseException e) {
	        		
	        	}
	        	query.setSkip(setlistList.size());
        	} while (!tempList.isEmpty());
        	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        	String setDate;
        	String year;
        	String setlist;
        	TreeMap<String, String> tempMap;
        	for (ParseObject set : setlistList) {
        		setDate = (String) DateFormat.format(
        				dateFormat.toLocalizedPattern(), convertToUtc(
        						set.getDate("setDate")));
        		year = setDate.substring(0, 4);
        		setlist = set.getString("set");
        		if (setlistMap.containsKey(year)) {
        			tempMap = setlistMap.get(year);
        			if (!tempMap.containsKey(setDate)) {
        				tempMap.put(setDate, setlist);
        			}
        		}
        		else {
        			tempMap = new TreeMap<String, String>(
        					String.CASE_INSENSITIVE_ORDER);
        			tempMap.put(setDate, setlist);
        			setlistMap.put(year, tempMap);
        		}
        	}
            return setlistMap;
        }
        
        protected void onProgressUpdate(Void... nothing) {}
        
        @Override
        protected void onCancelled(TreeMap<String, TreeMap<String, String>> setlistMap) {}
        
        @Override
        protected void onPostExecute(TreeMap<String, TreeMap<String, String>> setlistMap) {
        	setlistProgress.setVisibility(View.INVISIBLE);
        	if (setlistListView != null) {
        		String setlistDate = SharedPreferencesSingleton.instance()
            			.getString(ResourcesSingleton.instance().getString(
            					R.string.set_date_key), "");
            	String setlist = SharedPreferencesSingleton.instance()
            			.getString(ResourcesSingleton.instance().getString(
            					R.string.setlist_key), "");
            	if (!setlistDate.isEmpty()) {
            		TreeMap<String, String> tempMap = setlistMap.get(
            				setlistDate.substring(0, 4));
            		tempMap.put(setlistDate, setlist);
            	}
        		SetlistAdapter setlistAdapter = new SetlistAdapter(
            			ApplicationEx.getApp(), setlistListView, setlistMap);
        		setlistListView.setAdapter(setlistAdapter);
            	for (Integer expandedGroup : expandedGroups) {
                	setlistListView.expandGroup(expandedGroup);
                }
            	setlistListView.setSelection(firstVisiblePosition);
        		setlistListView.setVisibility(View.VISIBLE);
        	}
        }
    }
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
    		Locale.getDefault());
    
    private Date convertToUtc(Date date) {
    	Calendar mbCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        mbCal.setTimeInMillis(date.getTime());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    @SuppressLint("NewApi")
    protected void openStats() {
        if (getSlidingMenu().isMenuShowing()) {
            getSlidingMenu().setOnClosedListener(new OnClosedListener() {
                @Override
                public void onClosed() {
                    getSlidingMenu().setOnClosedListener(null);
                    onStatsPressed();
                }
            });
            getSlidingMenu().showContent();
            if (!SharedPreferencesSingleton.instance().contains(
            		ResourcesSingleton.instance().getString(R.string.stats_key)) ||
                SharedPreferencesSingleton.instance().getBoolean(
                		ResourcesSingleton.instance().getString(R.string.quicktip_key), false)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                    SharedPreferencesSingleton.instance().edit().putBoolean(
                    		ResourcesSingleton.instance().getString(R.string.stats_key), true).commit();
                else
                    SharedPreferencesSingleton.instance().edit().putBoolean(
                    		ResourcesSingleton.instance().getString(R.string.stats_key), true).apply();
                /*
                showQuickTipMenu(quickTipMenuView,
                        "Touch your score to enter Stats & Standings",
                        Constants.QUICK_TIP_TOP);
                */
            }
        }
    }
    
    private Intent getOpenFacebookIntent() {
        try {
            ApplicationEx.getApp().getPackageManager().getPackageInfo(
                    "com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("fb://profile/401123586629428"));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/DMBTrivia"));
        }
    }
    
    private Intent getOpenTwitterIntent() {
        Uri uri = Uri.parse("http://www.twitter.com/dmbtrivia");
        return new Intent(Intent.ACTION_VIEW, uri);
    }
    /*
    private class SetlistBackgroundWaitTask extends AsyncTask<Void, Void, Void> {
        private String name;
        private ImageViewEx background;
        
        private SetlistBackgroundWaitTask(String name, ImageViewEx background) {
            this.name = name;
            this.background = background;
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            if (isCancelled())
                return null;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (!isCancelled())
                setlistBackground(name, background);
        }
    }
    */
    
    
    
    /*
    @Override
    public String getSplashBackground() {
        return splashBackground;
    }
    
    @Override
    public String getQuizBackground() {
        return quizBackground;
    }
    
    @Override
    public String getLeadersBackground() {
        return leadersBackground;
    }
    
    @Override
    public void loadSetlist() {
        showSetlist(true);
    }

    private void showSetlist(boolean animate) {
        try {
            FragmentSetlist fSetlist = new FragmentSetlist();
            FragmentTransaction ft = fMan.beginTransaction();
            if (animate)
                ft.setCustomAnimations(R.anim.slide_in_right,
                        R.anim.slide_out_left);
            ft.replace(android.R.id.content, fSetlist, "fSetlist")
                    .commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fSetlist;
            setBackground(currentBackground, false);
            inSetlist = true;
            DatabaseHelperSingleton.instance().setUserValue(inSetlist ? 1 : 0,
                    DatabaseHelper.COL_IN_SETLIST, userId);
            invalidateOptionsMenu();
        } catch (IllegalStateException e) {}
    }
    */
    /**
     * Broadcast receiver classes
     */
    private class ConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(
                    Constants.ACTION_CONNECTION)) {
                if (intent.hasExtra("hasConnection")) {
                    if (!intent.getBooleanExtra("hasConnection", false))
                        currFrag.showNetworkProblem();
                    else
                    	ApplicationEx.getSetlist();
                }
            }
        }
    }

    /**
     * OnButtonListener implemented methods and associated methods/classes
     */
    @SuppressLint("NewApi")
	@Override
    public void setBackground(final String name, final boolean showNew,
            final String screen) {
        if (!showNew) {
            if (name == null)
                return;
            if (setBackgroundTask != null)
                setBackgroundTask.cancel(true);
            int resourceId = ResourcesSingleton.instance().getIdentifier(name, "drawable", getPackageName());
            try {
                if (name.equals("setlist")) {
                    ApplicationEx.setSetlistBitmap(getBitmap(resourceId));
                    if (currFrag != null) {
                        currFrag.setBackground(ApplicationEx.getSetlistBitmap());
                    }
                }
                else {
                    if (fieldsList.indexOf(resourceId) >= 0)
                        ApplicationEx.setBackgroundBitmap(getBitmap(resourceId));
                    else
                        ApplicationEx.setBackgroundBitmap(getBitmap(R.drawable.splash4));
                    if (currFrag != null) {
                        currFrag.setBackground(ApplicationEx.getBackgroundBitmap());
                    }
                }
            } catch (RuntimeException err) {
                Log.e(Constants.LOG_TAG, "Failed to set background!", err);
                if (setBackgroundWaitTask != null)
                    setBackgroundWaitTask.cancel(true);
                setBackgroundWaitTask = new SetBackgroundWaitTask(name, showNew, screen);
                if (Build.VERSION.SDK_INT <
                        Build.VERSION_CODES.HONEYCOMB)
                    setBackgroundWaitTask.execute();
                else
                    setBackgroundWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError memErr) {
                ApplicationEx.showShortToast("Error setting background");
                /*
                if (setBackgroundWaitTask != null)
                    setBackgroundWaitTask.cancel(true);
                setBackgroundWaitTask = new SetBackgroundWaitTask(name, showNew, screen);
                if (Build.VERSION.SDK_INT <
                        Build.VERSION_CODES.HONEYCOMB)
                    setBackgroundWaitTask.execute();
                else
                    setBackgroundWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                */
            }
        }
        else {
        	if (setBackgroundTask != null)
        		setBackgroundTask.cancel(true);
        	setBackgroundTask = new SetBackgroundTask(name, showNew, screen, null);
            if (Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.HONEYCOMB)
            	setBackgroundTask.execute();
            else
            	setBackgroundTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    
    private class SetBackgroundTask extends AsyncTask<Void, Void, Void> {
        private String name;
        private boolean showNew;
        private String screen;
        
        private int currentId;
        private int resourceId;
        
        private SetBackgroundTask(String name, boolean showNew, String screen,
                ImageViewEx background) {
            this.name = name;
            this.showNew = showNew;
            this.screen = screen;
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            if (isCancelled())
                return null;
            if (name == null)
                name = "splash4";
            resourceId = ResourcesSingleton.instance().getIdentifier(name, "drawable", getPackageName());
            if (isCancelled())
                return null;
            if (showNew) {
                rawIndex = fieldsList.indexOf(resourceId);
                if (rawIndex < 0)
                    rawIndex = fieldsList.indexOf(R.drawable.splash4);
                rawIndex++;
                if (rawIndex >= fieldsList.size())
                    rawIndex = 0;
                currentId = fieldsList.get(rawIndex);
                if (isCancelled())
                    return null;
                switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    portBackground = ResourcesSingleton.instance().getResourceEntryName(currentId);
                    DatabaseHelperSingleton.instance().setPortBackground(userId, portBackground);
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    landBackground = ResourcesSingleton.instance().getResourceEntryName(currentId);
                    DatabaseHelperSingleton.instance().setLandBackground(userId, landBackground);
                    break;
                default:
                    portBackground = ResourcesSingleton.instance().getResourceEntryName(currentId);
                    DatabaseHelperSingleton.instance().setPortBackground(userId, portBackground);
                    break;
                }
                if (isCancelled())
                    return null;
                /*
                Log.d(Constants.LOG_TAG, "SWITCH: " + currentBackground);
                if (screen.equals("splash")) {
                    DatabaseHelperSingleton.instance().setSplashBackground(userId,
                            currentBackground);
                    splashBackground = currentBackground;
                }
                else if (screen.equals("quiz")) {
                    DatabaseHelperSingleton.instance().setQuizBackground(userId,
                            currentBackground);
                    quizBackground = currentBackground;
                }
                else if (screen.equals("leaders")) {
                    DatabaseHelperSingleton.instance().setLeadersBackground(userId,
                            currentBackground);
                    leadersBackground = currentBackground;
                }
                */
                if (isCancelled())
                    return null;
                if (currentId != resourceId) {
                    try {
                    	ApplicationEx.setBackgroundBitmap(getBitmap(currentId));
                        if (isCancelled())
                            return null;
                        /*
                        if (ApplicationEx.getBackgroundDrawable() != null &&
                        		ApplicationEx.getBackgroundDrawable() instanceof
                        			TransitionDrawable) {
                            oldBitmapDrawable = (BitmapDrawableEx)(
                            		((TransitionDrawable)ApplicationEx.getBackgroundDrawable())
                            				.getDrawable(1));
                        }
                        else if (ApplicationEx.getBackgroundDrawable() != null)
                            oldBitmapDrawable = new BitmapDrawableEx(res, ((BitmapDrawable)ApplicationEx.getBackgroundDrawable()).getBitmap());
                        if (ApplicationEx.getBackgroundDrawable() != null)
                            oldBitmapDrawable = new BitmapDrawableEx(res, ((BitmapDrawable)ApplicationEx.getBackgroundDrawable()).getBitmap());
                        if (ApplicationEx.getBackgroundDrawable() != null) {
                            arrayDrawable[0] = oldBitmapDrawable;
                            arrayDrawable[1] = tempDrawable;
                        }
                        */
                    } catch (OutOfMemoryError memErr) {
                        if (isCancelled())
                            return null;
                        ApplicationEx.showShortToast("Error switching backgrounds");
                    	//setBackground(currentBackground, showNew, screen);
                    } catch (Resources.NotFoundException e) {
                        if (isCancelled())
                            return null;
                        switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                        case Configuration.ORIENTATION_PORTRAIT:
                            setBackground(portBackground, showNew, screen);
                            break;
                        case Configuration.ORIENTATION_LANDSCAPE:
                            setBackground(landBackground, showNew, screen);
                            break;
                        default:
                            setBackground(portBackground, showNew, screen);
                            break;
                        }
                    }
                }
                else {
                    if (isCancelled())
                        return null;
                    switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                    case Configuration.ORIENTATION_PORTRAIT:
                        setBackground(portBackground, showNew, screen);
                        break;
                    case Configuration.ORIENTATION_LANDSCAPE:
                        setBackground(landBackground, showNew, screen);
                        break;
                    default:
                        setBackground(portBackground, showNew, screen);
                        break;
                    }
                }
            }
            else {
                if (isCancelled())
                    return null;
                switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    portBackground = ResourcesSingleton.instance().getResourceEntryName(currentId);
                    DatabaseHelperSingleton.instance().setPortBackground(userId, portBackground);
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    landBackground = ResourcesSingleton.instance().getResourceEntryName(currentId);
                    DatabaseHelperSingleton.instance().setLandBackground(userId, landBackground);
                    break;
                default:
                    portBackground = ResourcesSingleton.instance().getResourceEntryName(currentId);
                    DatabaseHelperSingleton.instance().setPortBackground(userId, portBackground);
                    break;
                }
                if (isCancelled())
                    return null;
                /*
                if (screen.equals("setlist")) {
                    if (isCancelled())
                        return null;
                    ApplicationEx.setSetlistDrawable(getDrawable(resourceId));
                }
                else {
                	*/
                    if (isCancelled())
                        return null;
                    if (fieldsList.indexOf(resourceId) >= 0)
                        ApplicationEx.setBackgroundBitmap(getBitmap(resourceId));
                    else
                        ApplicationEx.setBackgroundBitmap(getBitmap(R.drawable.splash4));
                //}
            }
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
        	if (currFrag != null) {
	            if (showNew) {
	                /*
	                if (currentId != resourceId && arrayDrawable[0] != null &&
	                        arrayDrawable[1] != null) {
	                    ApplicationEx.setBackgroundDrawable(
	                            new TransitionDrawable(arrayDrawable));
	                	((TransitionDrawable)ApplicationEx.getBackgroundDrawable())
	                			.setCrossFadeEnabled(true);
	                	try {
	                	    currFrag.setBackground(ApplicationEx.getBackgroundDrawable());
	                	} catch (IllegalArgumentException e) {
	                	    Log.e(Constants.LOG_TAG, "Failed to set background!", e);
	                	} catch (NullPointerException e) {}
	                    //background.setImageDrawable(transitionDrawable);
	                    ((TransitionDrawable)ApplicationEx.getBackgroundDrawable())
	                    		.startTransition(500);
	                }
	                else {
	                    try {
	                        currFrag.setBackground(((BitmapDrawable)tempDrawable).getBitmap());
	                    } catch (NullPointerException e) {}
	                }
	                */
	                try {
	                	getWindow().setBackgroundDrawable(null);
                        currFrag.setBackground(ApplicationEx.getBackgroundBitmap());
                    } catch (NullPointerException e) {}
	                    //background.setImageDrawable(tempDrawable);
	            }
	            else {
                    try {
                    	getWindow().setBackgroundDrawable(null);
	                	currFrag.setBackground(ApplicationEx.getBackgroundBitmap());
                    } catch (IllegalArgumentException e) {
                        Log.e(Constants.LOG_TAG, "Failed to set background!", e);
                    } catch (NullPointerException err) {
	                } catch (OutOfMemoryError memErr) {
	                    ApplicationEx.showShortToast("Error setting background");
	                    /*
	                    Thread backgroundThread = new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {}
                                Log.i(Constants.LOG_TAG, "SetBackgroundTask OOM");
                                setBackground(name, showNew, screen);
                            }
                        };
                        backgroundThread.start();
                        */
                    }
                }
            }
            //ApplicationEx.setBackgroundBitmap(((BitmapDrawable)currFrag.getBackground()).getBitmap());
        	switchButton.setEnabled(true);
        }
    }
    
    private class SetBackgroundWaitTask extends AsyncTask<Void, Void, Void> {
        private String name;
        private boolean showNew;
        private String screen;
        
        private SetBackgroundWaitTask(String name, boolean showNew,
                String screen) {
            this.name = name;
            this.showNew = showNew;
            this.screen = screen;
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            if (isCancelled())
                return null;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (!isCancelled()) {
                setBackground(name, showNew, screen);
            }
        }
    }
    
    @Override
    public String getBackground() {
        switch(ResourcesSingleton.instance().getConfiguration().orientation) {
        case Configuration.ORIENTATION_PORTRAIT:
            return portBackground;
        case Configuration.ORIENTATION_LANDSCAPE:
            return landBackground;
        default:
            return portBackground;
        }
    }
    
    @Override
    public void setlistBackground(final String name,
            final ImageViewEx background) {
        if (name == null)
            return;
        int resourceId = ResourcesSingleton.instance().getIdentifier(name, "drawable", getPackageName());
        if (background != null) {
            try {
                ApplicationEx.setSetlistBitmap(getBitmap(resourceId));
                background.setImageDrawable(null);
            	BitmapDrawable bitmapDrawable = new BitmapDrawable(
            			ResourcesSingleton.instance(),
            			ApplicationEx.getSetlistBitmap());
        		bitmapDrawable.setColorFilter(new PorterDuffColorFilter(
            			ResourcesSingleton.instance().getColor(
            					R.color.background_light),
    					PorterDuff.Mode.SRC_ATOP));
                background.setImageDrawable(bitmapDrawable);
                //background.setImageBitmap(ApplicationEx.getSetlistBitmap());
            } catch (OutOfMemoryError memErr) {
                ApplicationEx.showShortToast("Error setting setlist");
                /*
                if (setlistBackgroundWaitTask != null)
                    setlistBackgroundWaitTask.cancel(true);
                setlistBackgroundWaitTask = new SetlistBackgroundWaitTask(name, background);
                if (Build.VERSION.SDK_INT <
                        Build.VERSION_CODES.HONEYCOMB)
                    setlistBackgroundWaitTask.execute();
                else
                    setlistBackgroundWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                */
            }
        }
        /*
        if (setlistBackgroundTask != null)
            setlistBackgroundTask.cancel(true);
        setlistBackgroundTask = new SetBackgroundTask(name, false, "setlist",
                background);
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
            setlistBackgroundTask.execute();
        else
            setlistBackgroundTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */
    }
    
    @Override
    public void onLoginPressed(int loginType, String user, String pass) {
        isLogging = true;
        showLogin();
        switch(loginType) {
        case FragmentBase.LOGIN_FACEBOOK:
            facebookLogin();
            break;
        case FragmentBase.LOGIN_TWITTER:
        	try {
        		twitterLogin();
        	} catch (IllegalArgumentException e) {}
            break;
        case FragmentBase.LOGIN_ANON:
            anonymousLogin();
            break;
        case FragmentBase.LOGIN_EMAIL:
            emailLogin(user, pass);
            break;
        case FragmentBase.SIGNUP_EMAIL:
            emailSignup(user, pass);
            break;
        }
        DatabaseHelperSingleton.instance().setUserValue(isLogging ? 1 : 0,
                DatabaseHelper.COL_LOGGING, userId);
    }
    
    private void facebookLogin() {
        facebookLogin = true;
        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    if (err != null) {
                        if (err.getCode() == ParseException.CONNECTION_FAILED)
                        	ApplicationEx.showLongToast("Network error");
                        else
                        	ApplicationEx.showLongToast(
                                    "Login failed, try again");
                        logOut();
                    }
                    else {
                        ApplicationEx.showLongToast("Login failed, try again");
                        logOut();
                    }
                } else {
                    newUser = user.isNew();
                    userId = user.getObjectId();
                    if (!DatabaseHelperSingleton.instance().hasUser(userId))
                        DatabaseHelperSingleton.instance().addUser(user, "Facebook");
                    else
                        DatabaseHelperSingleton.instance().setOffset(1, user.getObjectId());
                    getFacebookDisplayName(user);
                    if (isLogging)
                        setupUser(newUser);
                }
            }
        });
    }
    
    private void getFacebookDisplayName(final ParseUser user) {
    	if (user.getString("displayName") == null) {
            Session session = ParseFacebookUtils.getSession();
            boolean error = false;
        	if (session != null && session.getState().isOpened()) {
        		do {
            		try {
	            		Request.executeMeRequestAsync(session,
	            				new GraphUserCallback() {
							@Override
							public void onCompleted(GraphUser graphUser,
									Response response) {
								if (user != null && graphUser != null &&
										response != null &&
										response.getError() == null) {
									displayName = graphUser.getFirstName() +
											" " + graphUser.getLastName()
													.substring(0, 1) + ".";
									// TODO Deal with object has outstanding network connection
									// This is likely a bug in the Parse code
					                try {
					                	user.put("displayName", displayName);
					                    user.saveEventually();
					                }
					                catch (RuntimeException e) {}
					                DatabaseHelperSingleton.instance().setUserValue(
					                		displayName,
					                		DatabaseHelper.COL_DISPLAY_NAME,
					                		userId);
								}
							}
	            		});
	            		error = false;
            		} catch (IllegalStateException e) {
            			error = true;
            		}
        		} while (error);
        	}
        }
    }
    
    private void twitterLogin() {
        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    if (err != null) {
                        if (err.getCode() == ParseException.CONNECTION_FAILED)
                        	ApplicationEx.showLongToast("Network error");
                        else
                        	ApplicationEx.showLongToast(
                        			"Login failed, try again");
                        logOut();
                    }
                    else {
                        ApplicationEx.showLongToast("Login failed, try again");
                        logOut();
                    }
                } else {
                    newUser = user.isNew();
                    userId = user.getObjectId();
                    if (user.getString("displayName") == null) {
                        user.put("displayName", "@" +
                                ParseTwitterUtils.getTwitter().getScreenName());
                        try {
                            user.saveEventually();
                        }
                        catch (RuntimeException e) {}
                    }
                    if (!DatabaseHelperSingleton.instance().hasUser(user.getObjectId()))
                        DatabaseHelperSingleton.instance().addUser(user, "Twitter");
                    else
                        DatabaseHelperSingleton.instance().setOffset(1, user.getObjectId());
                    if (isLogging)
                        setupUser(newUser);
                }
            }
        });
    }
    
    private void anonymousLogin() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException e) {
                if (e != null) {
                    if (e.getCode() == ParseException.CONNECTION_FAILED)
                    	ApplicationEx.showLongToast("Network error");
                    else
                    	ApplicationEx.showLongToast("Login failed, try again");
                    logOut();
                }
                else {
                    newUser = user.isNew();
                    userId = user.getObjectId();
                    try {
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    if (!DatabaseHelperSingleton.instance().hasUser(
                                                user.getObjectId()))
                                        DatabaseHelperSingleton.instance().addUser(user,
                                                "Anonymous");
                                    else
                                        DatabaseHelperSingleton.instance().setOffset(1,
                                                user.getObjectId());
                                    if (isLogging)
                                        setupUser(newUser);
                                }
                            }
                        });
                    }
                    catch (RuntimeException exception) {}
                }
            }
        });
    }
    
    private void emailSignup(String username, String password) {
        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        int atIndex = username.indexOf("@");
        int numReplace = atIndex - 2;
        if (numReplace < 1)
            numReplace = 1;
        user.put("displayName", username.substring(0, username.indexOf("@")+1));
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException err) {
                if (err == null) {
                    newUser = user.isNew();
                    userId = user.getObjectId();
                    if (!DatabaseHelperSingleton.instance().hasUser(userId))
                        DatabaseHelperSingleton.instance().addUser(user, "Email");
                    else
                        DatabaseHelperSingleton.instance().setOffset(1, user.getObjectId());
                    if (isLogging)
                        setupUser(newUser);
                }
                else {
                    if (err.getCode() == ParseException.CONNECTION_FAILED)
                    	ApplicationEx.showLongToast("Network error");
                    else if (err.getCode() != 202)
                    	ApplicationEx.showLongToast("Sign up failed: " +
                    			err.getCode());
                    logOut();
                }
            }
        });
    }
    
    private void emailLogin(final String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    if (err != null) {
                        Log.e(Constants.LOG_TAG, "Login failed: " +
                                err.getCode());
                        if (err.getCode() == ParseException.CONNECTION_FAILED)
                        	ApplicationEx.showLongToast("Network error");
                        else if (err.getCode() ==
                                ParseException.OBJECT_NOT_FOUND)
                        	ApplicationEx.showLongToast("Invalid password");
                        logOut();
                    }
                    else {
                        ApplicationEx.showLongToast("Login failed, try again");
                        logOut();
                    }
                } else {
                    newUser = user.isNew();
                    userId = user.getObjectId();
                    if (user.getString("displayName") == null) {
                        user.put("displayName", username.substring(0,
                                         username.indexOf("@")+1));
                        try {
                            user.saveEventually();
                        }
                        catch (RuntimeException e) {}
                    }
                    if (!DatabaseHelperSingleton.instance().hasUser(userId))
                        DatabaseHelperSingleton.instance().addUser(user, "Email");
                    else
                        DatabaseHelperSingleton.instance().setOffset(1, userId);
                    if (isLogging)
                        setupUser(newUser);
                }
            }
        });
    }
    
    @Override
    public void onInfoPressed(/*boolean fresh*/) {
        try {
            inInfo = true;
            DatabaseHelperSingleton.instance().setUserValue(inInfo ? 1 : 0,
                    DatabaseHelper.COL_IN_INFO, userId);
            FragmentInfo fInfo = new FragmentInfo();
            currFrag = fInfo;
            FragmentTransaction ft = fMan.beginTransaction();
            /*
            if (!fresh)
                ft.setCustomAnimations(R.anim.slide_in_top,
                        R.anim.slide_out_top);
            */
            ft.replace(android.R.id.content, fInfo, "fInfo")
                    .commitAllowingStateLoss();
            fMan.executePendingTransactions();
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        } catch (IllegalStateException e) {}
    }
    
    @Override
    public void onStatsPressed() {
        inLoad = true;
        DatabaseHelperSingleton.instance().setUserValue(inLoad ? 1 : 0,
                DatabaseHelper.COL_IN_LOAD, userId);
        showLoad();
        fetchDisplayName();
    }
    
    private void showLoad() {
        try {
            FragmentLoad fLoad = new FragmentLoad();
            currFrag = fLoad;
            FragmentTransaction ft = fMan.beginTransaction();
            if (currFrag != null && currFrag instanceof FragmentPager &&
                    currFrag.isVisible())
                ((FragmentPager)currFrag).removeChildren(ft);
            /*
            ft.setCustomAnimations(R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom);
            */
            ft.replace(android.R.id.content, fLoad, "fLoad")
                    .commitAllowingStateLoss();
            fMan.executePendingTransactions();
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        } catch (IllegalStateException e) {}
    }
    
    @Override
    public void onFaqPressed(/*boolean fresh*/) {
        try {
            inFaq = true;
            DatabaseHelperSingleton.instance().setUserValue(inFaq ? 1 : 0,
                    DatabaseHelper.COL_IN_FAQ, userId);
            FragmentFaq fFaq = new FragmentFaq();
            currFrag = fFaq;
            FragmentTransaction ft = fMan.beginTransaction();
            /*
            if (!fresh)
                ft.setCustomAnimations(R.anim.slide_in_top,
                        R.anim.slide_out_top);
            */
            ft.replace(android.R.id.content, fFaq, "fFaq")
                    .commitAllowingStateLoss();
            fMan.executePendingTransactions();
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        } catch (IllegalStateException e) {}
    }
    
    private class GetStatsTask extends AsyncTask<Void, Void, Void> {
        int currScore;
        ParseException error;
        int hints;
        
        private GetStatsTask(int currScore) {
            this.currScore = currScore;
        }
        
        @Override
        protected void onPreExecute() {
            currFrag.showLoading("Downloading standings...");
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
        	// TODO Use already known values for correct answers and hints
            leadersBundle = new Bundle();
            leadersBundle.putString("userId", userId);
            leadersBundle.putString("userName", displayName);
            leadersBundle.putString("userScore", Integer.toString(currScore));
            try {
	            leadersBundle.putString("userAnswers",
	                    Integer.toString(correctAnswers.size()));
            } catch (NullPointerException e) {
            	leadersBundle.putString("userAnswers", "Error!");
            }
            if (isCancelled())
                return null;
            DatabaseHelperSingleton.instance().clearLeaders();
            ArrayList<String> devList = new ArrayList<String>();
            devList.add("unPF5wRxnK");
            devList.add("LuzjEBVnC8");
            devList.add("8aLb2I0fQA");
            devList.add("krEPKBuzFN");
            devList.add("k5VoRhL5BQ");
            devList.add("9LvKnpSEqu");
            if (isCancelled())
                return null;
            ParseQuery<ParseObject> hintsQuery = new ParseQuery<ParseObject>("CorrectAnswers");
            hintsQuery.whereEqualTo("userId", userId)
                      .whereEqualTo("hint", true);
            try {
                hints = hintsQuery.count();
                leadersBundle.putString("userHints", Integer.toString(
                        hints));
            } catch (ParseException e) {
                if (e.getCode() == 101)
                    leadersBundle.putString("userHints", Integer.toString(
                            hints));
                else {
                    error = e;
                    publishProgress();
                }
            }
            if (isCancelled())
                return null;
            ParseQuery<ParseObject> questionQuery = new ParseQuery<ParseObject>("Question");
            questionQuery.orderByDescending("createdAt");
            try {
                ParseObject question = questionQuery.getFirst();
                Date questionDate = question.getCreatedAt();
                Calendar cal = new GregorianCalendar(
                        TimeZone.getTimeZone("GMT"));
                cal.setTime(questionDate);
                cal.setTimeZone(TimeZone.getDefault());
                String lastQuestionDate = DateFormat.getDateFormat(
                        ApplicationEx.getApp()).format(cal.getTime());
                leadersBundle.putString("lastQuestion",
                        lastQuestionDate);
            } catch (ParseException e) {
                error = e;
                publishProgress();
            }
            ArrayList<String> ranks = new ArrayList<String>(devList);
            ArrayList<ParseUser> rankObjects = new ArrayList<ParseUser>();
            ParseQuery<ParseUser> rankQuery = ParseUser.getQuery();
            rankQuery.whereExists("displayName");
            rankQuery.whereExists("score");
            rankQuery.setLimit(1000);
            rankQuery.orderByDescending("score");
            int rank = -1;
            do {
            	rankQuery.whereNotContainedIn("objectId", ranks);
	            try {
	            	List<ParseUser> rankList = rankQuery.find();
	            	if (rankList.size() == 0)
	            		break;
	            	for (int i = 0; i < rankList.size(); i++) {
	            		ranks.add(rankList.get(i).getObjectId());
	            		rankObjects.add(rankList.get(i));
	            		if (rankList.get(i).getObjectId().equals(userId)) {
	            			rank = ++i;
	            			break;
	            		}
	            	}
	            	if (rank > -1)
	            		leadersBundle.putString("userRank",
	            				(rank < 10 ? "0" : "") +
	            						Integer.toString(rank));
	            } catch (ParseException e) {
	            	error = e;
	            	publishProgress();
	            }
            } while (!isCancelled());
            if (!leadersBundle.containsKey("userRank"))
            	leadersBundle.putString("userRank", "");
            getLeaders(rankObjects);
            if (!isCancelled()) {
                inStats = true;
                DatabaseHelperSingleton.instance().setUserValue(inStats ? 1 : 0,
                        DatabaseHelper.COL_IN_STATS, userId);
                inLoad = false;
                DatabaseHelperSingleton.instance().setUserValue(inLoad ? 1 : 0,
                        DatabaseHelper.COL_IN_LOAD, userId);
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (error != null)
                Log.e(Constants.LOG_TAG, "Error getting stats: " +
                        error.getMessage());
            currFrag.showNetworkProblem();
            if (getStatsTask != null)
                getStatsTask.cancel(true);
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            showLeaders();
        }
        
        private void getLeaders(List<ParseUser> leaders) {
            rankList = new ArrayList<String>();
            userList = new ArrayList<String>();
            scoreList = new ArrayList<String>();
            userIdList = new ArrayList<String>();
            Number tempNum = 0;
            int tempInt = 0;
            int limit = 0;
            for (ParseObject leader : leaders) {
                if (isCancelled())
                    return;
                if (limit >= 50)
                    break;
                userIdList.add(leader.getObjectId());
                if (limit+1 < 10)
                    rankList.add("0" + Integer.toString(limit+1));
                else
                    rankList.add(Integer.toString(limit+1));
                userList.add(leader.getString("displayName"));
                tempNum = leader.getNumber("score");
                tempInt = tempNum == null ? 0 : tempNum.intValue();
                scoreList.add(Integer.toString(tempInt));
                if (leader.getObjectId().equals(userId))
                    leadersBundle.putString("userScore",
                            Integer.toString(tempInt));
                limit++;
                DatabaseHelperSingleton.instance().addLeader(userId,
                        rankList.get(rankList.size()-1),
                        userList.get(userList.size()-1),
                        scoreList.get(scoreList.size()-1),
                        userIdList.get(userIdList.size()-1));
            }
            leadersBundle.putStringArrayList("rank", rankList);
            leadersBundle.putStringArrayList("user", userList);
            leadersBundle.putStringArrayList("score", scoreList);
            leadersBundle.putStringArrayList("userIdList",
                    userIdList);
        }
    }
    
    @Override
    public Bundle getLeadersState() {
        return leadersBundle;
    }
    
    @SuppressLint("NewApi")
	@Override
    public void setupUser(boolean newUser) {
        if (userId == null && !isLogging) {
            showSplash(false, false);
            return;
        }
        if (userId != null)
            getUserData(userId);
        if (fMan.findFragmentByTag("fLogin") == null)
            showLogin();
        if (userTask != null)
            userTask.cancel(true);
        userTask = new UserTask(newUser);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            userTask.execute();
        else
            userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private class UserTask extends AsyncTask<Void, Void, Void> {
        private boolean newUser;
        
        private UserTask(boolean newUser) {
            this.newUser = newUser;
        }
        
        @Override
        protected void onPreExecute() {
            if (newUser && currFrag != null)
                currFrag.showLoading("Creating account...");
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                publishProgress();
                return null;
            }
            if (userId == null) {
                publishProgress();
                return null;
            }
            if (getScoreTask != null)
                getScoreTask.cancel(true);
            if (DatabaseHelperSingleton.instance().isAnonUser(userId)) {
                if (correctAnswers != null)
                    correctAnswers.clear();
                else
                    correctAnswers = new ArrayList<String>();
                getNextQuestions(false, SharedPreferencesSingleton.instance().getInt(
                		ResourcesSingleton.instance().getString(R.string.level_key),
						Constants.HARD));
            }
            else
                publishProgress();
            if (displayName == null && !isCancelled()) {
                displayName = user.getString("displayName");
                DatabaseHelperSingleton.instance().setUserValue(displayName, 
                        DatabaseHelper.COL_DISPLAY_NAME, userId);
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (userId == null || user == null) {
                logOut();
                ApplicationEx.showLongToast("Login failed, try again");
            }
            else if (!DatabaseHelperSingleton.instance().isAnonUser(userId))
                getScore(true, false, userId, newUser);
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (userId != null) {
                switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    setBackground(DatabaseHelperSingleton.instance().getPortBackground(userId),
                            false, "quiz");
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    setBackground(DatabaseHelperSingleton.instance().getLandBackground(userId),
                            false, "quiz");
                    break;
                default:
                    setBackground(DatabaseHelperSingleton.instance().getPortBackground(userId),
                            false, "quiz");
                    break;
                }
            }
        }
    }
    
    @Override
    public void logOut(/*boolean force*/) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            new LogOutTask().execute();
        else
            new LogOutTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private class LogOutTask extends AsyncTask<Void, Void, Void> {
        //boolean force;
        
        private LogOutTask(/*boolean force*/) {
            //this.force = force;
        }
        /*
        @Override
        protected void onPreExecute() {
            showLogin(false);
        }
        */
        @Override
        protected Void doInBackground(Void... nothing) {
            loggedIn = false;
            Editor editor = SharedPreferencesSingleton.instance().edit();
            editor.putString(ResourcesSingleton.instance().getString(R.string.scoretext_key), "");
            editor.putString(ResourcesSingleton.instance().getString(R.string.questiontext_key), "");
            editor.putString(ResourcesSingleton.instance().getString(R.string.hinttext_key), "");
            editor.putString(ResourcesSingleton.instance().getString(R.string.answertext_key), "");
            editor.putString(ResourcesSingleton.instance().getString(R.string.placetext_key), "");
            editor.putInt(ResourcesSingleton.instance().getString(R.string.hinttimevis_key), View.VISIBLE);
            editor.putInt(ResourcesSingleton.instance().getString(R.string.hinttextvis_key), View.INVISIBLE);
            editor.putInt(ResourcesSingleton.instance().getString(R.string.skiptimevis_key), View.VISIBLE);
            editor.putInt(ResourcesSingleton.instance().getString(R.string.skiptextvis_key), View.INVISIBLE);
            editor.putString(ResourcesSingleton.instance().getString(R.string.hintnum_key), "");
            editor.putString(ResourcesSingleton.instance().getString(R.string.skipnum_key), "");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                editor.commit();
            else
                editor.apply();
            if (userTask != null)
                userTask.cancel(true);
            if (getScoreTask != null)
                getScoreTask.cancel(true);
            if (getNextQuestionsTask != null)
                getNextQuestionsTask.cancel(true);
            if (getStageTask != null)
                getStageTask.cancel(true);
            if (getStatsTask != null)
                getStatsTask.cancel(true);
            if (userId != null) {
                DatabaseHelperSingleton.instance().setUserValue(isLogging ? 1 : 0,
                        DatabaseHelper.COL_LOGGING, userId);
                DatabaseHelperSingleton.instance().setUserValue(loggedIn ? 1 : 0,
                        DatabaseHelper.COL_LOGGED_IN, userId);
                DatabaseHelperSingleton.instance().setOffset(0, userId);
            }
            ParseUser.logOut();
            user = null;
            userId = null;
            displayName = null;
            if (correctAnswers != null) {
                correctAnswers.clear();
                correctAnswers = null;
                ApplicationEx.setStringArrayPref(
                		ResourcesSingleton.instance().getString(R.string.correct_key), correctAnswers);
            }
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        protected void onProgressUpdate(Void... nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            isLogging = false;
            setLoggingOut(false);
            if (!inInfo)
                showSplash(false, false);
            else
                onInfoPressed();
        }
    }
    
    @Override
    public void showScoreDialog() {
        DialogFragment newFragment = new FragmentScoreDialog();
        newFragment.show(getSupportFragmentManager(), "dScore");
    }
    
    @Override
    public void showNameDialog() {
        DialogFragment newFragment = new FragmentNameDialog();
        newFragment.show(getSupportFragmentManager(), "dName");
    }
    /*
    public void showDownloadDialog() {
    	DialogFragment newFragment = new FragmentDownloadDialog();
    	newFragment.show(getSupportFragmentManager(), "dDownload");
    }
    */
    @Override
    public void next() {
    	if (!questionIds.isEmpty()) {
	        if (Build.VERSION.SDK_INT <
	                Build.VERSION_CODES.HONEYCOMB)
	            new BackgroundTask(questionIds.get(0)).execute();
	        else
	            new BackgroundTask(questionIds.get(0)).executeOnExecutor(
	                    AsyncTask.THREAD_POOL_EXECUTOR);
    	}
        newQuestion = false;
        DatabaseHelperSingleton.instance().setUserValue(newQuestion ? 1 : 0,
                DatabaseHelper.COL_NEW_QUESTION, userId);
        getNextQuestions(false, SharedPreferencesSingleton.instance().getInt(
        		ResourcesSingleton.instance().getString(R.string.level_key),
				Constants.HARD));
    }
    
    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private String questionId;
        
        private BackgroundTask(String questionId) {
            this.questionId = questionId;
        }
        @Override
        protected Void doInBackground(Void... nothing) {
            if (isCancelled())
                return null;
            if (questionId != null && correctAnswers != null &&
            		correctAnswers.contains(questionId)) {
                if (correctAnswers != null && correctAnswers.size() % 20 == 0) {
                    if (getBackground() == null) {
                        if (userId != null) {
                            if (isCancelled())
                                return null;
                            portBackground =
                                DatabaseHelperSingleton.instance().getPortBackground(userId);
                            landBackground =
                                DatabaseHelperSingleton.instance().getLandBackground(userId);
                            if (getBackground() == null) {
                                switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                                case Configuration.ORIENTATION_PORTRAIT:
                                    portBackground = "splash4";
                                    DatabaseHelperSingleton.instance().setPortBackground(userId, portBackground);
                                    break;
                                case Configuration.ORIENTATION_LANDSCAPE:
                                    landBackground = "splash4";
                                    DatabaseHelperSingleton.instance().setLandBackground(userId, landBackground);
                                    break;
                                default:
                                    portBackground = "splash4";
                                    DatabaseHelperSingleton.instance().setPortBackground(userId, portBackground);
                                    break;
                                }
                            }
                        }
                        else {
                            switch(ResourcesSingleton.instance().getConfiguration().orientation) {
                            case Configuration.ORIENTATION_PORTRAIT:
                                portBackground = "splash4";
                                break;
                            case Configuration.ORIENTATION_LANDSCAPE:
                                landBackground = "splash4";
                                break;
                            default:
                                portBackground = "splash4";
                                break;
                            }
                        }
                    }
                    publishProgress();
                }
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            setBackground(getBackground(), true, "quiz");
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            
        }
    }
    
    @Override
    public boolean questionIdsEmpty() {
    	return questionIds.isEmpty();
    }

    @Override
    public String getQuestionId(int index) {
    	if (questionIds.isEmpty())
    		return null;
        return questionIds.get(index);
    }
    
    @Override
    public void addQuestionId(String questionId) {
    	questionIds.add(questionId);
    }
    
    @Override
    public void clearQuestionIds() {
    	questionIds.clear();
    }

    @Override
    public String getQuestion(int index) {
        return questions.get(index);
    }
    
    @Override
    public void addQuestion(String question) {
        questions.add(question);
    }
    
    @Override
    public void clearQuestions() {
    	questions.clear();
    }

    @Override
    public String getQuestionAnswer(int index) {
        return questionAnswers.get(index);
    }
    
    @Override
    public void addQuestionAnswer(String correctAnswer) {
        questionAnswers.add(correctAnswer);
    }
    
    @Override
    public void clearQuestionAnswers() {
    	questionAnswers.clear();
    }

    @Override
    public String getQuestionScore(int index) {
        return questionScores.get(index);
    }
    
    @Override
    public void addQuestionScore(String questionScore) {
        questionScores.add(questionScore);
    }
    
    @Override
    public void clearQuestionScores() {
    	questionScores.clear();
    }

    @Override
    public String getQuestionCategory(int index) {
        return questionCategories.get(index);
    }
    
    @Override
    public void addQuestionCategory(String questionCategory) {
        questionCategories.add(questionCategory);
    }
    
    @Override
    public void clearQuestionCategories() {
    	questionCategories.clear();
    }
    
    @Override
    public boolean getQuestionHint(int index) {
        return Boolean.parseBoolean(questionHints.get(index));
    }
    
    @Override
    public void setQuestionHint(boolean questionHint, int index) {
    	if (questionHints.size() > index)
    		questionHints.set(index, Boolean.toString(questionHint));
    }
    
    @Override
    public void addQuestionHint(boolean questionHint) {
        questionHints.add(Boolean.toString(questionHint));
    }
    
    @Override
    public void clearQuestionHints() {
    	questionHints.clear();
    }
    
    private void addQuestionSkip(boolean questionSkip) {
    	questionSkips.add(Boolean.toString(questionSkip));
    }
    
    @Override
    public boolean getQuestionSkip(int index) {
        return Boolean.parseBoolean(questionSkips.get(index));
    }
    
    @Override
    public void clearQuestionSkips() {
    	questionSkips.clear();
    }
    
    @Override
    public void getNextQuestions(boolean force, int level) {
        if (getNextQuestionsTask != null)
            getNextQuestionsTask.cancel(true);
        // TODO Use different levels: easy, hard and default
        getNextQuestionsTask = new GetNextQuestionsTask(force, Constants.HARD);
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
            getNextQuestionsTask.execute();
        else
            getNextQuestionsTask.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private class GetNextQuestionsTask extends AsyncTask<Void, Void, Void> {
        List<ParseObject> questionList;
        ParseException error;
        boolean questionNull = false;
        ArrayList<String> stageList;
        boolean force;
        boolean resumed = false;
        private ArrayList<String> tempQuestions;
        private int level;
        
        private GetNextQuestionsTask(boolean force, int level) {
            this.force = force;
            this.level = level;
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
        	/*
            if (((getQuestionId(1) == null && getQuestionId(2) != null) || 
                    correctAnswers != null &&
                        correctAnswers.contains(getQuestionId(1))) &&
                            !isCancelled() && !force) {
                updateIds();
                if (questionIds.size() <= Constants.CACHED_LIMIT)
                	getNextQuestions(force, level);
            }
            else */if (!isCancelled()) {
                if (!force && !questionIdsEmpty())
                    updateIds();
                publishProgress();
                if (questionIds.size() > Constants.CACHED_LIMIT) {
                	updatePersistedLists();
                	return null;
                }
                try {
                	ParseQuery<ParseObject> query = null;
	                if (tempQuestions == null)
	                	tempQuestions = new ArrayList<String>();
	                else
	                	tempQuestions.clear();
	                if (correctAnswers != null)
	                    tempQuestions.addAll(correctAnswers);
	                tempQuestions.addAll(questionIds);
	                ParseQuery<ParseObject> queryFirst = null;
	                ParseQuery<ParseObject> querySecond = null;
	                if (level == Constants.HARD) {
	                	queryFirst = new ParseQuery<ParseObject>("Question");
	                    queryFirst.whereNotContainedIn("objectId",
	                    		tempQuestions);
	                    queryFirst.whereLessThanOrEqualTo("score", 1000);
	                    querySecond = new ParseQuery<ParseObject>("Question");
	                    querySecond.whereNotContainedIn("objectId",
	                    		tempQuestions);
	                	querySecond.whereDoesNotExist("score");
	                	ArrayList<ParseQuery<ParseObject>> queries =
	                			new ArrayList<ParseQuery<ParseObject>>();
	                	queries.add(queryFirst);
	                	queries.add(querySecond);
	                	query = ParseQuery.or(queries);
	                }
	                else {
	                	query = new ParseQuery<ParseObject>("Question");
	                	query.whereNotContainedIn("objectId", tempQuestions);
	                	if (level == Constants.EASY) {
	                		int easy = DatabaseHelperSingleton.instance()
	                				.getUserIntValue(DatabaseHelper.COL_EASY,
	                						userId);
	                		if (easy <= 0)
	                			easy = 600;
	                		query.whereLessThanOrEqualTo("score", easy);
	                	}
	                	else if (level == Constants.MEDIUM) {
	                		int med = DatabaseHelperSingleton.instance()
	                				.getUserIntValue(DatabaseHelper.COL_MEDIUM,
	                						userId);
	                		if (med <= 0)
	                			med = 800;
	                		query.whereLessThanOrEqualTo("score", med);
	                	}
	                }
	                /*
	                if (checkCount || count < 0) {
	                    count = query.count();
	                    ApplicationEx.addParseQuery();
	                    checkCount = false;
	                    DatabaseHelperSingleton.instance().setCheckCount(userId,
	                    		checkCount);
	                }
	                */
                    if (/*count > 0 && */!isCancelled()) {
                    	query.setLimit(1000);
                    	query.addDescendingOrder("createdAt");
                        stageList = new ArrayList<String>();
                        /*
                        int skip = (int) (Math.random()*count);
                        int limit = Constants.CACHED_QUESTIONS -
                        		questionIds.size();
                        query.setLimit(limit);
                        if (query.getLimit() > (count-skip))
                            skip = count-query.getLimit();
                        query.setSkip(skip);
                        */
                        questionList = query.find();
                        if (!questionList.isEmpty() && !isCancelled()) {
                            Number score;
                            String questionId;
                            int index = -1;
                            ParseObject question;
                            do {
                            	index = (int) (Math.random()*questionList.size());
                            	question = questionList.get(index);
                            	questionId = question.getObjectId();
                            	addQuestionId(questionId);
                            	stageList.add(questionId);
                            	addQuestion(question.getString("question"));
                            	addQuestionAnswer(question.getString("answer"));
                            	addQuestionCategory(
                            			question.getString("category"));
                            	score = question.getNumber("score");
                            	addQuestionScore(score == null ? "1011" :
                            			Integer.toString(score.intValue()));
                            	addQuestionHint(false);
                            	questionSkips.add(Boolean.toString(false));
                            } while (questionIds.size() < Constants.CACHED_QUESTIONS);
                            /*
                            for (ParseObject question : questionList) {
                            	questionId = question.getObjectId();
                            	addQuestionId(questionId);
                            	stageList.add(questionId);
                            	addQuestion(question.getString("question"));
                            	addQuestionAnswer(question.getString("answer"));
                            	addQuestionCategory(
                            			question.getString("category"));
                            	score = question.getNumber("score");
                            	addQuestionScore(score == null ? "1011" :
                            			Integer.toString(score.intValue()));
                            	addQuestionHint(false);
                            	questionSkips.add(Boolean.toString(false));
                            }
                            */
                            getStage(userId, stageList, resumed);
                            if (!isCancelled())
                            	updatePersistedLists();
                        }
                    }
                    else if (!isCancelled())
                    	updatePersistedLists();
                } catch (OutOfMemoryError memErr) {
                	Log.e(Constants.LOG_TAG, "Error: " + memErr.getMessage());
                	if (getNextQuestionsTask != null)
                        getNextQuestionsTask.cancel(true);
                    getNextQuestions(force, level);
                } catch (ParseException e) {
                    error = e;
                }
            }
            updatePersistedLists();
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (!questionIdsEmpty() && questionIds.get(0) != null &&
            		!isCancelled()) {
                if (!loggedIn) {
                    try {
                        goToQuiz();
                    } catch (IllegalStateException exception) {}
                }
                else if (currFrag != null) {
                    currFrag.resumeQuestion();
                    resumed = true;
                }
            }
            else if (questionIdsEmpty() || questionIds.get(0) == null)
                questionNull = true;
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (error != null && !isCancelled()) {
                Log.e(Constants.LOG_TAG, "Error: " + error.getMessage());
                if (userTask != null)
                    userTask.cancel(true);
                currFrag.showNetworkProblem();
            }
            else if (!isCancelled() && questionNull) {
                if (!loggedIn) {
                    try {
                        goToQuiz();
                    } catch (IllegalStateException exception) {}
                }
                else {
                    if (questionIdsEmpty() || questionIds.get(0) == null)
                        currFrag.showNoMoreQuestions(SharedPreferencesSingleton.instance()
                        		.getInt(ResourcesSingleton.instance().getString(R.string.level_key),
                    						Constants.HARD));
                    else if (!resumed)
                        currFrag.resumeQuestion();
                }
            }
        }
        
        private void updateIds() {
        	questionIds.remove(0);
        	questions.remove(0);
        	questionAnswers.remove(0);
        	questionCategories.remove(0);
        	questionScores.remove(0);
        	questionHints.remove(0);
        	questionSkips.remove(0);
        }
        
    }
    
    private void updatePersistedLists() {
    	ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questionids_key),
        				questionIds);
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questions_key),
        				questions);
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questionanswers_key),
        				questionAnswers);
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questioncategories_key),
        				questionCategories);
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questionscores_key),
        				questionScores);
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questionhints_key),
        				questionHints);
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(
        				R.string.questionskips_key),
        				questionSkips);
    }
    
    private void getStage(String userId, ArrayList<String> questionIds,
    		boolean resumed) {
        if (getStageTask != null)
            getStageTask.cancel(true);
        getStageTask = new GetStageTask(userId, questionIds, resumed);
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
            getStageTask.execute();
        else
            getStageTask.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private class GetStageTask extends AsyncTask<Void, Void, Void> {
        private String userId;
        ArrayList<String> ids;
        private boolean resumed = false;
        private boolean progressed = false;
        
        private GetStageTask(String userId, ArrayList<String> ids,
        		boolean resumed) {
            this.userId = userId;
            this.ids = ids;
            this.resumed = resumed;
        }
        @Override
        protected Void doInBackground(Void... nothing) {
            ParseQuery<ParseObject> query =
            		new ParseQuery<ParseObject>("Stage");
            query.whereEqualTo("userId", userId);
            query.whereContainedIn("questionId", ids);
            query.setLimit(ids.size());
            try {
                List<ParseObject> questionList = query.find();
                int index = -1;
                if (!questionList.isEmpty()) {
                    for (int i = 0; i < questionList.size(); i++) {
                    	index = questionIds.indexOf(
                    			questionList.get(i).getString("questionId"));
                    	if (index >= 0) {
                    		questionHints.set(index, Boolean.toString(
                    				questionList.get(i).getBoolean("hint")));
                    		questionSkips.set(index, Boolean.toString(
                    				questionList.get(i).getBoolean("skip")));
                    		if (!progressed) {
                    			publishProgress();
                    			progressed = true;
                    		}
                    	}
                    }
                }
                else {
                    for (int i = 0; i < ids.size(); i++) {
                    	index = questionIds.indexOf(ids.get(i));
                    	if (index >= 0) {
                    		questionHints.set(index, Boolean.toString(false));
                    		questionSkips.set(index, Boolean.toString(false));
                    		if (!progressed) {
                    			publishProgress();
                    			progressed = true;
                    		}
                    	}
                    }
                }
            } catch (ParseException e) {
                Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                if (currFrag != null)
                    currFrag.showNetworkProblem();
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (!loggedIn) {
                try {
                    goToQuiz();
                } catch (IllegalStateException exception) {}
            }
            else if (!resumed)
                currFrag.resumeQuestion();
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            
        }
    }

    @Override
    public boolean isNewQuestion() {
        return newQuestion;
    }

    @Override
    public void setIsNewQuestion(boolean isNewQuestion) {
        newQuestion = isNewQuestion;
        DatabaseHelperSingleton.instance().setUserValue(newQuestion ? 1 : 0,
                DatabaseHelper.COL_NEW_QUESTION, userId);
    }
    
    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setDisplayName(String displayName) {
        currFrag.setDisplayName(displayName);
        this.displayName = displayName;
        if (user != null) {
            user.put("displayName", displayName);
            try {
                user.saveEventually();
            }
            catch (RuntimeException e) {}
        }
        if (userId != null)
            DatabaseHelperSingleton.instance().setUserValue(displayName, 
                    DatabaseHelper.COL_DISPLAY_NAME, userId);
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public int getCurrentScore() {
        return currScore;
    }

    @Override
    public void addCurrentScore(int addValue) {
        currScore += addValue;
        //saveUserScore(currScore);
    }
    
    @Override
    public void saveUserScore(final int currTemp) {
        try {
            user.put("score", currTemp);
            user.saveEventually();
        }
        catch (RuntimeException e) {}
        DatabaseHelperSingleton.instance().setScore(currTemp, userId);
    }
    
    @Override
    public void shareScreenshot(boolean isSetlist) {
    	if (isSetlist && currFrag != null)
    		currFrag.showResizedSetlist();
        String path = takeScreenshot();
        if (isSetlist && currFrag != null)
    		currFrag.hideResizedSetlist();
        if (path == null)
            return;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");

        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
        
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities =
                packageManager.queryIntentActivities(share, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe)
            startActivity(Intent.createChooser(share, "Share screenshot with"));
    }
    
    private String takeScreenshot() {
    	// Make file name and create file
        Calendar cal = Calendar.getInstance(TimeZone.getDefault(),
                Locale.getDefault());
        int calendarMonth = cal.get(Calendar.MONTH)+1;
        int calendarDate = cal.get(Calendar.DATE);
        int calendarHour = cal.get(Calendar.HOUR_OF_DAY);
        int calendarMinute = cal.get(Calendar.MINUTE);
        int calendarSecond = cal.get(Calendar.SECOND);
        String fileName = cal.get(Calendar.YEAR) + "_" + (calendarMonth < 10 ?
                ("0" + calendarMonth) : calendarMonth) + "_" +
                (calendarDate < 10 ? ("0" + calendarDate) : calendarDate) +
                "_" + (calendarHour < 10 ? ("0" + calendarHour) : calendarHour)
                + "_" + (calendarMinute < 10 ? ("0" + calendarMinute) :
                calendarMinute) + "_" + (calendarSecond < 10 ?
                ("0" + calendarSecond) : calendarSecond) + ".jpg";
        String path = ApplicationEx.cacheLocation + Constants.SCREENS_LOCATION +
                fileName;
        // Capture the contents of the screen
        Bitmap bitmap = null;
        try {
    		View rootView = getWindow().getDecorView();
            int totalHeight = rootView.getHeight();
        	int totalWidth = rootView.getWidth();
            bitmap = Bitmap.createBitmap(totalWidth, totalHeight,
            		Bitmap.Config.ARGB_8888);                
            Canvas c = new Canvas(bitmap);
            rootView.layout(0, 0, totalWidth, totalHeight);
            rootView.draw(c);
            /*
            rootView.buildDrawingCache();
            bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.destroyDrawingCache();
            bitmap = Bitmap.createScaledBitmap(bitmap, rootView.getWidth() / 2,
            		rootView.getHeight() / 2, true);
            rootView.setDrawingCacheEnabled(false);
            */
        } catch (OutOfMemoryError e) {
        	ApplicationEx.showLongToast("Oops, try again");
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            ApplicationEx.showLongToast("Oops, try again");
            e.printStackTrace(); 
            return null;
        }
        FileOutputStream fout = null;
        File imageFile = new File(path);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap.recycle();
        return path;
    }
    
    @Override
    public boolean getNetworkProblem() {
        return networkProblem;
    }

    @Override
    public void setNetworkProblem(boolean networkProblem) {
        this.networkProblem = networkProblem;
    }
    
    @Override
    public void addCorrectAnswer(String correctId) {
        if (correctAnswers == null)
            correctAnswers = new ArrayList<String>();
        correctAnswers.add(correctId);
        //count--;
        ApplicationEx.setStringArrayPref(
        		ResourcesSingleton.instance().getString(R.string.correct_key),
        		correctAnswers);
    }
    
    @Override
    public boolean isCorrectAnswer(String correctId) {
        if (correctAnswers == null)
            return false;
        else {
            if (correctAnswers.contains(correctId))
                return true;
            else
                return false;
        }
    }
    
    @Override
    public void setUserName(String userName) {
        if (leadersBundle != null && userName != null)
            leadersBundle.putString("userName", userName);
    }
    
    @Override
    public boolean isNewUser() {
        return newUser;
    }
    
    @Override
    public void resetPassword(String username) {
        ParseUser.requestPasswordResetInBackground(username,
                new RequestPasswordResetCallback() {
            public void done(ParseException e) {
                if (e == null)
                	ApplicationEx.showLongToast("Password reset email has " +
                    		"been sent");
                else
                	ApplicationEx.showLongToast("An error occurred, " +
                            "try again");
            }
        });
    }
    /*
    @Override
    public int getWidth() {
    	return width;
    }
    
    @Override
    public int getHeight() {
    	return height;
    }
    */
    @Override
    public boolean isLoggingOut() {
    	return loggingOut;
    }
    
    @Override
    public void setLoggingOut(boolean loggingOut) {
    	this.loggingOut = loggingOut;
    }

	@Override
	public void setHomeAsUp(boolean homeAsUp) {
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(homeAsUp);
		if (!homeAsUp)
		    getSherlock().getActionBar().setHomeButtonEnabled(false);
	}
    /*
    @Override
    public void showQuickTip(View view, String message) {
        if (SharedPreferencesSingleton.instance().getBoolean(
        		ResourcesSingleton.instance().getString(R.string.quicktip_key), false))
            CheatSheet.setup(view, message);
    }

    @Override
    public void showQuickTipMenu(ViewGroup view, String message, int location) {
        if (SharedPreferencesSingleton.instance().getBoolean(
        		ResourcesSingleton.instance().getString(R.string.quicktip_key), false))
            CheatSheetMenu.setup(view, message, getWidth(), getHeight(),
                    location);
    }
	*/
    @Override
    public SlidingMenu slidingMenu() {
        return getSlidingMenu();
    }
    
    @Override
    public void refreshMenu() {
        if (mMenu != null) {
            shareItem = mMenu.findItem(R.id.ShareMenu);
            setlistItem = mMenu.findItem(R.id.SetlistMenu);
            if (inSetlist) {
                if (shareItem != null)
                    shareItem.setVisible(true);
                if (setlistItem != null)
                    setlistItem.setVisible(false);
            }
            else if (!inStats && !inInfo && !inFaq && !inLoad && !isLogging) {
                if (shareItem != null)
                    shareItem.setVisible(false);
                if (setlistItem != null)
                    setlistItem.setVisible(true);
            }
            else {
                if (shareItem != null)
                    shareItem.setVisible(false);
                if (setlistItem != null)
                    setlistItem.setVisible(false);
            }
            onPrepareOptionsMenu(mMenu);
        }
    }
    
    @Override
    public boolean getGoToSetlist() {
    	if (goToSetlist) {
    		nManager.cancel(Constants.NOTIFICATION_NEW_SONG);
    	}
        return goToSetlist;
    }
    
    @Override
    public void setInSetlist(boolean inSetlist) {
    	if (this.inSetlist == inSetlist) {
    		return;
    	}
        this.inSetlist = inSetlist;
        DatabaseHelperSingleton.instance().setUserValue(inSetlist ? 1 : 0,
                DatabaseHelper.COL_IN_SETLIST, userId);
        refreshMenu();
        refreshSlidingMenu();
    }
    
    @Override
    public boolean getInSetlist() {
    	if (inSetlist) {
    		nManager.cancel(Constants.NOTIFICATION_NEW_SONG);
    	}
        return inSetlist;
    }
    
    @Override
    public Bitmap getBitmap(int resId) throws OutOfMemoryError {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16*1024];
        options.inSampleSize = 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	options.inMutable = true;
        return BitmapFactory.decodeResource(ResourcesSingleton.instance(),
        		resId, options);
    }
    
    @Override
    public void setCurrFrag(FragmentBase currFrag) {
    	this.currFrag = currFrag;
    }
    
    @Override
    public FragmentBase getCurrFrag() {
    	return currFrag;
    }
    
}