package com.jeffthefate.dmbquiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import com.jeffthefate.dmbquiz.activity.ActivityMain.YearComparator;

public class SavedInstance implements Serializable {

	private static final long serialVersionUID = -4719146721243365036L;
	
	private boolean loggedIn;
	private boolean isLogging;
	private boolean loggingOut;
	private boolean inLoad;
	private boolean inStats;
	private boolean inInfo;
	private boolean inFaq;
	private boolean inSetlist;
	private boolean inChooser;
	private boolean newQuestion;
	private boolean newUser;
	private boolean networkProblem;
	
	private String portBackground;
	private String landBackground;
	private String userId;
	private String displayName;
	
	private int currScore;
	
	private ArrayList<String> questionIds;
	private ArrayList<String> questions;
	private ArrayList<String> questionAnswers;
	private ArrayList<String> questionCategories;
	private ArrayList<String> questionScores;
	private ArrayList<String> questionHints;
	private ArrayList<String> questionSkips;
	private ArrayList<String> correctAnswers;
	
	private TreeMap<String, TreeMap<String, String>> setlistMap;
	
	private SetInfo latestSet;
	private SetInfo selectedSet;
	
	public SavedInstance() {
		loggedIn = false;
		isLogging = false;
		loggingOut = false;
		inLoad = false;
		inStats = false;
		inInfo = false;
		inFaq = false;
		inSetlist = false;
		inChooser = false;
		newQuestion = false;
		newUser = false;
		networkProblem = false;
		questionIds = new ArrayList<String>(Constants.CACHED_QUESTIONS);
		questions = new ArrayList<String>(Constants.CACHED_QUESTIONS);
		questionAnswers = new ArrayList<String>(Constants.CACHED_QUESTIONS);
		questionCategories = new ArrayList<String>(
				Constants.CACHED_QUESTIONS);
		questionScores = new ArrayList<String>(Constants.CACHED_QUESTIONS);
		questionHints = new ArrayList<String>(Constants.CACHED_QUESTIONS);
		questionSkips = new ArrayList<String>(Constants.CACHED_QUESTIONS);
		setlistMap = new TreeMap<String, TreeMap<String, String>>(
				new YearComparator());
		latestSet = null;
		selectedSet = null;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public boolean isLogging() {
		return isLogging;
	}

	public void setLogging(boolean isLogging) {
		this.isLogging = isLogging;
	}
	
	public boolean isLoggingOut() {
		return loggingOut;
	}
	
	public void setLoggingOut(boolean loggingOut) {
		this.loggingOut = loggingOut;
	}

	public boolean isInLoad() {
		return inLoad;
	}

	public void setInLoad(boolean inLoad) {
		this.inLoad = inLoad;
	}

	public boolean isInStats() {
		return inStats;
	}

	public void setInStats(boolean inStats) {
		this.inStats = inStats;
	}

	public boolean isInInfo() {
		return inInfo;
	}

	public void setInInfo(boolean inInfo) {
		this.inInfo = inInfo;
	}

	public boolean isInFaq() {
		return inFaq;
	}

	public void setInFaq(boolean inFaq) {
		this.inFaq = inFaq;
	}

	public boolean isInSetlist() {
		return inSetlist;
	}

	public void setInSetlist(boolean inSetlist) {
		this.inSetlist = inSetlist;
	}

	public boolean isInChooser() {
		return inChooser;
	}

	public void setInChooser(boolean inChooser) {
		this.inChooser = inChooser;
	}

	public boolean isNewQuestion() {
		return newQuestion;
	}

	public void setNewQuestion(boolean newQuestion) {
		this.newQuestion = newQuestion;
	}

	public boolean isNewUser() {
		return newUser;
	}

	public void setNewUser(boolean newUser) {
		this.newUser = newUser;
	}

	public boolean isNetworkProblem() {
		return networkProblem;
	}

	public void setNetworkProblem(boolean networkProblem) {
		this.networkProblem = networkProblem;
	}

	public String getPortBackground() {
		return portBackground;
	}

	public void setPortBackground(String portBackground) {
		this.portBackground = portBackground;
	}

	public String getLandBackground() {
		return landBackground;
	}

	public void setLandBackground(String landBackground) {
		this.landBackground = landBackground;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getCurrScore() {
		return currScore;
	}

	public void setCurrScore(int currScore) {
		this.currScore = currScore;
	}

	public ArrayList<String> getQuestionIds() {
		return questionIds;
	}

	public void setQuestionIds(ArrayList<String> questionIds) {
		this.questionIds = questionIds;
	}

	public ArrayList<String> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<String> questions) {
		this.questions = questions;
	}

	public ArrayList<String> getQuestionAnswers() {
		return questionAnswers;
	}

	public void setQuestionAnswers(ArrayList<String> questionAnswers) {
		this.questionAnswers = questionAnswers;
	}

	public ArrayList<String> getQuestionCategories() {
		return questionCategories;
	}

	public void setQuestionCategories(ArrayList<String> questionCategories) {
		this.questionCategories = questionCategories;
	}

	public ArrayList<String> getQuestionScores() {
		return questionScores;
	}

	public void setQuestionScores(ArrayList<String> questionScores) {
		this.questionScores = questionScores;
	}

	public ArrayList<String> getQuestionHints() {
		return questionHints;
	}

	public void setQuestionHints(ArrayList<String> questionHints) {
		this.questionHints = questionHints;
	}

	public ArrayList<String> getQuestionSkips() {
		return questionSkips;
	}

	public void setQuestionSkips(ArrayList<String> questionSkips) {
		this.questionSkips = questionSkips;
	}

	public ArrayList<String> getCorrectAnswers() {
		return correctAnswers;
	}

	public void setCorrectAnswers(ArrayList<String> correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	public TreeMap<String, TreeMap<String, String>> getSetlistMap() {
		return setlistMap;
	}

	public void setSetlistMap(
			TreeMap<String, TreeMap<String, String>> setlistMap) {
		this.setlistMap = setlistMap;
	}
	
	public SetInfo getLatestSet() {
		return latestSet;
	}

	public void setLatestSet(SetInfo latestSet) {
		this.latestSet = latestSet;
	}

	public SetInfo getSelectedSet() {
		return selectedSet;
	}

	public void setSelectedSet(SetInfo selectedSet) {
		this.selectedSet = selectedSet;
	}
	
}
