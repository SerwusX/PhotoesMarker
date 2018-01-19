package aei.polsl.pl.photoesmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrzej on 2018-01-19.
 */

public class FilesLister {
    public static List<String> getListOfPathStringsOfFilesInDir(String path){
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<String> pathStrings = new ArrayList<>();
        for (File file : files){
            if (!file.isDirectory())
                pathStrings.add(file.getAbsolutePath());
        }
        return pathStrings;
    }
}
