package com.jeffthefate.dmbquiz_dev.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.jeffthefate.dmbquiz_dev.ApplicationEx;
import com.jeffthefate.dmbquiz_dev.R;
import com.jeffthefate.dmbquiz_dev.activity.ActivityMain.UiCallback;
import com.parse.Parse;

public class FragmentBase extends Fragment implements UiCallback {
    
    OnButtonListener mCallback;
    
    public interface OnButtonListener {
        public String setBackground(String name, boolean showNew);
        public String getBackground();
        public void onInfoPressed();
        public void onStatsPressed();
        public void onLoginPressed(int loginType, String user, String pass);
        public void setupUser();
        public void showScoreDialog();
        public void showNameDialog();
        public void logOut(boolean force);
        public Bundle getLeadersState();
        public void next();
        public String getQuestionId();
        public void setQuestionId(String questionId);
        public String getQuestion();
        public void setQuestion(String question);
        public String getCorrectAnswer();
        public void setCorrectAnswer(String correctAnswer);
        public String getQuestionScore();
        public void setQuestionScore(String questionScore);
        public String getQuestionCategory();
        public void setQuestionCategory(String questionCategory);
        public String getNextQuestionId();
        public void setNextQuestionId(String nextQuestionId);
        public String getNextQuestion();
        public void setNextQuestion(String nextQuestion);
        public String getNextCorrectAnswer();
        public void setNextCorrectAnswer(String nextCorrectAnswer);
        public String getNextQuestionScore();
        public void setNextQuestionScore(String nextQuestionScore);
        public String getNextQuestionCategory();
        public void setNextQuestionCategory(String nextQuestionCategory);
        public String getThirdQuestionId();
        public void setThirdQuestionId(String thirdQuestionId);
        public String getThirdQuestion();
        public void setThirdQuestion(String thirdQuestion);
        public String getThirdCorrectAnswer();
        public void setThirdCorrectAnswer(String thirdCorrectAnswer);
        public String getThirdQuestionScore();
        public void setThirdQuestionScore(String thirdQuestionScore);
        public String getThirdQuestionCategory();
        public void setThirdQuestionCategory(String thirdQuestionCategory);
        public void nextQuestion();
        public String getUserId();
        public String getDisplayName();
        public ArrayList<String> getAnswerIds();
        public void addAnswerId(String answerId);
        public boolean hasAnswerId(String answerId);
        public boolean isNewQuestion();
        public void setIsNewQuestion(boolean isNewQuestion);
        public int getCurrentScore();
        public void addCurrentScore(int addValue);
        public void shareScreenshot();
        public int getQuestionsLeft();
        public void setDisplayName(String displayName);
        public boolean getNetworkProblem();
        public void setNetworkProblem(boolean networkProblem);
        public void saveUserScore(final int currTemp);
    }
    
    public static final int LOGIN_FACEBOOK = 0;
    public static final int LOGIN_TWITTER = 1;
    public static final int LOGIN_ANON = 2;
    public static final int SIGNUP_EMAIL = 3;
    public static final int LOGIN_EMAIL = 4;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        res = getResources();
        try {
            mCallback = (OnButtonListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        TEST
        Parse.initialize(getActivity(),
                "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
                "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
        */
        Parse.initialize(getActivity(),
                        "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
                        "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
        setHasOptionsMenu(false);
        audioManager = (AudioManager) getActivity().getSystemService(
                Context.AUDIO_SERVICE);
        fields = R.raw.class.getFields();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                ApplicationEx.getApp());
    }
    
    protected Resources res;
    protected SharedPreferences sharedPrefs;

    @Override
    public void showNetworkProblem() {}

    @Override
    public void showNoMoreQuestions() {}
    
    protected Intent getOpenFacebookIntent() {

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

    protected Intent getOpenTwitterIntent() {
        Uri uri = Uri.parse("http://www.twitter.com/dmbtrivia");
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    OnAudioFocusChangeListener afChangeListener = 
        new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                }
                break;
            }
        }
    };

    public void getAudioFocus(int resource) {
        audioManager.abandonAudioFocus(afChangeListener);
        int result = audioManager.requestAudioFocus(afChangeListener, 
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(ApplicationEx.getApp(),
                        resource);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        audioManager.abandonAudioFocus(afChangeListener);
                    } 
                });
                mediaPlayer.setOnErrorListener(new OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what,
                            int extra) {
                        audioManager.abandonAudioFocus(afChangeListener);
                        mp.reset();
                        mp.release();
                        return true;
                    } 
                });
            } catch (IllegalStateException e) {}
        }
    }
    
    /*
     * Play audio for correct/wrong/skip/hint if sound is enabled
     */
    protected void playAudio(String type) {
        do {
            rawIndex = (int) (Math.random()*fields.length);
        } while (!fields[rawIndex].getName().contains(type));
        try {
            currentAudio = fields[rawIndex].getInt(null);
        } catch (IllegalArgumentException e1) {e1.printStackTrace();
        } catch (IllegalAccessException e1) {e1.printStackTrace();}
        if (PreferenceManager.getDefaultSharedPreferences(
                ApplicationEx.getApp()).getBoolean(
                        getString(R.string.sound_key), false))
            getAudioFocus(currentAudio);
    }
    
    private Field[] fields;
    private int rawIndex = -1;
    private int currentAudio;
    
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    
    @Override
    public void onPause() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        audioManager.abandonAudioFocus(afChangeListener);
        super.onPause();
    }

    @Override
    public void updateTimerButtons() {}

    @Override
    public void updateScoreText() {}

    @Override
    public void resumeQuestion() {}
    
    @Override
    public void showLoading(String message) {}
    
    @Override
    public void resetHint() {}
    
    @Override
    public void disableButton(boolean isRetry) {}
    
    @Override
    public void enableButton(boolean isRetry) {}
    
    @Override
    public void setDisplayName(String displayName) {}
    
}
