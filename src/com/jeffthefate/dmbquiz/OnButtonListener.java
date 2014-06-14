package com.jeffthefate.dmbquiz;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.jeffthefate.dmbquiz.fragment.FragmentBase;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public interface OnButtonListener {
	/**
	 * Set the (non-selist) background image.
	 * @param name		name of resource to be applied as the background
	 * @param showNew	true if changing the background, false if setting it for
	 * 					the first time (initializing)
	 */
    public boolean setBackground(String name, boolean showNew, String screen);
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
     * Determines if there are any question ids
     * @return true if empty, false otherwise
     */
    public boolean questionIdsEmpty();
    /**
     * Current id for this question
     * @return id associated with this question in Parse
     */
    public String getQuestionId(int index);
    /**
     * Sets this question's id
     * @param questionId new id for this question; can be null
     */
    public void addQuestionId(String questionId);
    /**
     * Clear question id array
     */
    public void clearQuestionIds();
    /**
     * Current text for this question
     * @return question text
     */
    public String getQuestion(int index);
    /**
     * Sets the question text for this question
     * @param question new question text; can be null
     */
    public void addQuestion(String question);
    /**
     * Clear question array
     */
    public void clearQuestions();
    /**
     * Current correct answer for this question
     * @return answer text for this question
     */
    public String getQuestionAnswer(int index);
    /**
     * Sets the answer text for this question
     * @param nextCorrectAnswer new answer for this question; can be null
     */
    public void addQuestionAnswer(String correctAnswer);
    /**
     * Clear correct answer array
     */
    public void clearQuestionAnswers();
    /**
     * Current score for this question
     * @return point value for this question
     */
    public String getQuestionScore(int index);
    /**
     * Updates the current score value for this question
     * @param questionScore new score for this question
     */
    public void addQuestionScore(String questionScore);
    /**
     * Clear question score array
     */
    public void clearQuestionScores();
    /**
     * Current category for this question
     * @return name of category this question is in
     */
    public String getQuestionCategory(int index);
    /**
     * Sets the new category value for this question
     * @param questionCategory category to be set; can be null
     */
    public void addQuestionCategory(String questionCategory);
    /**
     * Clear question category array
     */
    public void clearQuestionCategories();
    /**
     * Current hint value for first cached question
     * @return true if this question was hinted, false otherwise
     */
    public boolean getQuestionHint(int index);
    /**
     * Sets the new hinted value for this question
     * @param questionHint new hinted value; true if hinted, false
     * 							otherwise
     */
    public void setQuestionHint(boolean questionHint, int index);
    public void addQuestionHint(boolean questionHint);
    /**
     * Clear question hint array
     */
    public void clearQuestionHints();
    /**
     * Current skip value for first cached question
     * @return true if this question was skipped, false otherwise
     */
    public boolean getQuestionSkip(int index);
    /**
     * Clear question skip array
     */
    public void clearQuestionSkips();
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
    public void shareScreenshot(boolean isSetlist);
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
    
    public void setGoToSetlist(boolean goToSetlist);
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
    
    public void setCurrFrag(FragmentBase currFrag);
    
    public FragmentBase getCurrFrag();
    
    public void updateLevel();
    
    public void updateSetlistMap(String setDate, String setVenue,
    		String setCity, String setlist);
}