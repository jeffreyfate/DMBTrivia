package com.jeffthefate.dmbquiz;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jeffthefate.stacktrace.ExceptionHandler;
import com.jeffthefate.stacktrace.ExceptionHandler.OnStacktraceListener;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;
import com.parse.SaveCallback;

/**
 * Used as a holder of many values and objects for the entire application.
 * 
 * @author Jeff Fate
 */
@SuppressLint("ShowToast")
public class ApplicationEx extends Application implements OnStacktraceListener {
    /**
     * The application's context
     */
    private static Context app;
    public static DatabaseHelper dbHelper;
    private static boolean mHasWifi = false;
    private static boolean mHasConnection = false;
    private static boolean mIsActive = false;
    private static ConnectivityManager connMan;
    public static String cacheLocation = null;
    public static Toast mToast;
    
    @Override
    public void onCreate() {
        super.onCreate();
        // make sure AsyncTask is loaded in the Main thread
        // https://code.google.com/p/android/issues/detail?id=20915
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }
        };
        app = this;
        mToast = Toast.makeText(app, "", Toast.LENGTH_LONG);
        dbHelper = DatabaseHelper.getInstance();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            cacheLocation = getExternalCacheDir().getAbsolutePath();
            File path = new File(cacheLocation + Constants.SCREENS_LOCATION);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                path.setExecutable(true, false);
                path.setReadable(true, false);
                path.setWritable(true, false);
            }
            path.mkdirs();
        }
        /*
        Parse.initialize(this, "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
                "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
        */
        Parse.initialize(this, "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
                "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
        ParseFacebookUtils.initialize("463296083721286");
        ParseTwitterUtils.initialize("xWnkCrbGRNGMVs2HDyShQ",
                "xaDerd1mUtfmjyuANARkuvNBrQFgsVpQmhYWDjnirOw");
        ExceptionHandler.register(this);
        connMan = ((ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = connMan.getActiveNetworkInfo();
        if (nInfo == null)
            mHasConnection = false;
        else
            mHasConnection = nInfo.isConnected();
        dbHelper.checkUpgrade();
    }
    /**
     * Used by other classes to get the application's global context.
     * @return  the context of the application
     */
    public static Context getApp() {
        return app;
    }
    
    @Override
    public void onStacktrace(String appPackage, String packageVersion,
            String deviceModel, String androidVersion, String stacktrace) {
        ParseObject object = new ParseObject("Log");
        object.put("androidVersion", androidVersion);
        object.put("appPackage", appPackage);
        object.put("deviceModel", deviceModel);
        object.put("packageVersion", packageVersion);
        object.put("stacktrace", stacktrace);
        try {
            object.saveInBackground();
        } catch (ExceptionInInitializerError e) {};
    }
    
    public static void reportQuestion(String questionId, String question,
            String answer, String score) {
        if (questionId != null && question != null && answer != null &&
                score != null) {
            ParseObject object = new ParseObject("Report");
            object.put("questionId", questionId);
            object.put("question", question);
            object.put("answer", answer);
            object.put("score", score);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException arg0) {
                    mToast.setText("Report sent, thank you");
                    mToast.show();
                }
            });
        }
        else {
            mToast.setText("Report sent, thank you");
            mToast.show();
        }
    }
    
    public static void setConnection(boolean hasConnection) {
        mHasConnection = hasConnection;
    }
    
    public static boolean getConnection() {
        return mHasConnection;
    }
    
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib"))
                    deleteDir(new File(appDir, s));
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }
    
    public static boolean isActive() {
        return mIsActive;
    }
    
    public static void setActive() {
        mIsActive = true;
    }
    
    public static void setInactive() {
        mIsActive = false;
    }
    
    public static void setStringArrayPref(String key,
            ArrayList<String> answers) {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(app);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        JSONArray array = new JSONArray(answers);
        if (!answers.isEmpty())
            editor.putString(key, array.toString());
        else
            editor.putString(key, null);
        editor.commit();
    }

    public static ArrayList<String> getStringArrayPref(String key) {
        long perfTime = System.currentTimeMillis();
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(app);
        String json = sharedPrefs.getString(key, null);
        ArrayList<String> answers = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    answers.add(array.optString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return answers;
    }
    
}