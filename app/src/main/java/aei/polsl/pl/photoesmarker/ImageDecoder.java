package aei.polsl.pl.photoesmarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;

/**
 * Created by Andrzej on 2018-02-03.
 */

public class ImageDecoder implements Runnable {

    private List<String> listOfImages;
    private int position;
    private int parameter;
    private volatile Bitmap result;

    public ImageDecoder(List<String> list, int pos, int param){
        listOfImages = list;
        position = pos;
        parameter = param;
    }

    public Bitmap getResult(){
        return result;
    }

    @Override
    public void run() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(listOfImages.get(position), bmOptions);

        int photoW = bmOptions.outWidth;

        // Determine how much to scale down the image
        int scaleFactor = photoW/parameter;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        result = BitmapFactory.decodeFile(listOfImages.get(position), bmOptions);
    }
}
