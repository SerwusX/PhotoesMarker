package aei.polsl.pl.photoesmarker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by Andrzej on 2017-12-28.
 */

public class PathAcquirer {

    private static final String defaultWorkingDirectory = Environment.DIRECTORY_DCIM;
    private static final String sharedPreferncesMainKey = "saved_path";
    private static final String sharedPreferncesKey = "current_path";

    public static String getCurrentPathStr(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        return sharedPref.getString(sharedPreferncesKey, defaultWorkingDirectory);
    }

    public static void updateCurrentPath(Context context, String newWorkingDirectoryStr){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sharedPreferncesKey, newWorkingDirectoryStr);
        editor.commit();
    }

    public static void resetCurrentPath(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        File path = Environment.getExternalStoragePublicDirectory(defaultWorkingDirectory);
        editor.putString(sharedPreferncesKey, path.getAbsolutePath());
        editor.commit();
    }

}
