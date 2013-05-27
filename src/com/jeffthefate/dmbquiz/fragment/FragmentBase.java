package com.jeffthefate.dmbquiz.fragment;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.OnButtonListener;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.activity.ActivityMain.UiCallback;
import com.parse.Parse;
//import android.graphics.drawable.TransitionDrawable;

public class FragmentBase extends Fragment implements UiCallback {
    
    protected OnButtonListener mCallback;
    
    protected ImageViewEx background;
    
    public static final int LOGIN_FACEBOOK = 0;
    public static final int LOGIN_TWITTER = 1;
    public static final int LOGIN_ANON = 2;
    public static final int SIGNUP_EMAIL = 3;
    public static final int LOGIN_EMAIL = 4;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnButtonListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonListener");
        }
    }

    @SuppressLint("NewApi")
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
    }
    /*
    @Override
    public void onDestroyView() {
        if (background != null) {
            background.setImageDrawable(null);
            Drawable drawable = background.getDrawable();
            if (drawable != null) {
                if (drawable instanceof BitmapDrawableEx) {
                    BitmapDrawableEx bitmapDrawable = (BitmapDrawableEx) drawable;
                    bitmapDrawable.setIsDisplayed(false);
                }
            }
        }
        super.onDestroyView();
    }
    */
    /*
    @Override
    public void onDestroyView() {
        if (background != null) {
            Drawable drawable = background.getDrawable();
            if (drawable != null) {
                /*
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    bitmap.recycle();
                }
                if (drawable instanceof TransitionDrawable) {
                    TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
                    Bitmap bitmap = ((BitmapDrawable)transitionDrawable.getDrawable(0)).getBitmap();
                    if (bitmap != null)
                        bitmap.recycle();
                    /*
                    bitmap = ((BitmapDrawable)transitionDrawable.getDrawable(1)).getBitmap();
                    if (bitmap != null)
                        bitmap.recycle();
                }
            }
        }
        super.onDestroyView();
    }
    */
    @Override
    public void showNetworkProblem() {}

    @Override
    public void showNoMoreQuestions(int level) {}

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
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null) {
                	try {
	                    if (mediaPlayer.isPlaying())
	                        mediaPlayer.stop();
                	} catch (IllegalStateException e) {}
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
                    mediaPlayer = null;
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
                        try {
	                        mp.reset();
	                        mp.release();
                        } catch (IllegalStateException e) {
                        } catch (NullPointerException e) {}
                        return true;
                    } 
                });
            } catch (IllegalStateException e) {
            } catch (NullPointerException e) {}
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
        } catch (IllegalArgumentException e1) {
        	currentAudio = R.raw.correct1;
        	e1.printStackTrace();
        } catch (IllegalAccessException e1) {
        	currentAudio = R.raw.correct1;
        	e1.printStackTrace();
    	}
        if (SharedPreferencesSingleton.instance().getBoolean(
                		ResourcesSingleton.instance().getString(R.string.sound_key), false))
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
    
    protected void setBackgroundBitmap(String name, String screen) {
        Bitmap backgroundBitmap = ApplicationEx.getBackgroundBitmap();
        if (backgroundBitmap == null) {
            Log.i(Constants.LOG_TAG, "setBackgroundBitmap");
        	if (name == null)
        	    mCallback.setBackground("splash4", false, screen);
        	else
        	    mCallback.setBackground(name, false, screen);
        }
        else {
            if (background != null) {
                /*
                if (backgroundDrawable instanceof TransitionDrawable) {
                    Drawable tempDrawable =
                        ((TransitionDrawable)backgroundDrawable).getDrawable(1);
                    if (tempDrawable != null)
                        background.setImageDrawable(tempDrawable);
                    else {
                        Log.i(Constants.LOG_TAG, "setBackgroundBitmap");
                        if (name == null)
                            mCallback.setBackground("splash4", false, screen);
                        else
                            mCallback.setBackground(name, false, screen);
                    }
                }
                else
                    background.setImageDrawable(backgroundDrawable);
                */
            	background.setImageDrawable(null);
            	BitmapDrawable bitmapDrawable = new BitmapDrawable(
            			ResourcesSingleton.instance(), backgroundBitmap);
            	if (screen.equalsIgnoreCase("info") ||
            			screen.equalsIgnoreCase("leaders"))
            		bitmapDrawable.setColorFilter(new PorterDuffColorFilter(
                			ResourcesSingleton.instance().getColor(
                					R.color.background_dark),
        					PorterDuff.Mode.SRC_ATOP));
            	else
            		bitmapDrawable.setColorFilter(null);
                background.setImageDrawable(bitmapDrawable);
            }
        }
    }
    
    @SuppressLint("NewApi")
	public void toggleSounds() {
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
    		SharedPreferencesSingleton.instance().edit().putBoolean(
    				ResourcesSingleton.instance().getString(R.string.sound_key),
    				!SharedPreferencesSingleton.instance().getBoolean(
    						ResourcesSingleton.instance().getString(R.string.sound_key),true))
				.commit();
    	else
    		SharedPreferencesSingleton.instance().edit().putBoolean(
    				ResourcesSingleton.instance().getString(R.string.sound_key),
    				!SharedPreferencesSingleton.instance().getBoolean(
    						ResourcesSingleton.instance().getString(R.string.sound_key),true))
				.apply();
    }
    
    @SuppressLint("NewApi")
	public void toggleNotifications() {
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
	    	SharedPreferencesSingleton.instance().edit().putBoolean(
	    			ResourcesSingleton.instance().getString(R.string.notification_key),
	                !SharedPreferencesSingleton.instance().getBoolean(
	                		ResourcesSingleton.instance().getString(R.string.notification_key), true))
	            .commit();
    	else
	    	SharedPreferencesSingleton.instance().edit().putBoolean(
	    			ResourcesSingleton.instance().getString(R.string.notification_key),
	                !SharedPreferencesSingleton.instance().getBoolean(
	                		ResourcesSingleton.instance().getString(R.string.notification_key), true))
	            .apply();
    }
    
    @SuppressLint("NewApi")
	public void toggleTips() {
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
	    	SharedPreferencesSingleton.instance().edit().putBoolean(
	    			ResourcesSingleton.instance().getString(R.string.quicktip_key),
	                !SharedPreferencesSingleton.instance().getBoolean(
	                		ResourcesSingleton.instance().getString(R.string.quicktip_key), true))
	            .commit();
    	else
	    	SharedPreferencesSingleton.instance().edit().putBoolean(
	    			ResourcesSingleton.instance().getString(R.string.quicktip_key),
	                !SharedPreferencesSingleton.instance().getBoolean(
	                		ResourcesSingleton.instance().getString(R.string.quicktip_key), true))
	            .apply();
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
            DatabaseHelperSingleton.instance().setOffset(0, mCallback.getUserId());
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
            mCallback.logOut();
        }
    }
    
    public void shareScreen() {
    	if (mCallback != null)
            mCallback.shareScreenshot();
    }
    
    public void exit() {
    	getActivity().moveTaskToBack(true);
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
    /*
    @Override
	public void setBackground(Bitmap newBackground) {
    	if (background != null && newBackground != null) {
    		background.setImageBitmap(null);
			background.setImageBitmap(newBackground);
    	}
    }
    */
    @Override
    public void setBackground(Bitmap newBackground) {
        if (background != null && newBackground != null) {
        	background.setImageDrawable(null);
        	BitmapDrawable bitmapDrawable = new BitmapDrawable(
        			ResourcesSingleton.instance(), newBackground);
        	bitmapDrawable.setColorFilter(null);
            background.setImageDrawable(bitmapDrawable);
        }
    }
	
	@Override
	public Drawable getBackground() {
		if (background == null)
    		return null;
    	else
    		return background.getDrawable();
	}
	
    @Override
    public void updateSetText() {}

    @Override
    public void showRetry() {}

    @Override
    public int getPage() {
        return 0;
    }
    
    @Override
    public void setPage(int page) {}

}
