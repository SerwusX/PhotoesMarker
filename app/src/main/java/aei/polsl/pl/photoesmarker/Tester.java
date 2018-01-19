package aei.polsl.pl.photoesmarker;

import android.util.Log;

import java.util.List;

/**
 * Created by Andrzej on 2018-01-19.
 */

public class Tester {
    static void logPathStrings(List<String> pathStrings){
        int i = 0;
        Log.d("######", "Lista plikow:");
        for (String pathString : pathStrings){
            Log.d("[" + String.valueOf(i) + "]", pathString);
            i++;
        }
        Log.d("######", "Koniec listy plikow.:");
    }
}
