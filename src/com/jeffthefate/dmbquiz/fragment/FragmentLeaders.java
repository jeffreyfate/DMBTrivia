package com.jeffthefate.dmbquiz.fragment;

import java.util.ArrayList;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.LeaderAdapter;
import com.jeffthefate.dmbquiz.R;
import com.parse.ParseUser;

public class FragmentLeaders extends FragmentBase {
    
    private int answerCount = 0;
    private int hintCount = 0;
    
    private ViewGroup leadersLayout;
    
    private TextView userText;
    private TextView userAnswerText;
    private TextView userAnswers;
    private TextView userHintText;
    private TextView userHints;
    
    private TextView leaderText;
    private ListView leaderList;
    
    private TextView userName;
    private TextView userScore;
    
    private TextView createdText;
    private TextView createdDate;
    
    private ArrayList<String> rankList;
    private ArrayList<String> userList;
    private ArrayList<String> scoreList;
    private ArrayList<String> userIdList;
    
    private Resources res;
    
    public FragmentLeaders() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        setHasOptionsMenu(true);
    }
    
    private boolean isRestored = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leaders, container, false);
        leadersLayout = (ViewGroup) view.findViewById(R.id.ListLayout);
        userText = (TextView) view.findViewById(R.id.UserText);
        userAnswerText = (TextView) view.findViewById(R.id.Stat1Name);
        userAnswers = (TextView) view.findViewById(R.id.Stat1Score);
        userHintText = (TextView) view.findViewById(R.id.Stat2Name);
        userHints = (TextView) view.findViewById(R.id.Stat2Score);
        leaderText = (TextView) view.findViewById(R.id.LeadersText);
        leaderList = (ListView) view.findViewById(android.R.id.list);
        userName = (TextView) view.findViewById(R.id.UserName);
        userScore = (TextView) view.findViewById(R.id.UserScore);
        createdText = (TextView) view.findViewById(R.id.LastQuestionText);
        createdDate = (TextView) view.findViewById(R.id.LastQuestionDate);
        if (savedInstanceState != null) {
            userText.setText(savedInstanceState.getString("userText"));
            userAnswerText.setText(savedInstanceState.getString(
                    "userAnswerText"));
            userAnswers.setText(savedInstanceState.getString("userAnswers"));
            userHintText.setText(savedInstanceState.getString("userHintText"));
            userHints.setText(savedInstanceState.getString("userHints"));
            userName.setText(savedInstanceState.getString("userName"));
            userScore.setText(savedInstanceState.getString("userScore"));
            leaderText.setText(savedInstanceState.getString("leaderText"));
            rankList = savedInstanceState.getStringArrayList("rank");
            userList = savedInstanceState.getStringArrayList("user");
            scoreList = savedInstanceState.getStringArrayList("score");
            userIdList = savedInstanceState.getStringArrayList("userIdList");
            createdText.setText(savedInstanceState.getString("createdText"));
            createdDate.setText(savedInstanceState.getString("createdDate"));
            if (mCallback != null && mCallback.getUserId() != null &&
                    rankList != null && userList != null && scoreList != null &&
                    userIdList != null) {
                leaderList.setAdapter(new LeaderAdapter(ApplicationEx.getApp(),
                        mCallback.getUserId(), rankList, userList, scoreList, userIdList,
                        R.layout.row_standings, new String[] {"name", "score"},
                        new int[] {R.id.text1, R.id.text2}));
                leadersLayout.setBackgroundColor(
                        res.getColor(R.color.background_dark));
                isRestored = true;
            }
        }
        else if (mCallback != null && (mCallback.getLeadersState() == null ||
                mCallback.getLeadersState().isEmpty())) {
            if (mCallback.getUserId() != null) {
                userText.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_USER_TEXT, mCallback.getUserId()));
                userAnswerText.setText(
                        ApplicationEx.dbHelper.getUserStringValue(
                                DatabaseHelper.COL_USER_ANSWER_TEXT,
                                mCallback.getUserId()));
                userAnswers.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_USER_ANSWERS,
                                mCallback.getUserId()));
                userHintText.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_USER_HINT_TEXT,
                                mCallback.getUserId()));
                userHints.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_USER_HINTS, mCallback.getUserId()));
                userName.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_USER_NAME_TEXT,
                                mCallback.getUserId()));
                userScore.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_USER_SCORE_TEXT,
                                mCallback.getUserId()));
                leaderText.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_LEADER_TEXT, mCallback.getUserId()));
                createdText.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_CREATED_TEXT,
                                mCallback.getUserId()));
                createdDate.setText(ApplicationEx.dbHelper.getUserStringValue(
                        DatabaseHelper.COL_CREATED_DATE,
                                mCallback.getUserId()));
                rankList = ApplicationEx.dbHelper.getLeaderRanks();
                userList = ApplicationEx.dbHelper.getLeaderUsers();
                scoreList = ApplicationEx.dbHelper.getLeaderScores();
                userIdList = ApplicationEx.dbHelper.getLeaderIds();
                if (mCallback.getUserId() != null && rankList != null &&
                        userList != null && scoreList != null &&
                        userIdList != null) {
                    leaderList.setAdapter(new LeaderAdapter(
                            ApplicationEx.getApp(), mCallback.getUserId(),
                            rankList, userList, scoreList, userIdList,
                            R.layout.row_standings,
                            new String[] {"name", "score"},
                            new int[] {R.id.text1, R.id.text2}));
                    leadersLayout.setBackgroundColor(
                            res.getColor(R.color.background_dark));
                    isRestored = true;
                }
            }
        }
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("userText", userText.getText().toString());
        outState.putString("userAnswerText",
                userAnswerText.getText().toString());
        outState.putString("userAnswers", userAnswers.getText().toString());
        outState.putString("userHintText", userHintText.getText().toString());
        outState.putString("userHints", userHints.getText().toString());
        outState.putString("userName", userName.getText().toString());
        outState.putString("userScore", userScore.getText().toString());
        outState.putString("leaderText", leaderText.getText().toString());
        outState.putStringArrayList("rank", rankList);
        outState.putStringArrayList("user", userList);
        outState.putStringArrayList("score", scoreList);
        outState.putStringArrayList("userIdList", userIdList);
        outState.putString("createdText", createdText.getText().toString());
        outState.putString("createdDate", createdDate.getText().toString());
        super.onSaveInstanceState(outState);
    }
    
    private String userNameText;
    private String userScoreText;
    private ParseUser user;
    
    @Override
    public void onPause() {
        ApplicationEx.dbHelper.setUserValue(userText.getText().toString(),
                DatabaseHelper.COL_USER_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(userAnswerText.getText().toString(),
                DatabaseHelper.COL_USER_ANSWER_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(userAnswers.getText().toString(),
                DatabaseHelper.COL_USER_ANSWERS, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(userHintText.getText().toString(),
                DatabaseHelper.COL_USER_HINT_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(userHints.getText().toString(),
                DatabaseHelper.COL_USER_HINTS, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(userName.getText().toString(),
                DatabaseHelper.COL_USER_NAME_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(userScore.getText().toString(),
                DatabaseHelper.COL_USER_SCORE_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(leaderText.getText().toString(),
                DatabaseHelper.COL_LEADER_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(createdText.getText().toString(),
                DatabaseHelper.COL_CREATED_TEXT, mCallback.getUserId());
        ApplicationEx.dbHelper.setUserValue(createdDate.getText().toString(),
                DatabaseHelper.COL_CREATED_DATE, mCallback.getUserId());
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null && !sharedPrefs.contains(
                getString(R.string.dialog_key))) {
            mCallback.showScoreDialog();
            sharedPrefs.edit().putBoolean(getString(R.string.dialog_key), true)
                    .commit();
        }
        if (!isRestored && mCallback != null &&
                mCallback.getLeadersState() != null) {
            Bundle leadersBundle = mCallback.getLeadersState();
            userAnswers.setText(leadersBundle.getString("userAnswers"));
            userHints.setText(leadersBundle.getString("userHints"));
            userName.setText(leadersBundle.getString("userName"));
            userScore.setText(leadersBundle.getString("userScore"));
            rankList = leadersBundle.getStringArrayList("rank");
            userList = leadersBundle.getStringArrayList("user");
            scoreList = leadersBundle.getStringArrayList("score");
            userIdList = leadersBundle.getStringArrayList("userIdList");
            createdText.setText("Latest question");
            createdDate.setText(leadersBundle.getString("lastQuestion"));
            if (mCallback.getUserId() != null && rankList != null &&
                    userList != null && scoreList != null &&
                    userIdList != null) {
                leaderList.setAdapter(new LeaderAdapter(ApplicationEx.getApp(),
                        mCallback.getUserId(), rankList, userList, scoreList,
                        userIdList, R.layout.row_standings,
                        new String[] {"name", "score"},
                        new int[] {R.id.text1, R.id.text2}));
                leadersLayout.setBackgroundColor(
                        res.getColor(R.color.background_dark));
            }
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats, menu);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.Notifications).setTitle(sharedPrefs.getBoolean(
                getString(R.string.notification_key), true) ?
                        "Disable Notifications" : "Enable Notifications");
        super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.NameMenu:
            if (mCallback != null)
                mCallback.showNameDialog();
            break;
        case R.id.ScreenMenu:
            if (mCallback != null)
                mCallback.shareScreenshot();
            break;
        case R.id.Notifications:
            sharedPrefs.edit().putBoolean(getString(R.string.notification_key),
                    !sharedPrefs.getBoolean(
                            getString(R.string.notification_key), true))
                .commit();
            break;
        case R.id.BackMenu:
            getActivity().onBackPressed();
            break;
        default:
            super.onOptionsItemSelected(item);
            break;
        }
        return true;
    }
    
    @Override
    public void setDisplayName(String displayName) {
        if (userName != null && displayName != null) {
            userName.setText(displayName);
            if (mCallback != null)
                mCallback.setUserName(displayName);
        }
    }
    
}