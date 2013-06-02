package com.jeffthefate.dmbquiz;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public interface OnButtonListener {
	/**
	 * Set the (non-selist) background image.
	 * @param name		name of resource to be applied as the background
	 * @param showNew	true if changing the background, false if setting it for
	 * 					the first time (initializing)
	 */
    public void setBackground(String name, boolean showNew, String screen);
    /**
     * Return the current (non-setlist) background name, based on orientation
     * @return
     */
    public String getBackground();
    /**
     * Set the setlist background given to the given image view
     * @param name			name of the setlist background resource
     * @param background	image view to apply the background to
     */
    public void setlistBackground(String name, ImageViewEx background);
    /*
    public String getSplashBackground();
    public String getQuizBackground();
    public String getLeadersBackground();
    */
    /**
     * Start the login process.
     * 
     * Based on the type, a login is started and the login UI is shown.
     * 
     * @param loginType specific type logging in (Facebook, Twitter, email,
     * 					anonymous)
     * @param user username text (only required for email login or signup)
     * @param pass password text (only required for email login or signup)
     */
    public void onLoginPressed(int loginType, String user, String pass);
    /**
     * FAQ button in menu is pressed; UI for FAQ is displayed
     */
    public void onInfoPressed(/*boolean fresh*/);
    /**
     * Stats button is pressed; UI for stats (leaders) is displayed and stats
     * info is fetched
     */
    public void onStatsPressed();
    /**
     * FAQ button is pressed in the stats screen; score FAQ is displayed
     */
    public void onFaqPressed();
    
    /**
     * Grab the leaders information that was previously fetched; used by leaders
     * UI to display
     * @return a bundle of information to display in leaders UI
     */
    public Bundle getLeadersState();
    
    /**
     * Setup the current user.
     * 
     * Depending on the scenario, this:
     * 		Shows the login UI
     * 		Continues the login process
     * 
     * Then it fetches the information associated with this user, either from
     * stored values or from Parse.
     * 
     * @param newUser whether this user was just created; used to display that
     * 				  this account is being created
     */
    public void setupUser(boolean newUser);
    /**
     * Log out of current user in the background.
     * 
     * Resets values of objects that correspond to the user so they are fresh
     * for the next user.
     */
    public void logOut(/*boolean force*/);
    
    /**
     * Display the dialog fragment with notes about how scoring works
     */
    public void showScoreDialog();
    /**
     * Display the dialog fragment that allows the user to edit the display name
     * associated with their account
     */
    public void showNameDialog();
    
    //public void loadSetlist();
    /**
     * Get the next question.
     * 
     * Updates the background, if necessary, and fetches more questions.
     */
    public void next();
    
    /**
     * Current id for this question
     * @return id associated with this question in Parse
     */
    public String getQuestionId();
    /**
     * Sets this question's id
     * @param questionId new id for this question; can be null
     */
    public void setQuestionId(String questionId);
    /**
     * Current text for this question
     * @return question text
     */
    public String getQuestion();
    /**
     * Sets the question text for this question
     * @param question new question text; can be null
     */
    public void setQuestion(String question);
    /**
     * Current correct answer for this question
     * @return answer text for this question
     */
    public String getCorrectAnswer();
    /**
     * Sets the answer text for this question
     * @param nextCorrectAnswer new answer for this question; can be null
     */
    public void setCorrectAnswer(String correctAnswer);
    /**
     * Current score for this question
     * @return point value for this question
     */
    public String getQuestionScore();
    /**
     * Updates the current score value for this question
     * @param questionScore new score for this question
     */
    public void setQuestionScore(String questionScore);
    /**
     * Current category for this question
     * @return name of category this question is in
     */
    public String getQuestionCategory();
    /**
     * Sets the new category value for this question
     * @param questionCategory category to be set; can be null
     */
    public void setQuestionCategory(String questionCategory);
    /**
     * Current hint value for first cached question
     * @return true if this question was hinted, false otherwise
     */
    public boolean getQuestionHint();
    /**
     * Sets the new hinted value for this question
     * @param questionHint new hinted value; true if hinted, false
     * 							otherwise
     */
    public void setQuestionHint(boolean questionHint);
    /**
     * Current skip value for first cached question
     * @return true if this question was skipped, false otherwise
     */
    public boolean getQuestionSkip();
    /**
     * Current id for this question
     * @return id associated with this question in Parse
     */
    public String getNextQuestionId();
    /**
     * Sets this question's id
     * @param nextQuestionId new id for this question; can be null
     */
    public void setNextQuestionId(String nextQuestionId);
    /**
     * Current text for this question
     * @return question text
     */
    public String getNextQuestion();
    /**
     * Sets the question text for this question
     * @param nextQuestion new question text; can be null
     */
    public void setNextQuestion(String nextQuestion);
    /**
     * Current correct answer for this question
     * @return answer text for this question
     */
    public String getNextCorrectAnswer();
    /**
     * Sets the answer text for this question
     * @param nextCorrectAnswer new answer for this question; can be null
     */
    public void setNextCorrectAnswer(String nextCorrectAnswer);
    /**
     * Current score for this question
     * @return point value for this question
     */
    public String getNextQuestionScore();
    /**
     * Updates the current score value for this question
     * @param nextQuestionScore new score for this question
     */
    public void setNextQuestionScore(String nextQuestionScore);
    /**
     * Current category for this question
     * @return name of category this question is in
     */
    public String getNextQuestionCategory();
    /**
     * Sets the new category value for this question
     * @param nextQuestionCategory category to be set; can be null
     */
    public void setNextQuestionCategory(String nextQuestionCategory);
    /**
     * Current hint value for second cached question
     * @return true if this question was hinted, false otherwise
     */
    public boolean getNextQuestionHint();
    /**
     * Sets the new hinted value for this question
     * @param nextQuestionHint new hinted value; true if hinted, false
     * 							otherwise
     */
    public void setNextQuestionHint(boolean nextQuestionHint);
    /**
     * Current skip value for second cached question
     * @return true if this question was skipped, false otherwise
     */
    public boolean getNextQuestionSkip();
    /**
     * Current id for this question
     * @return id associated with this question in Parse
     */
    public String getThirdQuestionId();
    /**
     * Sets this question's id
     * @param thirdQuestionId new id for this question; can be null
     */
    public void setThirdQuestionId(String thirdQuestionId);
    /**
     * Current text for this question
     * @return question text
     */
    public String getThirdQuestion();
    /**
     * Sets the question text for this question
     * @param thirdQuestion new question text; can be null
     */
    public void setThirdQuestion(String thirdQuestion);
    /**
     * Current correct answer for this question
     * @return answer text for this question
     */
    public String getThirdCorrectAnswer();
    /**
     * Sets the answer text for this question
     * @param thirdCorrectAnswer new answer for this question; can be null
     */
    public void setThirdCorrectAnswer(String thirdCorrectAnswer);
    /**
     * Current score for this question
     * @return point value for this question
     */
    public String getThirdQuestionScore();
    /**
     * Updates the current score value for this question
     * @param thirdQuestionScore new score for this question
     */
    public void setThirdQuestionScore(String thirdQuestionScore);
    /**
     * Current category for this question
     * @return name of category this question is in
     */
    public String getThirdQuestionCategory();
    /**
     * Sets the new category value for this question
     * @param thirdQuestionCategory category to be set; can be null
     */
    public void setThirdQuestionCategory(String thirdQuestionCategory);
    /**
     * Current hint value for third cached question
     * @return true if this question was hinted, false otherwise
     */
    public boolean getThirdQuestionHint();
    /**
     * Sets the new hinted value for this question
     * @param thirdQuestionHint new hinted value; true if hinted, false
     * 							otherwise
     */
    public void setThirdQuestionHint(boolean thirdQuestionHint);
    /**
     * Current skip value for third cached question
     * @return true if this question was skipped, false otherwise
     */
    public boolean getThirdQuestionSkip();
    
    /**
     * Update the cache of questions.
     * 
     * Three questions and associated information for each are cached in a good
     * state.  It is possible that there are two, one or zero questions cached
     * due to network issues, but we try to keep three cached.
     * 
     * This method will attempt to fetch questions and update the cached objects
     * to hold three.  The action is done in the background so the UI thread is
     * not blocked.
     * 
     * @param force if true, move the queue of cached questions forward before
     * 				getting new questions
     * @param level difficulty of questions to be fetched
     */
    public void getNextQuestions(boolean force, int level);
    
    /**
     * Determines if the current question is new or not
     * @return true if current question is new, false otherwise
     */
    public boolean isNewQuestion();
    /**
     * Indicates if the app is in a new question state
     * @param isNewQuestion true if there is a new question displayed, false
     * 						otherwise
     */
    public void setIsNewQuestion(boolean isNewQuestion);
    
    /**
     * User id associated with current user; from Parse
     * @return user id
     */
    public String getUserId();
    /**
     * Set current user's display name in stats
     * @param displayName new name to be displayed in stats
     */
    public void setDisplayName(String displayName);
    /**
     * Current user's display name in stats
     * @return current display name
     */
    public String getDisplayName();
    
    /**
     * Get the current user's score
     * @return score for the current user
     */
    public int getCurrentScore();
    /**
     * Add given value to current user's score
     * @param addValue value to add
     */
    public void addCurrentScore(int addValue);
    /**
     * Save given score to Parse for the current user
     * @param currTemp score to save
     */
    public void saveUserScore(final int currTemp);
    
    /**
     * Capture the current screen and start a share intent using the file path
     * to the captured screenshot
     */
    public void shareScreenshot();
    /**
     * Indicate that there is currently no network connectivity
     * @param networkProblem true if there is no network connectivity
     */
    public void setNetworkProblem(boolean networkProblem);
    /**
     * Find if there is a network connectivity problem
     * @return true if there is no network connectivity, false otherwise
     */
    public boolean getNetworkProblem();
    
    /**
     * Add the given question id to the user's correct answers
     * @param correctId question id to add
     */
    public void addCorrectAnswer(String correctId);
    /**
     * Checks if the given question id is in the user's correct answers
     * @param correctId question id to check
     * @return true if id is present in correct answers
     */
    public boolean isCorrectAnswer(String correctId);
    
    /**
     * Add a user name for stats, for the current user
     * @param userName text user name for current user
     */
    public void setUserName(String userName);
    /**
     * Check if there is a user being created
     * @return if this user is new
     */
    public boolean isNewUser();
    /**
     * Initiate password reset in Parse for the given user
     * @param username associated with the user that needs password reset
     */
    public void resetPassword(String username);
    /*
    public int getWidth();
    public int getHeight();
    */
    /**
     * Checks if the current state of the app is logging out
     * @return true if logging out, false otherwise
     */
    public boolean isLoggingOut();
    /**
     * Sets the current logging out state
     * @param loggingOut true if user is logging out, false otherwise
     */
    public void setLoggingOut(boolean loggingOut);
    /**
     * Sets the home button in the action bar as up; enables or disables the
     * icon button
     * @param homeAsUp true to enable, false otherwise
     */
    public void setHomeAsUp(boolean homeAsUp);
    /*
    public void showQuickTip(View view, String message);
    public void showQuickTipMenu(ViewGroup view, String message, int location);
    */
    /**
     * Get the app's sliding menu object
     * @return sliding menu used in the activity
     */
    public SlidingMenu slidingMenu();
    /**
     * Refresh the action bar menu item(s).  Used when the pager switches tabs
     */
    public void refreshMenu();
    
    /**
     * Determines if app should first navigate to the setlist
     * @return true if setlist should be viewed first, false otherwise
     */
    public boolean getGoToSetlist();
    /**
     * Set when app enters or leaves setlist tab
     * @param inSetlist true if now in setlist, false otherwise
     */
    public void setInSetlist(boolean inSetlist);
    /**
     * Determine if app is viewing the setlist tab
     * @return true if viewing the setlist
     */
    public boolean getInSetlist();
    
    /**
     * Generate a {@link android.graphics.drawable.BitmapDrawable BitmapDrawable
     * } from the given resource id
     * @param resId id of the image to generate a drawable of
     * @return drawable of image from resource id
     */
    public Bitmap getBitmap(int resId);
}