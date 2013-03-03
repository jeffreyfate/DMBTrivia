package com.jeffthefate.dmbquiz.fragment;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.CheatSheetMenu;
import com.jeffthefate.dmbquiz.OnButtonListener;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.activity.ActivityMain.UiCallback;
import com.parse.Parse;

public class FragmentBase extends Fragment implements UiCallback {
    
    protected OnButtonListener mCallback;
    private ViewGroup toolTipView;
    
    protected RelativeLayout statsButton;
    protected RelativeLayout switchButton;
    protected RelativeLayout reportButton;
    protected RelativeLayout shareButton;
    protected RelativeLayout nameButton;
    protected RelativeLayout exitButton;
    protected RelativeLayout logoutButton;
    protected TextView logoutText;
    
    protected RelativeLayout soundsButton;
    protected CheckedTextView soundsText;
    protected RelativeLayout notificationsButton;
    protected CheckedTextView notificationsText;
    protected RelativeLayout tipsButton;
    protected CheckedTextView tipsText;
    
    protected RelativeLayout followButton;
    protected RelativeLayout likeButton;
    
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
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	toolTipView = (ViewGroup) inflater.inflate(R.layout.tooltip,
                (ViewGroup) getActivity().findViewById(R.id.ToolTipLayout));
		return null;
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
    
    protected Drawable getBackgroundDrawable(String name) {
    	if (name == null)
    		return res.getDrawable(res.getIdentifier("splash8", "drawable",
    				getActivity().getPackageName()));
    	else
    		return res.getDrawable(res.getIdentifier(name,	"drawable",
    				getActivity().getPackageName()));
    }
    
    public void openStats() {
    	if (mCallback != null) {
        	if (!sharedPrefs.contains(getString(R.string.stats_key)) ||
        			sharedPrefs.getBoolean(getString(R.string.quicktip_key),
        					false)) {
                sharedPrefs.edit().putBoolean(getString(R.string.stats_key),
                		true).commit();
                CheatSheetMenu.setup(toolTipView, "Touch your score to " +
                		"enter Stats & Standings", mCallback.getWidth(),
                		mCallback.getHeight());
            }
            mCallback.onStatsPressed();
        }
    }
    
    public void switchBackground() {
    	if (mCallback != null)
            mCallback.setBackground(mCallback.getBackground(), true);
    }
    
    public void toggleSounds() {
        sharedPrefs.edit().putBoolean(getString(R.string.sound_key),
                !sharedPrefs.getBoolean(getString(R.string.sound_key),
                        true))
            .commit();
    }
    
    public void toggleNotifications() {
        sharedPrefs.edit().putBoolean(getString(R.string.notification_key),
                !sharedPrefs.getBoolean(
                        getString(R.string.notification_key), true))
            .commit();
    }
    
    public void toggleTips() {
        sharedPrefs.edit().putBoolean(getString(R.string.quicktip_key),
                !sharedPrefs.getBoolean(
                        getString(R.string.quicktip_key), true))
            .commit();
    }
    
    public void report() {
    	ApplicationEx.reportQuestion(mCallback.getQuestionId(),
                mCallback.getQuestion(), mCallback.getCorrectAnswer(),
                mCallback.getQuestionScore());
    }
    
    public void changeName() {
    	if (mCallback != null)
            mCallback.showNameDialog();
    }
    
    public void logOut() {
    	if (mCallback != null) {
            ApplicationEx.dbHelper.setOffset(0, mCallback.getUserId());
            mCallback.setLoggingOut(true);
            mCallback.setQuestionId(null);
            mCallback.setQuestion(null);
            mCallback.setCorrectAnswer(null);
            mCallback.setQuestionCategory(null);
            mCallback.setQuestionScore(null);
            mCallback.setNextQuestionId(null);
            mCallback.setNextQuestion(null);
            mCallback.setNextCorrectAnswer(null);
            mCallback.setNextQuestionCategory(null);
            mCallback.setNextQuestionScore(null);
            mCallback.setThirdQuestionId(null);
            mCallback.setThirdQuestion(null);
            mCallback.setThirdCorrectAnswer(null);
            mCallback.setThirdQuestionCategory(null);
            mCallback.setThirdQuestionScore(null);
            mCallback.logOut(true);
        }
    }
    
    public void shareScreen() {
    	if (mCallback != null)
            mCallback.shareScreenshot();
    }
    
    public void exit() {
    	getActivity().moveTaskToBack(true);
    }
    
    public void follow() {
    	startActivity(getOpenTwitterIntent());
    }
    
    public void like() {
    	startActivity(getOpenFacebookIntent());
    }

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

	@Override
	public void setBackground(Drawable background) {}
	
	@Override
	public Drawable getBackground() {return null;}

	@Override
	public void toggleMenu() {}
    
}
