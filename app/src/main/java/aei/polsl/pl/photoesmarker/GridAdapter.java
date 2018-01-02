package aei.polsl.pl.photoesmarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Mateusz on 2017-12-25.
 */

class GridAdapter extends BaseAdapter {
    private Context context;
    private List listOfItems;

    public GridAdapter(Context context, List listOfItems) {
        this.context = context;
        this.listOfItems = listOfItems;
    }

    @Override
    public int getCount() {
        return listOfItems.size();
    }

    @Override
    public Object getItem(int i) {
        return listOfItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        File currentFile = (File) listOfItems.get(position);
        ImageView imageThumbnail;
        TextView filename;
        View gridView;
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            gridView = new View(context);
            gridView = layoutInflater.inflate(R.layout.grid_cell, null);
            // if it's not recycled, initialize some attributes
            imageThumbnail = gridView.findViewById(R.id.imageThumbnail);
            filename = gridView.findViewById(R.id.filepath);

        if (currentFile.isDirectory()) { // wyswietlanie ikony folderu
                imageThumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.foldericon));
                ViewGroup.LayoutParams params = imageThumbnail.getLayoutParams();
                params.width = 50;
                params.height = 50;
                imageThumbnail.requestLayout();
            } else if (currentFile.isFile()) { // wyswietlanie miniatury obrazu

                ViewGroup.LayoutParams params = imageThumbnail.getLayoutParams();
                params.width = 300;
                params.height = 300;
                imageThumbnail.requestLayout();

                if (imageThumbnail.getTag() != null) {
                    ((ImageGetter) imageThumbnail.getTag()).cancel(true);
                }
                ImageGetter task = new ImageGetter(imageThumbnail);
                task.execute(currentFile.getAbsolutePath());
                imageThumbnail.setTag(task);

            }
            filename.setText(((File) listOfItems.get(position)).getName());
        } else{
            gridView = convertView;
        }
        return gridView;

    }

    private class ImageGetter extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public ImageGetter(ImageView v) {
            imageView = v;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(params[0], bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/100, photoH/100);

        // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(params[0], bmOptions);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setImageBitmap(result);
        }
    }
}



