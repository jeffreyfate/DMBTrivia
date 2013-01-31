package com.jeffthefate.dmbquiz_dev.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeffthefate.dmbquiz_dev.ApplicationEx;
import com.jeffthefate.dmbquiz_dev.Constants;
import com.jeffthefate.dmbquiz_dev.DatabaseHelper;
import com.jeffthefate.dmbquiz_dev.R;
import com.jeffthefate.dmbquiz_dev.VersionedActionBar;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentBase;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentBase.OnButtonListener;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentInfo;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentLeaders;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentLoad;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentLogin;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentNameDialog;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentQuiz;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentScoreDialog;
import com.jeffthefate.dmbquiz_dev.fragment.FragmentSplash;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.facebook.FacebookError;
import com.parse.facebook.Util;

public class ActivityMain extends FragmentActivity implements OnButtonListener {
    
    /*
     * Fresh login scenario:
     * 
     * Set to isLogging
     * Login fragment shown
     * Background Parse login process started
     * When finished, userId and display name saved
     * Login timer started
     * Check for a user
     * Check for a userId
     * Get all state values from userId
     * Check for display name and get from user if necessary
     * Get user score from database and save to user object
     * Get answer hint map and answer list from database
     * If answer list is empty, get answers from Parse
     * Grab all persisted question values
     *      Get question count if necessary
     *      Get next question if necessary
     * Start question screen if necessary
     * Setup the UI for the current question
    */
    
    FragmentManager fMan;
    
    private ImageView background;
    private TextView noConnection;
    private ParseUser user;
    private String userId;
    private FragmentBase currFrag;
    
    private int rawIndex = -1;
    private Field[] fields;
    private ArrayList<Integer> fieldsList;
    private String currentBackground = null;
    
    private boolean loggedIn = false;
    private boolean isLogging = false;
    private boolean inLoad = false;
    private boolean inStats = false;
    private boolean inInfo = false;
    
    private String questionId;
    private String question;
    private String correctAnswer;
    private String questionCategory;
    private String questionScore;
    private String nextQuestionId;
    private String nextQuestion;
    private String nextCorrectAnswer;
    private String nextQuestionCategory;
    private String nextQuestionScore;
    private String thirdQuestionId;
    private String thirdQuestion;
    private String thirdCorrectAnswer;
    private String thirdQuestionCategory;
    private String thirdQuestionScore;
    
    private int questionCount = -1;
    
    private boolean newQuestion = false;
    
    private boolean leaderDone = false;
    private boolean questionDone = false;
    
    private ShowStatsTask showStatsTask;
    private GetStatsTask getStatsTask;
    private GetParseTask getParseTask;
    private com.jeffthefate.dmbquiz_dev.activity.ActivityMain.GetParseTask.GetAnswersTask getAnswersTask;
    private com.jeffthefate.dmbquiz_dev.activity.ActivityMain.GetParseTask.GetScoreTask getScoreTask;
    private SetupQuestionTask setupQuestionTask;
    private Bundle leadersBundle;
    private ArrayList<String> answerIds = null;
    private ArrayList<String> rankList = new ArrayList<String>();
    private ArrayList<String> userList = new ArrayList<String>();
    private ArrayList<String> scoreList = new ArrayList<String>();
    private ArrayList<String> userIdList = new ArrayList<String>();
    
    private String displayName;
    private int currScore = -1;
    
    private UserTask userTask;
    
    private boolean isPersisted = false;
    private boolean networkProblem = false;
    private boolean facebookLogin = false;
    
    public interface UiCallback {
        public void showNetworkProblem();
        public void showLoading(String message);
        public void showNoMoreQuestions();
        public void resumeQuestion();
        public void updateTimerButtons();
        public void updateScoreText();
        public void resetHint();
        public void disableButton(boolean isRetry);
        public void enableButton(boolean isRetry);
        public void setDisplayName(String displayName);
    }
    
    private NotificationManager nManager;
    
    private ConnectionReceiver connReceiver;
    
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
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        else
            VersionedActionBar.newInstance().create(this).setDisplayHome();
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
        background = (ImageView) findViewById(R.id.Background);
        noConnection = (TextView) findViewById(R.id.NoConnection);
        /*
        TEST
        Parse.initialize(this, "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
                "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
        */
        Parse.initialize(this, "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
                "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
        PushService.subscribe(this, "", ActivityMain.class);
        PushService.setDefaultPushCallback(this, ActivityMain.class);
        fMan = getSupportFragmentManager();
        if (savedInstanceState != null) {
            loggedIn = savedInstanceState.getBoolean("loggedIn");
            isLogging = savedInstanceState.getBoolean("isLogging");
            inLoad = savedInstanceState.getBoolean("inLoad");
            inStats = savedInstanceState.getBoolean("inStats");
            inInfo = savedInstanceState.getBoolean("inInfo");
            currentBackground = savedInstanceState.getString(
                    "currentBackground");
            userId = savedInstanceState.getString("userId");
            questionId = savedInstanceState.getString("questionId");
            question = savedInstanceState.getString("question");
            correctAnswer = savedInstanceState.getString("correctAnswer");
            questionScore = savedInstanceState.getString("questionScore");
            questionCategory = savedInstanceState.getString("questionCategory");
            nextQuestionId = savedInstanceState.getString("nextQuestionId");
            nextQuestion = savedInstanceState.getString("nextQuestion");
            nextCorrectAnswer = savedInstanceState.getString(
                    "nextCorrectAnswer");
            nextQuestionScore = savedInstanceState.getString(
                    "nextQuestionScore");
            nextQuestionCategory = savedInstanceState.getString(
                    "nextQuestionCategory");
            thirdQuestionId = savedInstanceState.getString("thirdQuestionId");
            thirdQuestion = savedInstanceState.getString("thirdQuestion");
            thirdCorrectAnswer = savedInstanceState.getString(
                    "thirdCorrectAnswer");
            thirdQuestionScore = savedInstanceState.getString(
                    "thirdQuestionScore");
            thirdQuestionCategory = savedInstanceState.getString(
                    "thirdQuestionCategory");
            questionCount = savedInstanceState.getInt("questionCount");
            newQuestion = savedInstanceState.getBoolean("newQuestion");
            answerIds = savedInstanceState.getStringArrayList("answerIds");
            displayName = savedInstanceState.getString("displayName");
            currScore = savedInstanceState.getInt("currScore");
            isPersisted = true;
            networkProblem = savedInstanceState.getBoolean("networkProblem");
            ApplicationEx.dbHelper.setUserValue(networkProblem ? 1 : 0,
                    DatabaseHelper.COL_NETWORK_PROBLEM, userId);
        }
        else {
            userId = ApplicationEx.dbHelper.getCurrUser();
            if (userId != null) {
                getPersistedData();
                isPersisted = true;
            }
        }
        if (userId == null)
            userId = ApplicationEx.dbHelper.getCurrUser();
        if (currentBackground == null && userId != null)
            currentBackground =
                    ApplicationEx.dbHelper.getCurrBackground(userId);
        if (currentBackground != null) {
            Resources res = getResources();
            int resourceId = res.getIdentifier(currentBackground, "drawable",
                    getPackageName());
            background.setImageResource(resourceId);
        }
        nManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        connReceiver = new ConnectionReceiver();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("loggedIn", loggedIn);
        outState.putBoolean("isLogging", isLogging);
        outState.putBoolean("inLoad", inLoad);
        outState.putBoolean("inStats", inStats);
        outState.putBoolean("inInfo", inInfo);
        outState.putString("currentBackgrond", currentBackground);
        outState.putString("userId", userId);
        outState.putString("questionId", questionId);
        outState.putString("question", question);
        outState.putString("correctAnswer", correctAnswer);
        outState.putString("questionCategory", questionCategory);
        outState.putString("questionScore", questionScore);
        outState.putString("nextQuestionId", nextQuestionId);
        outState.putString("nextQuestion", nextQuestion);
        outState.putString("nextCorrectAnswer", nextCorrectAnswer);
        outState.putString("nextQuestionCategory", nextQuestionCategory);
        outState.putString("nextQuestionScore", nextQuestionScore);
        outState.putString("thirdQuestionId", thirdQuestionId);
        outState.putString("thirdQuestion", thirdQuestion);
        outState.putString("thirdCorrectAnswer", thirdCorrectAnswer);
        outState.putString("thirdQuestionCategory", thirdQuestionCategory);
        outState.putString("thirdQuestionScore", thirdQuestionScore);
        outState.putInt("questionCount", questionCount);
        outState.putBoolean("newQuestion", newQuestion);
        outState.putStringArrayList("answerIds", answerIds);
        outState.putString("displayName", displayName);
        outState.putInt("currScore", currScore);
        outState.putBoolean("networkProblem", networkProblem);
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (getAnswersTask != null)
            getAnswersTask.cancel(true);
        if (getScoreTask != null)
            getScoreTask.cancel(true);
        if (getParseTask != null)
            getParseTask.cancel(true);
        if (!isLogging) {
            if (!loggedIn)
                logOut(false);
            if (userId == null)
                checkUser();
            else
                showLoggedInFragment();
        }
        else {
            if (userId != null)
                setupUser();
            else if (!facebookLogin)
                checkUser();
        }
        ApplicationEx.setActive();
        nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
        registerReceiver(connReceiver,
                new IntentFilter(Constants.ACTION_CONNECTION));
    }
    
    @Override
    public void onBackPressed() {
        if (!inStats && !inLoad && !isLogging && !inInfo)
            moveTaskToBack(true);
        else {
            if (inLoad) {
                if (showStatsTask != null)
                    showStatsTask.cancel(true);
                if (getStatsTask != null)
                    getStatsTask.cancel(true);
                inLoad = false;
                ApplicationEx.dbHelper.setUserValue(inLoad ? 1 : 0,
                        DatabaseHelper.COL_IN_LOAD, userId);
                showQuiz();
            }
            else if (inStats) {
                inStats = false;
                ApplicationEx.dbHelper.setUserValue(inStats ? 1 : 0,
                        DatabaseHelper.COL_IN_STATS, userId);
                showQuiz();
            }
            else if (inInfo) {
                inInfo = false;
                ApplicationEx.dbHelper.setUserValue(inInfo ? 1 : 0,
                        DatabaseHelper.COL_IN_INFO, userId);
                showSplash();
            }
            else if (isLogging) {
                if (userTask != null)
                    userTask.cancel(true);
                if (facebookTask != null)
                    facebookTask.cancel(true);
                if (getAnswersTask != null)
                    getAnswersTask.cancel(true);
                if (getScoreTask != null)
                    getScoreTask.cancel(true);
                if (getParseTask != null)
                    getParseTask.cancel(true);
                if (setupQuestionTask != null)
                    setupQuestionTask.cancel(true);
                logOut(true);
            }
        }   
    }

    @Override
    public String setBackground(String name, boolean showNew) {
        Resources res = getResources();
        if (name == null)
            name = "splash8";
        int resourceId = res.getIdentifier(name, "drawable", getPackageName());
        if (showNew) {
            rawIndex = fieldsList.indexOf(resourceId);
            if (rawIndex < 0)
                rawIndex = fieldsList.indexOf(R.drawable.splash8);
            rawIndex++;
            if (rawIndex >= fieldsList.size())
                rawIndex = 0;
            int currentId = fieldsList.get(rawIndex);
            currentBackground = res.getResourceEntryName(currentId);
            ApplicationEx.dbHelper.setCurrBackground(userId, currentBackground);
            if (currentId != resourceId)
                background.setImageResource(currentId);
            else
                setBackground(currentBackground, showNew);
        }
        else {
            if (fieldsList.indexOf(resourceId) >= 0)
                background.setImageResource(resourceId);
            else
                background.setImageResource(R.drawable.splash8);
        }
        return currentBackground;
    }
    
    private void checkUser() {
        noConnection.setVisibility(View.INVISIBLE);
        user = ParseUser.getCurrentUser();
        if (user == null)
            userId = null;
        else
            userId = user.getObjectId();
        if (!loggedIn && !isLogging && (user == null || userId == null))
            logOut(true);
        else
            setupUser();
    }
    
    @Override
    public void setupUser() {
        if (userId == null && !isLogging) {
            showSplash();
            return;
        }
        showLogin();
        if (userTask != null)
            userTask.cancel(true);
        userTask = new UserTask();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            userTask.execute();
        else
            userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        currFrag.showLoading("Syncing account...");
    }
    
    private void showLoggedInFragment() {
        getQuestionCountFromParse(false);
        if (user == null)
            user = ParseUser.getCurrentUser();
        if (displayName == null) {
            displayName = user.getString("displayName");
            ApplicationEx.dbHelper.setUserValue(displayName, 
                    DatabaseHelper.COL_DISPLAY_NAME, userId);
        }
        if (inStats)
            showLeaders();
        else if (inLoad)
            onStatsPressed();
        else {
            loggedIn = true;
            ApplicationEx.dbHelper.setUserValue(loggedIn ? 1 : 0,
                    DatabaseHelper.COL_LOGGED_IN, userId);
            if (answerIds == null)
                answerIds = ApplicationEx.dbHelper.readAnswers(userId);
            showQuiz();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
    
    @Override
    public void onInfoPressed() {
        try {
            FragmentInfo fInfo = new FragmentInfo();
            fMan.beginTransaction().replace(android.R.id.content, fInfo,
                    "fInfo").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fInfo;
            inInfo = true;
            ApplicationEx.dbHelper.setUserValue(inInfo ? 1 : 0,
                    DatabaseHelper.COL_IN_INFO, userId);
        } catch (IllegalStateException e) {}
    }
    
    @Override
    public void onStatsPressed() {
        showLoad();
        if (getStatsTask != null)
            getStatsTask.cancel(true);
        getStatsTask = new GetStatsTask(
                ApplicationEx.dbHelper.getScore(userId));
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
            getStatsTask.execute();
        else
            getStatsTask.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
        inLoad = true;
        ApplicationEx.dbHelper.setUserValue(inLoad ? 1 : 0,
                DatabaseHelper.COL_IN_LOAD, userId);
    }
    
    @Override
    public void onLoginPressed(int loginType, String user, String pass) {
        isLogging = true;
        isPersisted = false;
        showLogin();
        switch(loginType) {
        case FragmentBase.LOGIN_FACEBOOK:
            facebookLogin();
            break;
        case FragmentBase.LOGIN_TWITTER:
            twitterLogin();
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
        ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                DatabaseHelper.COL_LOGGING, userId);
    }
    
    private void goToQuiz() {
        isLogging = false;
        ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                DatabaseHelper.COL_LOGGING, userId);
        showQuiz();
        loggedIn = true;
        ApplicationEx.dbHelper.setUserValue(loggedIn ? 1 : 0,
                DatabaseHelper.COL_LOGGED_IN, userId);
    }
    
    private void facebookLogin() {
        facebookLogin = true;
        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    if (err != null) {
                        if (err.getCode() == ParseException.CONNECTION_FAILED)
                            Toast.makeText(ApplicationEx.getApp(),
                                    "Network error", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(ApplicationEx.getApp(),
                                    "Login failed, try again",
                                    Toast.LENGTH_LONG).show();
                        logOut(false);
                    }
                    else
                        logOut(false);
                } else {
                    userId = user.getObjectId();
                    if (!ApplicationEx.dbHelper.hasUser(userId))
                        ApplicationEx.dbHelper.addUser(user, "Facebook");
                    else
                        ApplicationEx.dbHelper.setOffset(1, user.getObjectId());
                    facebookTask = new FacebookTask(user); 
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                        facebookTask.execute();
                    else
                        facebookTask.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR);
                    facebookLogin = false;
                }
            }
        });
    }
    
    private FacebookTask facebookTask;
    
    private class FacebookTask extends AsyncTask<Void, Void, Void> {
        ParseUser user;
        
        private FacebookTask(ParseUser user) {
            this.user = user;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            if (user.getString("displayName") == null ||
                    (user.isNew() &&
                            !user.getString("displayName").endsWith(".") &&
                            !user.getString("displayName").contains(" "))) {
                String response;
                String firstName = null;
                String lastName = null;
                try {
                    response = ParseFacebookUtils.getFacebook().request(
                            "me");
                    JSONObject json = Util.parseJson(response);
                    firstName = json.getString("first_name");
                    lastName = json.getString("last_name");
                } 
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                } 
                catch (FacebookError e) {
                    e.printStackTrace();
                } 
                catch (JSONException e) {
                    e.printStackTrace();
                }
                displayName = firstName + " " + lastName.substring(0, 1) + ".";
                user.put("displayName", displayName);
                try {
                    user.saveEventually();
                }
                catch (RuntimeException e) {}
                ApplicationEx.dbHelper.setUserValue(displayName, 
                        DatabaseHelper.COL_DISPLAY_NAME, userId);
            }
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (isLogging)
                setupUser();
        }
    }
    
    private void twitterLogin() {
        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    if (err != null) {
                        if (err.getCode() == ParseException.CONNECTION_FAILED)
                            Toast.makeText(ApplicationEx.getApp(),
                                    "Network error", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(ApplicationEx.getApp(),
                                    "Login failed, try again",
                                    Toast.LENGTH_LONG).show();
                        logOut(false);
                    }
                    else
                        logOut(false);
                } else {
                    userId = user.getObjectId();
                    if (user.getString("displayName") == null ||
                            (user.isNew() && !user.getString("displayName")
                                    .startsWith("@"))) {
                        user.put("displayName", "@" +
                                ParseTwitterUtils.getTwitter().getScreenName());
                        try {
                            user.saveEventually();
                        }
                        catch (RuntimeException e) {}
                    }
                    if (!ApplicationEx.dbHelper.hasUser(user.getObjectId()))
                        ApplicationEx.dbHelper.addUser(user, "Twitter");
                    else
                        ApplicationEx.dbHelper.setOffset(1, user.getObjectId());
                    if (isLogging)
                        setupUser();
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
                        Toast.makeText(ApplicationEx.getApp(),
                                "Network error", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(ApplicationEx.getApp(),
                                "Login failed, try again", Toast.LENGTH_LONG)
                            .show();
                    logOut(false);
                }
                else {
                    userId = user.getObjectId();
                    try {
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    if (!ApplicationEx.dbHelper.hasUser(
                                                user.getObjectId()))
                                        ApplicationEx.dbHelper.addUser(user,
                                                "Anonymous");
                                    else
                                        ApplicationEx.dbHelper.setOffset(1,
                                                user.getObjectId());
                                    if (isLogging)
                                        setupUser();
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
                    userId = user.getObjectId();
                    if (!ApplicationEx.dbHelper.hasUser(userId))
                        ApplicationEx.dbHelper.addUser(user, "Email");
                    else
                        ApplicationEx.dbHelper.setOffset(1, user.getObjectId());
                    if (isLogging)
                        setupUser();
                }
                else {
                    if (err.getCode() == ParseException.CONNECTION_FAILED)
                        Toast.makeText(ApplicationEx.getApp(), "Network error",
                                Toast.LENGTH_LONG).show();
                    else if (err.getCode() != 202)
                        Toast.makeText(ApplicationEx.getApp(),
                                "Sign up failed: " + err.getCode(),
                                Toast.LENGTH_LONG).show();
                    logOut(false);
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
                            Toast.makeText(ApplicationEx.getApp(),
                                    "Network error", Toast.LENGTH_LONG).show();
                        else if (err.getCode() ==
                                ParseException.OBJECT_NOT_FOUND)
                            Toast.makeText(ApplicationEx.getApp(),
                                    "Invalid password", Toast.LENGTH_LONG)
                                .show();
                        logOut(false);
                    }
                    else
                        logOut(false);
                } else {
                    userId = user.getObjectId();
                    if (user.getString("displayName") == null || (user.isNew()
                            && !user.getString("displayName").endsWith("@"))) {
                        user.put("displayName", username.substring(0,
                                         username.indexOf("@")+1));
                        try {
                            user.saveEventually();
                        }
                        catch (RuntimeException e) {}
                    }
                    if (!ApplicationEx.dbHelper.hasUser(userId))
                        ApplicationEx.dbHelper.addUser(user, "Email");
                    else
                        ApplicationEx.dbHelper.setOffset(1, userId);
                    if (isLogging)
                        setupUser();
                }
            }
        });
    }
    
    @Override
    public String getBackground() {
        return currentBackground;
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

    @Override
    public void logOut(boolean force) {
        loggedIn = false;
        if (userTask != null)
            userTask.cancel(true);
        if (setupQuestionTask != null)
            setupQuestionTask.cancel(true);
        if (facebookTask != null)
            facebookTask.cancel(true);
        if (getParseTask != null)
            getParseTask.cancel(true);
        if (!getNetworkProblem() || force) {
            isLogging = false;
            showSplash();
        }
        if (userId != null) {
            ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                    DatabaseHelper.COL_LOGGING, userId);
            ApplicationEx.dbHelper.setUserValue(loggedIn ? 1 : 0,
                    DatabaseHelper.COL_LOGGED_IN, userId);
            ApplicationEx.dbHelper.setOffset(0, userId);
            ApplicationEx.dbHelper.setUserValue("", DatabaseHelper.COL_ANSWER,
                    userId);
            ApplicationEx.dbHelper.setUserValue(-1,
                    DatabaseHelper.COL_SKIP_TICK, userId);
            ApplicationEx.dbHelper.setUserValue(-1,
                    DatabaseHelper.COL_HINT_TICK, userId);
            ApplicationEx.dbHelper.setUserValue(0,
                    DatabaseHelper.COL_HINT_PRESSED, userId);
            ApplicationEx.dbHelper.setUserValue(0,
                    DatabaseHelper.COL_SKIP_PRESSED, userId);
            ApplicationEx.dbHelper.setUserValue("",
                    DatabaseHelper.COL_HINT, userId);
            ApplicationEx.dbHelper.setUserValue(0,
                    DatabaseHelper.COL_IS_CORRECT, userId);
        }
        ParseUser.logOut();
        user = null;
        userId = null;
        displayName = null;
        if (answerIds != null) {
            answerIds.clear();
            answerIds = null;
        }
    }
    
    private class ShowStatsTask extends AsyncTask<Void, Void, Void> {
        int loadSec = 0;
        
        @Override
        protected Void doInBackground(Void... nothing) {
            do {
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                loadSec++;
            } while (!isCancelled() && (!leaderDone || !questionDone));
            if (!isCancelled()) {
                inStats = true;
                ApplicationEx.dbHelper.setUserValue(inStats ? 1 : 0,
                        DatabaseHelper.COL_IN_STATS, userId);
                inLoad = false;
                ApplicationEx.dbHelper.setUserValue(inLoad ? 1 : 0,
                        DatabaseHelper.COL_IN_LOAD, userId);
            }
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (loadSec == 5)
                currFrag.showLoading("Downloading standings...");
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            showLeaders();
        }
    }
    
    private void showLeaders() {
        try {
            FragmentLeaders fLeaders = new FragmentLeaders();
            fMan.beginTransaction().replace(android.R.id.content, fLeaders,
                    "fLeaders").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fLeaders;
        } catch (IllegalStateException e) {}
    }
    
    private void showLogin() {
        try {
            FragmentLogin fLogin = new FragmentLogin();
            fMan.beginTransaction().replace(android.R.id.content, fLogin,
                    "fLogin").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fLogin;
        } catch (IllegalStateException e) {}
    }
    
    private void showSplash() {
        try {
            FragmentSplash fSplash = new FragmentSplash();
            fMan.beginTransaction().replace(android.R.id.content, fSplash,
                    "fSplash").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fSplash;
        } catch (IllegalStateException e) {}
    }
    
    private void showQuiz() {
        try {
            FragmentQuiz fQuiz = new FragmentQuiz();
            fMan.beginTransaction().replace(android.R.id.content, fQuiz,
                    "fQuiz").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fQuiz;
        } catch (IllegalStateException e) {}
    }
    
    private void showLoad() {
        try {
            FragmentLoad fLoad = new FragmentLoad();
            fMan.beginTransaction().replace(android.R.id.content, fLoad,
                    "fLoad").commitAllowingStateLoss();
            fMan.executePendingTransactions();
            currFrag = fLoad;
        } catch (IllegalStateException e) {}
    }
    
    @Override
    public void onPause() {
        unregisterReceiver(connReceiver);
        if (userTask != null)
            userTask.cancel(true);
        if (setupQuestionTask != null)
            setupQuestionTask.cancel(true);
        if (showStatsTask != null)
            showStatsTask.cancel(true);
        if (facebookTask != null)
            facebookTask.cancel(true);
        if (getStatsTask != null)
            getStatsTask.cancel(true);
        if (getAnswersTask != null)
            getAnswersTask.cancel(true);
        if (!isLogging) {
            getParseTask = new GetParseTask(false, userId);
            if (Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.HONEYCOMB)
                getParseTask.execute();
            else
                getParseTask.executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR);
            getQuestionCountFromParse(false);
        }
        ApplicationEx.setInactive();
        super.onPause();
    }
    
    private class GetStatsTask extends AsyncTask<Void, Void, Void> {
        int currScore;
        ParseException error;
        
        private GetStatsTask(int currScore) {
            this.currScore = currScore;
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            leaderDone = false;
            questionDone = false;
            leadersBundle = new Bundle();
            leadersBundle.putString("userId", userId);
            leadersBundle.putString("userName", displayName);
            leadersBundle.putString("userScore", Integer.toString(currScore));
            leadersBundle.putString("userAnswers",
                    Integer.toString(answerIds.size()));
            leadersBundle.putString("userHints",
                    Integer.toString(
                            ApplicationEx.dbHelper.getHintCount(userId)));
            if (isCancelled())
                return null;
            ApplicationEx.dbHelper.clearLeaders();
            showStatsTask = new ShowStatsTask();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                showStatsTask.execute();
            else
                showStatsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            ArrayList<String> devList = new ArrayList<String>();
            devList.add("unPF5wRxnK");
            devList.add("LuzjEBVnC8");
            devList.add("8aLb2I0fQA");
            devList.add("krEPKBuzFN");
            devList.add("k5VoRhL5BQ");
            if (isCancelled())
                return null;
            ParseQuery leadersQuery = ParseUser.getQuery();
            leadersQuery.whereExists("displayName").whereExists("score")
                    .whereNotContainedIn("objectId", devList)
                    .orderByDescending("score").setLimit(50);
            leadersQuery.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> leaders, ParseException e) {
                    if (e == null) {
                        if (Build.VERSION.SDK_INT <
                                Build.VERSION_CODES.HONEYCOMB)
                            new GetLeadersTask(leaders).execute();
                        else
                            new GetLeadersTask(leaders).executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                        error = e;
                        publishProgress();
                    }
                }
            });
            if (isCancelled())
                return null;
            ParseQuery questionQuery = new ParseQuery("Question");
            questionQuery.orderByDescending("createdAt");
            questionQuery.getFirstInBackground(new GetCallback() {
                @Override
                public void done(ParseObject question, ParseException e) {
                    if (e == null) {
                        Date questionDate = question.getCreatedAt();
                        Calendar cal = new GregorianCalendar(
                                TimeZone.getTimeZone("GMT"));
                        cal.setTime(questionDate);
                        cal.setTimeZone(TimeZone.getDefault());
                        String lastQuestionDate = DateFormat.getDateFormat(
                                ApplicationEx.getApp()).format(cal.getTime());
                        leadersBundle.putString("lastQuestion",
                                lastQuestionDate);
                    }
                    else {
                        error = e;
                        publishProgress();
                    }
                    questionDone = true;
                }
            });
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            currFrag.showNetworkProblem();
            if (getStatsTask != null)
                getStatsTask.cancel(true);
            if (showStatsTask != null)
                showStatsTask.cancel(true);
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            
        }
        
        private class GetLeadersTask extends AsyncTask<Void, Void, Void> {
            List<ParseObject> leaders;
            
            private GetLeadersTask(List<ParseObject> leaders) {
                this.leaders = leaders;
            }
            
            @Override
            protected Void doInBackground(Void... nothing) {
                rankList = new ArrayList<String>();
                userList = new ArrayList<String>();
                scoreList = new ArrayList<String>();
                userIdList = new ArrayList<String>();
                Number tempNum = 0;
                int tempInt = 0;
                int limit = 0;
                for (ParseObject leader : leaders) {
                    if (isCancelled())
                        return null;
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
                    ApplicationEx.dbHelper.addLeader(userId,
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
                leaderDone = true;
                return null;
            }
            
            protected void onProgressUpdate(Void... nothing) {
                
            }
            
            @Override
            protected void onCancelled(Void nothing) {
            }
            
            @Override
            protected void onPostExecute(Void nothing) {
                
            }
        }
    }

    @Override
    public Bundle getLeadersState() {
        return leadersBundle;
    }
    
    private class GetParseTask extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> correctAnswers = new ArrayList<String>();
        private String answerId;
        private int sum = 0;
        private Number score;
        private boolean show = false;
        private String userId;
        
        private GetParseTask(boolean show, String userId) {
            this.show = show;
            this.userId = userId;
        }
        
        private void getAnswerCount(final String userId) {
            ParseQuery query = new ParseQuery("CorrectAnswers");
            query.whereEqualTo("userId", userId);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int correctCount, ParseException e) {
                    if (answerIds != null && !isCancelled()) {
                        if (answerIds.size() != correctCount) {
                            if (questionCount > answerIds.size() &&
                                    answerIds.size() < correctCount &&
                                    !isCancelled())
                                getCorrectAnswers(userId);
                            else if (!isCancelled()) {
                                correctAnswers =
                                        new ArrayList<String>(answerIds);
                                getScore(userId);
                            }
                        }
                        else if (!isCancelled()) {
                            correctAnswers =
                                new ArrayList<String>(answerIds);
                            getScore(userId);
                        }
                    }
                    else if (!isCancelled())
                        getCorrectAnswers(userId);
                }
            });
        }
        
        private void getCorrectAnswers(final String userId) {
            ParseQuery query = new ParseQuery("CorrectAnswers");
            query.whereContains("userId", userId);
            query.whereNotContainedIn("questionId", correctAnswers);
            query.setLimit(1000);
            query.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> answerList,
                        ParseException e) {
                    if (e == null && !isCancelled()) {
                        if (getAnswersTask != null)
                            getAnswersTask.cancel(true);
                        getAnswersTask = new GetAnswersTask(answerList);
                        if (Build.VERSION.SDK_INT <
                                Build.VERSION_CODES.HONEYCOMB)
                            getAnswersTask.execute();
                        else
                            getAnswersTask.executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (!isCancelled()) {
                        Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                        if (userTask != null)
                            userTask.cancel(true);
                        if (show && currFrag != null)
                            currFrag.showNetworkProblem();
                    }
                }
            });
        }
        
        public class GetAnswersTask extends AsyncTask<Void, Void, Void> {
            private List<ParseObject> answerList;
            
            private GetAnswersTask(List<ParseObject> answerList) {
                this.answerList = answerList;
            }
            
            @Override
            protected Void doInBackground(Void... nothing) {
                for (ParseObject answer : answerList) {
                    if (isCancelled())
                        return null;
                    answerId = answer.getString("questionId");
                    if (answerId != null && userId != null) {
                        if (answer.getBoolean("hint") && !isCancelled())
                            ApplicationEx.dbHelper.markAnswerCorrect(
                                    answerId, userId, true, true);
                        else if (!isCancelled())
                            ApplicationEx.dbHelper.markAnswerCorrect(
                                    answerId, userId, true, false);
                        if (!correctAnswers.contains(answerId) &&
                                !isCancelled())
                            correctAnswers.add(answerId);
                    }
                }
                if (answerList.size() == 1000 && !isCancelled())
                    getCorrectAnswers(userId);
                else if (!isCancelled()) {
                    answerIds = new ArrayList<String>(correctAnswers);
                    if (!isCancelled())
                        publishProgress();
                    if (!isCancelled())
                        getScore(userId);
                }
                return null;
            }
            
            protected void onProgressUpdate(Void... nothing) {
                if (show && !isCancelled())
                    currFrag.showLoading("Calculating score...");
            }
            
            @Override
            protected void onCancelled(Void nothing) {
            }
            
            @Override
            protected void onPostExecute(Void nothing) {
            }
        }
        
        private void getScore(final String userId) {
            ParseQuery query = new ParseQuery("Question");
            query.whereContainedIn("objectId", correctAnswers);
            query.setLimit(1000);
            query.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> questionList,
                        ParseException e) {
                    if (e == null && !isCancelled()) {
                        if (getScoreTask != null)
                            getScoreTask.cancel(true);
                        getScoreTask = new GetScoreTask(questionList);
                        if (Build.VERSION.SDK_INT <
                                Build.VERSION_CODES.HONEYCOMB)
                            getScoreTask.execute();
                        else
                            getScoreTask.executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (!isCancelled()) {
                        Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                        if (userTask != null)
                            userTask.cancel(true);
                        if (show && currFrag != null)
                            currFrag.showNetworkProblem();
                    }
                }
            });
        }
        
        public class GetScoreTask extends AsyncTask<Void, Void, Void> {
            private List<ParseObject> questionList;
            
            private GetScoreTask(List<ParseObject> questionList) {
                this.questionList = questionList;
            }
            
            @Override
            protected Void doInBackground(Void... nothing) {
                int index = -1;
                for (ParseObject question : questionList) {
                    if (isCancelled())
                        return null;
                    score = question.getNumber("score");
                    sum += score == null ? 1000 : score.intValue();
                    correctAnswers.remove(question.getObjectId());
                }
                if (questionList.size() == 1000 && !isCancelled())
                    getScore(userId);
                else if (!isCancelled()) {
                    currScore = sum;
                    saveUserScore(currScore);
                    if (show && !isCancelled())
                        publishProgress();
                }
                return null;
            }
            
            protected void onProgressUpdate(Void... nothing) {
                if (userId != null && !isCancelled()) {
                    setupQuestionTask = new SetupQuestionTask(userId);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                        setupQuestionTask.execute();
                    else
                        setupQuestionTask.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            
            @Override
            protected void onCancelled(Void nothing) {
            }
            
            @Override
            protected void onPostExecute(Void nothing) {
            }
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            if (getAnswersTask != null)
                getAnswersTask.cancel(true);
            if (getScoreTask != null)
                getScoreTask.cancel(true);
            if (userId == null)
                return null;
            if (isCancelled())
                return null;
            getAnswerCount(userId);
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
        }
    }
    
    private void getQuestionCountFromParse(final boolean getQuestions) {
        final long perfTime = System.currentTimeMillis();
        ParseCloud.callFunctionInBackground("getQuestionCount", null,
                new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> count, ParseException e) {
                if (e == null) {
                    questionCount = Integer.parseInt(count.get("total")
                            .toString());
                    ApplicationEx.dbHelper.setUserValue(questionCount,
                            DatabaseHelper.COL_QUESTION_COUNT, userId);
                    if (getQuestions) {
                        if (ApplicationEx.dbHelper.getUserType(userId)
                                .equalsIgnoreCase("Anonymous")) {
                            answerIds = new ArrayList<String>();
                            nextQuestion();
                        }
                        else {
                            getParseTask = new GetParseTask(true, userId);
                            if (Build.VERSION.SDK_INT <
                                    Build.VERSION_CODES.HONEYCOMB)
                                getParseTask.execute();
                            else
                                getParseTask.executeOnExecutor(
                                        AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
                else
                    Log.e(Constants.LOG_TAG, "Error getting question count: " +
                            e.getMessage());
            }
        });
    }
    
    private class UserTask extends AsyncTask<Void, Void, Void> {
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
            if (questionCount < 0 && !isCancelled())
                getQuestionCountFromParse(true);
            else if (!isCancelled()) {
                if (ApplicationEx.dbHelper.isAnonUser(userId) &&
                        !isCancelled()) {
                    answerIds = new ArrayList<String>();
                    nextQuestion();
                }
                else if (!isCancelled()) {
                    getParseTask = new GetParseTask(true, userId);
                    if (Build.VERSION.SDK_INT <
                            Build.VERSION_CODES.HONEYCOMB)
                        getParseTask.execute();
                    else
                        getParseTask.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            if (displayName == null && !isCancelled()) {
                displayName = user.getString("displayName");
                ApplicationEx.dbHelper.setUserValue(displayName, 
                        DatabaseHelper.COL_DISPLAY_NAME, userId);
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            logOut(true);
            Toast.makeText(ApplicationEx.getApp(), "Login failed, try again",
                    Toast.LENGTH_LONG).show();
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (userId != null)
                setBackground(ApplicationEx.dbHelper.getCurrBackground(userId),
                        false);
        }
    }
    
    @Override
    public void saveUserScore(final int currTemp) {
        try {
            user.put("score", currTemp);
            user.saveEventually();
        }
        catch (RuntimeException e) {}
        ApplicationEx.dbHelper.setScore(currTemp, userId);
    }
    
    private class SetupQuestionTask extends AsyncTask<Void, Void, Void> {
        private String userId;
        
        private SetupQuestionTask(String userId) {
            this.userId = userId;
        }
        
        @Override
        protected Void doInBackground(Void... nothing) {
            if (questionId == null && !isCancelled()) {
                questionId = ApplicationEx.dbHelper.getCurrQuestionId(userId);
                question = ApplicationEx.dbHelper.getCurrQuestionQuestion(
                        userId);
                correctAnswer = ApplicationEx.dbHelper.getCurrQuestionAnswer(
                        userId);
                questionCategory = 
                        ApplicationEx.dbHelper.getCurrQuestionCategory(userId);
                questionScore = ApplicationEx.dbHelper.getCurrQuestionScore(
                        userId);
                currFrag.resetHint();
            }
            if (nextQuestionId == null && !isCancelled()) {
                nextQuestionId = ApplicationEx.dbHelper.getNextQuestionId(
                        userId);
                nextQuestion = ApplicationEx.dbHelper.getNextQuestionQuestion(
                        userId);
                nextCorrectAnswer = 
                        ApplicationEx.dbHelper.getNextQuestionAnswer(userId);
                nextQuestionCategory = 
                        ApplicationEx.dbHelper.getNextQuestionCategory(userId);
                nextQuestionScore = ApplicationEx.dbHelper.getNextQuestionScore(
                        userId);
            }
            if (thirdQuestionId == null && !isCancelled()) {
                thirdQuestionId = ApplicationEx.dbHelper.getThirdQuestionId(
                        userId);
                thirdQuestion = ApplicationEx.dbHelper.getThirdQuestionQuestion(
                        userId);
                thirdCorrectAnswer =
                        ApplicationEx.dbHelper.getThirdQuestionAnswer(userId);
                thirdQuestionCategory = 
                        ApplicationEx.dbHelper.getThirdQuestionCategory(userId);
                thirdQuestionScore =
                        ApplicationEx.dbHelper.getThirdQuestionScore(userId);
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (questionId == null && !isCancelled()) {
                if (answerIds != null && !isCancelled())
                    nextQuestion();
            }
            else if (!isCancelled()) {
                if (!loggedIn) {
                    try {
                        goToQuiz();
                    } catch (IllegalStateException exception) {}
                }
                else
                    currFrag.resumeQuestion();
            }
        }
    }
    
    @Override
    public void nextQuestion() {
        questionId = nextQuestionId;
        nextQuestionId = thirdQuestionId;
        thirdQuestionId = null;
        question = nextQuestion != null ? nextQuestion.trim() :
                nextQuestion;
        nextQuestion = thirdQuestion != null ? thirdQuestion.trim() :
                thirdQuestion;
        thirdQuestion = null;
        correctAnswer = nextCorrectAnswer != null ?
                nextCorrectAnswer.trim() : nextCorrectAnswer;
        nextCorrectAnswer = thirdCorrectAnswer != null ?
                        thirdCorrectAnswer.trim() : thirdCorrectAnswer;
        thirdCorrectAnswer = null;
        questionCategory = nextQuestionCategory != null ?
                nextQuestionCategory.trim() : nextQuestionCategory;
        nextQuestionCategory = thirdQuestionCategory != null ?
                        thirdQuestionCategory.trim() : thirdQuestionCategory;
        thirdQuestionCategory = null;
        questionScore = nextQuestionScore;
        nextQuestionScore = thirdQuestionScore;
        thirdQuestionScore = null;
        if (answerIds.size() >= questionCount) {
            questionId = null;
            nextQuestionId = null;
            thirdQuestionId = null;
            question = null;
            nextQuestion = null;
            thirdQuestion = null;
            correctAnswer = null;
            nextCorrectAnswer = null;
            thirdCorrectAnswer = null;
            questionCategory = null;
            nextQuestionCategory = null;
            thirdQuestionCategory = null;
            questionScore = null;
            nextQuestionScore = null;
            thirdQuestionScore = null;
            if (!loggedIn) {
                try {
                    goToQuiz();
                } catch (IllegalStateException exception) {}
            }
            else
                showQuiz();
            return;
        }
        if ((questionId != null && answerIds.contains(questionId)) ||
                (questionId == null && (nextQuestionId != null ||
                thirdQuestionId != null)))
            nextQuestion();
        else {
            currFrag.resetHint();
            if (userId != null && questionId != null) {
                if (!loggedIn) {
                    try {
                        goToQuiz();
                    } catch (IllegalStateException exception) {}
                }
                else
                    currFrag.resumeQuestion();
            }
            ArrayList<String> tempList = new ArrayList<String>(answerIds);
            ArrayList<String> skipList =
                    ApplicationEx.dbHelper.getSkipQuestions(userId);
            tempList.removeAll(skipList);
            tempList.addAll(skipList);
            if (tempList.size() == questionCount &&
                    answerIds.size() < questionCount)
                tempList = new ArrayList<String>(answerIds);
            ParseQuery query = new ParseQuery("Question");
            int skip = (int) (Math.random()*(questionCount-answerIds.size()));
            int total = 0;
            if (questionId != null && !tempList.contains(questionId))
                tempList.add(questionId);
            if (nextQuestionId != null && !tempList.contains(nextQuestionId))
                tempList.add(nextQuestionId);
            if (thirdQuestion != null && !tempList.contains(thirdQuestionId))
                tempList.add(thirdQuestionId);
            if (questionId == null) {
                total = questionCount-answerIds.size()-3;
                if (total < 0)
                    total = 0;
                query.setSkip(total < skip ? total : skip);
                query.setLimit(3);
                query.whereNotContainedIn("objectId", tempList);
            }
            else if (nextQuestionId == null) {
                if (thirdQuestionId == null) {
                    total = questionCount-tempList.size()-2;
                    if (total < 0)
                        total = 0;
                    query.setSkip(total < skip ? total : skip);
                    query.setLimit(2);
                    query.whereNotContainedIn("objectId", tempList);
                }
                else {
                    total = questionCount-tempList.size()-1;
                    if (total < 0)
                        total = 0;
                    query.setSkip(total < skip ? total : skip);
                    query.setLimit(1);
                    query.whereNotContainedIn("objectId", tempList);
                }
            }
            else {
                total = questionCount-tempList.size()-1;
                if (total < 0)
                    total = 0;
                query.setSkip(total < skip ? total : skip);
                query.setLimit(1);
                query.whereNotContainedIn("objectId", tempList);
            }
            query.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> questions,
                        ParseException e) {
                    if (e == null) {
                        if (!questions.isEmpty()) {
                            Number score;
                            ParseObject followQuestion = null;
                            if (questions.size() == 3) {
                                ParseObject currQuestion = questions.get(0);
                                questionId = currQuestion.getObjectId();
                                question = currQuestion.getString("question");
                                correctAnswer =
                                        currQuestion.getString("answer");
                                questionCategory = currQuestion.getString(
                                        "category");
                                score = currQuestion.getNumber("score");
                                questionScore = score == null ? "1011" :
                                        Integer.toString(score.intValue());
                                if (questionId != null && userId != null) {
                                    if (!loggedIn) {
                                        try {
                                            goToQuiz();
                                        } catch (
                                            IllegalStateException exception) {}
                                    }
                                    else
                                        currFrag.resumeQuestion();
                                }
                                currQuestion = questions.get(1);
                                nextQuestionId = currQuestion.getObjectId();
                                nextQuestion = currQuestion.getString(
                                        "question");
                                nextCorrectAnswer = currQuestion.getString(
                                        "answer");
                                nextQuestionCategory = currQuestion.getString(
                                        "category");
                                score = currQuestion.getNumber("score");
                                nextQuestionScore = score == null ? "1011" :
                                        Integer.toString(score.intValue());
                                followQuestion = questions.get(2);
                            }
                            else if (questions.size() == 2) {
                                ParseObject currQuestion = questions.get(0);
                                nextQuestionId = currQuestion.getObjectId();
                                nextQuestion = currQuestion.getString(
                                        "question");
                                nextCorrectAnswer = currQuestion.getString(
                                        "answer");
                                nextQuestionCategory = currQuestion.getString(
                                        "category");
                                score = currQuestion.getNumber("score");
                                nextQuestionScore = score == null ? "1011" :
                                        Integer.toString(score.intValue());
                                followQuestion = questions.get(1);
                            }
                            else {
                                followQuestion = questions.get(0);
                            }
                            if (nextQuestionId == null) {
                                nextQuestionId = followQuestion.getObjectId();
                                nextQuestion = followQuestion.getString(
                                        "question");
                                nextCorrectAnswer = followQuestion.getString(
                                        "answer");
                                nextQuestionCategory = followQuestion.getString(
                                        "category");
                                score = followQuestion.getNumber("score");
                                nextQuestionScore = score == null ? "1011" :
                                        Integer.toString(score.intValue());
                            }
                            else {
                                thirdQuestionId = followQuestion.getObjectId();
                                thirdQuestion = followQuestion.getString(
                                        "question");
                                thirdCorrectAnswer = followQuestion.getString(
                                        "answer");
                                thirdQuestionCategory = followQuestion.getString(
                                        "category");
                                score = followQuestion.getNumber("score");
                                thirdQuestionScore = score == null ? "1011" :
                                        Integer.toString(score.intValue());
                            }
                            ApplicationEx.dbHelper.setQuestions(userId,
                                    questionId, question, correctAnswer,
                                    questionCategory, questionScore,
                                    nextQuestionId, nextQuestion,
                                    nextCorrectAnswer, nextQuestionCategory,
                                    nextQuestionScore, thirdQuestionId,
                                    thirdQuestion, thirdCorrectAnswer,
                                    thirdQuestionCategory, thirdQuestionScore);
                        }
                        else {
                            ApplicationEx.dbHelper.setQuestions(userId,
                                    questionId, question, correctAnswer,
                                    questionCategory, questionScore,
                                    nextQuestionId, nextQuestion,
                                    nextCorrectAnswer, nextQuestionCategory,
                                    nextQuestionScore, thirdQuestionId,
                                    thirdQuestion, thirdCorrectAnswer,
                                    thirdQuestionCategory, thirdQuestionScore);
                            if (!loggedIn) {
                                try {
                                    goToQuiz();
                                } catch (IllegalStateException exception) {}
                            }
                            else
                                currFrag.resumeQuestion();
                        }
                    }
                    else {
                        Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                        if (userTask != null)
                            userTask.cancel(true);
                        currFrag.showNetworkProblem();
                    }
                }
            });
        }
    }
    
    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... nothing) {
            int answerCount = ApplicationEx.dbHelper.getAnswerCount(userId);
            if (questionId != null &&
                    !ApplicationEx.dbHelper.getQuestionSkip(questionId, userId)
                    && answerCount > 0 && answerCount % 20 == 0) {
                if (currentBackground == null) {
                    if (userId != null) {
                        currentBackground =
                            ApplicationEx.dbHelper.getCurrBackground(userId);
                        if (currentBackground == null)
                            currentBackground = "splash8";
                    }
                    else
                        currentBackground = "splash8";
                    
                }
                publishProgress();
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            ApplicationEx.dbHelper.setCurrBackground(userId,
                    setBackground(currentBackground, true));
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            
        }
    }
    
    @Override
    public void next() {
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
            new BackgroundTask().execute();
        else
            new BackgroundTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
        newQuestion = false;
        ApplicationEx.dbHelper.setUserValue(newQuestion ? 1 : 0,
                DatabaseHelper.COL_NEW_QUESTION, userId);
        if (answerIds != null) {
            if (answerIds.size() == questionCount)
                currFrag.showNoMoreQuestions();
            else
                nextQuestion();
        }
    }

    @Override
    public String getQuestionId() {
        return questionId;
    }

    @Override
    public String getQuestion() {
        return question;
    }

    @Override
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    @Override
    public String getQuestionScore() {
        return questionScore;
    }

    @Override
    public String getQuestionCategory() {
        return questionCategory;
    }

    @Override
    public String getNextQuestionId() {
        return nextQuestionId;
    }

    @Override
    public String getNextQuestion() {
        return nextQuestion;
    }

    @Override
    public String getNextCorrectAnswer() {
        return nextCorrectAnswer;
    }

    @Override
    public String getNextQuestionScore() {
        return nextQuestionScore;
    }

    @Override
    public String getNextQuestionCategory() {
        return nextQuestionCategory;
    }
    
    @Override
    public String getThirdQuestionId() {
        return nextQuestionId;
    }

    @Override
    public String getThirdQuestion() {
        return nextQuestion;
    }

    @Override
    public String getThirdCorrectAnswer() {
        return nextCorrectAnswer;
    }

    @Override
    public String getThirdQuestionScore() {
        return nextQuestionScore;
    }

    @Override
    public String getThirdQuestionCategory() {
        return nextQuestionCategory;
    }

    @Override
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    @Override
    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public void setQuestionScore(String questionScore) {
        this.questionScore = questionScore;
    }

    @Override
    public void setQuestionCategory(String questionCategory) {
        this.questionCategory = questionCategory;
    }

    @Override
    public void setNextQuestionId(String nextQuestionId) {
        this.nextQuestionId = nextQuestionId;
    }

    @Override
    public void setNextQuestion(String nextQuestion) {
        this.nextQuestion = nextQuestion;
    }

    @Override
    public void setNextCorrectAnswer(String nextCorrectAnswer) {
        this.nextCorrectAnswer = nextCorrectAnswer;
    }

    @Override
    public void setNextQuestionScore(String nextQuestionScore) {
        this.nextQuestionScore = nextQuestionScore;
    }

    @Override
    public void setNextQuestionCategory(String nextQuestionCategory) {
        this.nextQuestionCategory = nextQuestionCategory;
    }
    
    @Override
    public void setThirdQuestionId(String thirdQuestionId) {
        this.thirdQuestionId = thirdQuestionId;
    }

    @Override
    public void setThirdQuestion(String thirdQuestion) {
        this.thirdQuestion = thirdQuestion;
    }

    @Override
    public void setThirdCorrectAnswer(String thirdCorrectAnswer) {
        this.thirdCorrectAnswer = thirdCorrectAnswer;
    }

    @Override
    public void setThirdQuestionScore(String thirdQuestionScore) {
        this.thirdQuestionScore = thirdQuestionScore;
    }

    @Override
    public void setThirdQuestionCategory(String thirdQuestionCategory) {
        this.thirdQuestionCategory = thirdQuestionCategory;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ArrayList<String> getAnswerIds() {
        return answerIds;
    }

    @Override
    public void addAnswerId(String answerId) {
        answerIds.add(answerId);
    }
    
    @Override
    public boolean hasAnswerId(String answerId) {
        return answerIds.contains(answerId);
    }

    @Override
    public boolean isNewQuestion() {
        return newQuestion;
    }

    @Override
    public void setIsNewQuestion(boolean isNewQuestion) {
        newQuestion = isNewQuestion;
        ApplicationEx.dbHelper.setUserValue(newQuestion ? 1 : 0,
                DatabaseHelper.COL_NEW_QUESTION, userId);
    }

    @Override
    public int getCurrentScore() {
        return currScore;
    }

    @Override
    public void addCurrentScore(int addValue) {
        currScore += addValue;
        saveUserScore(currScore);
    }

    @Override
    public void shareScreenshot() {
        String path = takeScreenshot();
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
    
    @Override
    public int getQuestionsLeft() {
        if (answerIds == null)
            return questionCount;
        else
            return questionCount-answerIds.size();
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
            ApplicationEx.dbHelper.setUserValue(displayName, 
                    DatabaseHelper.COL_DISPLAY_NAME, userId);
    }
    
    private String takeScreenshot() {
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
        
        Bitmap bitmap;
        View v1 = background.getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
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
        return path;
    }
    
    private void getPersistedData() {
        loggedIn = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_LOGGED_IN, userId) == 1 ? true : false;
        isLogging = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_LOGGING, userId) == 1 ? true : false;
        inLoad = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_IN_LOAD, userId) == 1 ? true : false;
        inStats = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_IN_STATS, userId) == 1 ? true : false;
        inInfo = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_IN_INFO, userId) == 1 ? true : false;
        currentBackground = ApplicationEx.dbHelper.getUserStringValue(
                DatabaseHelper.COL_CURR_BACKGROUND, userId);
        questionId = ApplicationEx.dbHelper.getCurrQuestionId(userId);
        question = ApplicationEx.dbHelper.getCurrQuestionQuestion(userId);
        correctAnswer = ApplicationEx.dbHelper.getCurrQuestionAnswer(userId);
        questionScore = ApplicationEx.dbHelper.getCurrQuestionScore(userId);
        questionCategory =
                ApplicationEx.dbHelper.getCurrQuestionCategory(userId);
        nextQuestionId = ApplicationEx.dbHelper.getNextQuestionId(userId);
        nextQuestion = ApplicationEx.dbHelper.getNextQuestionQuestion(userId);
        nextCorrectAnswer =
                ApplicationEx.dbHelper.getNextQuestionAnswer(userId);
        nextQuestionScore = ApplicationEx.dbHelper.getNextQuestionScore(userId);
        nextQuestionCategory =
                ApplicationEx.dbHelper.getNextQuestionCategory(userId);
        thirdQuestionId = ApplicationEx.dbHelper.getThirdQuestionId(userId);
        thirdQuestion = ApplicationEx.dbHelper.getThirdQuestionQuestion(userId);
        thirdCorrectAnswer =
                ApplicationEx.dbHelper.getThirdQuestionAnswer(userId);
        thirdQuestionScore = ApplicationEx.dbHelper.getThirdQuestionScore(
                userId);
        thirdQuestionCategory =
                ApplicationEx.dbHelper.getThirdQuestionCategory(userId);
        questionCount = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_QUESTION_COUNT, userId);
        newQuestion = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_NEW_QUESTION, userId) == 1 ? true : false;
        displayName = ApplicationEx.dbHelper.getUserStringValue(
                DatabaseHelper.COL_DISPLAY_NAME, userId);
        currScore = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_SCORE, userId);
        networkProblem = ApplicationEx.dbHelper.getUserIntValue(
                DatabaseHelper.COL_NETWORK_PROBLEM, userId) == 1 ? true : false;
        answerIds = ApplicationEx.dbHelper.readAnswers(userId);
    }
    
    private class ConnectionReceiver extends BroadcastReceiver {
        
        boolean wifiEnabled = false;
        ConnectivityManager connMan;
     
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(
                    Constants.ACTION_CONNECTION)) {
                if (intent.hasExtra("hasConnection")) {
                    if (!intent.getBooleanExtra("hasConnection", false))
                        currFrag.showNetworkProblem();
                }
            }
        }
     
    }

    @Override
    public boolean getNetworkProblem() {
        return networkProblem;
    }

    @Override
    public void setNetworkProblem(boolean networkProblem) {
        this.networkProblem = networkProblem;
    }
    
}