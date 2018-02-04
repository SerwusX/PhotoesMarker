package aei.polsl.pl.photoesmarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import aei.polsl.pl.photoesmarker.ImageDecoder;
import aei.polsl.pl.photoesmarker.R;

/**
 * Created by Andrzej on 2018-02-01.
 */

public class ImageAdapter extends BaseAdapter  {
    private Context mContext;
    private int layoutParam;

    public ImageAdapter(Context c) {
        mContext = c;
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        layoutParam = screenWidth/mContext.getResources().getInteger(R.integer.number_of_columns_gridview);
    }

    public int getCount() {
        return imageList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    private List<Bitmap> miniatures;

    public void calculateMiniatures(){
        miniatures = new ArrayList<>();
        Thread[] threads = new Thread[getCount()];
        ImageDecoder[] imageDecoders = new ImageDecoder[getCount()];

        for (int i = 0; i < getCount(); i++){
            imageDecoders[i] = new ImageDecoder(imageList, i, layoutParam);
            threads[i] = new Thread(imageDecoders[i]);
            threads[i].start();
        }

        for (int i = 0; i < getCount(); i++){
            Log.d("LICZĘ: ", Integer.toString(i));
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            miniatures.add(imageDecoders[i].getResult());
        }
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);


            imageView.setLayoutParams(new GridView.LayoutParams(layoutParam, layoutParam));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(miniatures.get(position));

        return imageView;
    }

    public void updateImageList(List<String> newImageList){
        imageList = newImageList;
    }

    //Wersja dla obrazów z resources
    //Parametrem jest lista ID obrazów z resources
    private Bitmap decodeImageFromResources(Context context, List<Integer> list, int position){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), list.get(position), bmOptions);
        int photoW = bmOptions.outWidth;

        // Determine how much to scale down the image
        int scaleFactor = photoW/layoutParam;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeResource(context.getResources(), list.get(position), bmOptions);
    }

    //Wersja dla obrazów z pamięci
    //Parametrem jest lista bezwzględnych ścieżek jako stringi
    private Bitmap decodeImageFromDeviceMemory(List<String> list, int position){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(list.get(position), bmOptions);

        int photoW = bmOptions.outWidth;

        // Determine how much to scale down the image
        int scaleFactor = photoW/layoutParam;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(list.get(position), bmOptions);

    }

    private List<String> imageList = new ArrayList<String>();

    private ArrayList<Integer> testList = new ArrayList<Integer>() {{
        add(R.drawable.test_image); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
        add(R.drawable.triss); add(R.drawable.triss);
    }};

}