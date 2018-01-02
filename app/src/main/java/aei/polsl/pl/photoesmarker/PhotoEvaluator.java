package aei.polsl.pl.photoesmarker;

import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by Andrzej on 2017-12-27.
 */

public class PhotoEvaluator {

    public static void ratePhoto(String imageFilePath, int rating, int quality){

        String ratingAndQualityStr = String.valueOf(rating) + "," + String.valueOf(quality);

        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            exif.setAttribute(ExifInterface.TAG_MAKE, ratingAndQualityStr);
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tego proszę nie wywoływać, odpowiednia metoda jest w klasie PhotoMarker!
    static int getPhotoRating(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String ratingAndQualityStr = exif.getAttribute(ExifInterface.TAG_MAKE);
            String[] parts = ratingAndQualityStr.split(",");
            return Integer.parseInt(parts[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } catch (NullPointerException e){
            return 0;
        } catch (NumberFormatException e){
            return 0;
        } catch (ArrayIndexOutOfBoundsException e){
            return 0;
        }
    }

    //Tego proszę nie wywoływać, odpowiednia metoda jest w klasie PhotoMarker!
    static int getPhotoQuality(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String ratingAndQualityStr = exif.getAttribute(ExifInterface.TAG_MAKE);
            String[] parts = ratingAndQualityStr.split(",");
            return Integer.parseInt(parts[1]);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } catch (NullPointerException e){
            return 0;
        } catch (NumberFormatException e){
            return 0;
        } catch (ArrayIndexOutOfBoundsException e){
            return 0;
        }
    }
}
