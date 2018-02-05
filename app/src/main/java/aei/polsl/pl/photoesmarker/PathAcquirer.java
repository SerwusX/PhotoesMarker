package aei.polsl.pl.photoesmarker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PathAcquirer {

    //stała zawierająca ścieżkę do katalogu ze zdjęciami jako string
    private static final String defaultWorkingDirectory = Environment.DIRECTORY_DCIM;

    //stałe zawierające klucze do zapisu i odczytu danych z preferences
    private static final String sharedPreferncesMainKey = "saved_path";
    private static final String sharedPreferncesKey = "current_path";

    //zwraca obecną ścieżkę
    static String getCurrentPathStr(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        return sharedPref.getString(sharedPreferncesKey, defaultWorkingDirectory);
    }

    //aktualiazuje obecną ścieżkę
    static void updateCurrentPath(Context context, String newWorkingDirectoryStr){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sharedPreferncesKey, newWorkingDirectoryStr);
        editor.commit();
    }

    //resetuje obecną ścieżkę, przydatne w przypadku błędów
    private static void resetCurrentPath(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        File path = Environment.getExternalStoragePublicDirectory(defaultWorkingDirectory);
        editor.putString(sharedPreferncesKey, path.getAbsolutePath());
        editor.commit();
    }

    //nieużywana, do przyszłych zastosowań
    static List<String> getListOfEverythingFromDir(Context context){
        List<String> paths = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        String pathString = sharedPref.getString(sharedPreferncesKey, defaultWorkingDirectory);
        File directory = new File(pathString);
        File[] farray = directory.listFiles();
        for(File file : farray){
            paths.add(file.getAbsolutePath());
            Log.d("WSZYSTKIE PLIKI", file.getAbsolutePath());
        }
        return paths;
    }

    //nieużywana, do przyszłych zastosowań
    static List<String> getListOfDirsFromDir(Context context){
        List<String> onlyDirStringPaths = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        String pathString = sharedPref.getString(sharedPreferncesKey, defaultWorkingDirectory);
        File directory = new File(pathString);
        File[] farray = directory.listFiles();
        for (File file : farray){
            if (file.isDirectory()){
                onlyDirStringPaths.add(file.getAbsolutePath());
                Log.d("TYLKO KATALOGI", file.getAbsolutePath());
            }
        }

        Collections.sort(onlyDirStringPaths, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        return onlyDirStringPaths;
    }

    //zwraca listę z ścieżkami jpgów z obecnej ścieżki
    static List<String> getListOfJPGFilesFromDir(Context context){
        List<String> imagesPaths = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferncesMainKey, Context.MODE_PRIVATE);
        String pathString = sharedPref.getString(sharedPreferncesKey, defaultWorkingDirectory);
        File directory = new File(pathString);
        File[] arrayOfImagesPathStrings = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        });


        try {
            for (File file : arrayOfImagesPathStrings){
                imagesPaths.add(file.getAbsolutePath());
            }
        }catch (RuntimeException e){
            resetCurrentPath(context);
        }
        return imagesPaths;
    }

}
