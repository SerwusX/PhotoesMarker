//klasa potrzebna do przetwarzania miniaturek

package aei.polsl.pl.photoesmarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;


public class ImageDecoder implements Runnable {

    //lista ze ścieżkami do zdjęć
    private List<String> listOfImages;

    //zmienna z pozycją zdjęcia w liście
    private int position;

    //parametr ten pokazuje jak przeskalować zdjęcia żeby zmieściło się w gridView
    private int parameter;

    //
    private volatile Bitmap result;

    //konstruktor
    ImageDecoder(List<String> list, int pos, int param){
        listOfImages = list;
        position = pos;
        parameter = param;
    }

    //zwraca przetworzoną miniaturę zdjęcia
    Bitmap getResult(){
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
