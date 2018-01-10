package aei.polsl.pl.photoesmarker;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

/**
 * @author Mateusz T
 * Aktywnosc wyswietlajaca klikniete zdjecie.
 * W niej istnieje też możliwość oceniania zdjęcia.
 */
public class ImageViewActivity extends AppCompatActivity {
    String pathToJpg;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.engineer_equip4);
        this.mediaPlayer = mediaPlayer;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
        Intent intent = getIntent();

        pathToJpg = intent.getStringExtra("pathToJpg");
        ImageView imageView =  findViewById(R.id.imageView);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathToJpg, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

// Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/500, photoH/500);

// Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(pathToJpg, bmOptions);
        imageView.setImageBitmap(bitmap);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ListFileActivity.generujDzwiek(mediaPlayer);
                ListFileActivity.exifDialog(ImageViewActivity.this, pathToJpg);
                return true;
            }
        });

    }
    public void onClickPowrot(View v) {
        ListFileActivity.generujDzwiek(mediaPlayer);
        Intent intent = new Intent(ImageViewActivity.this, ListFileActivity.class);
        intent.putExtra("path", new File(pathToJpg).getParent());
        startActivity(intent);
    }
    public void onClickOcenianie(View v){ //ocenianie zdjecia
        ListFileActivity.generujDzwiek(mediaPlayer);
        final Dialog dialog = new Dialog(ImageViewActivity.this);
        dialog.setContentView(R.layout.activity_assessment);
        dialog.setTitle("Oceń zdjęcia");
        final SeekBar rating = dialog.findViewById(R.id.rating);
        final SeekBar quality = dialog.findViewById(R.id.quality);
        final TextView qualityValue =  dialog.findViewById(R.id.qualityValue);
        final TextView ratingValue =  dialog.findViewById(R.id.ratingValue);
        final Button buttonOk =  dialog.findViewById(R.id.buttonOk);
        rating.setProgress(50);
        quality.setProgress(50);

        qualityValue.setText(String.valueOf(rating.getProgress()));
        ratingValue.setText(String.valueOf(rating.getProgress()));

        rating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                qualityValue.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        quality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                ratingValue.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buttonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ListFileActivity.generujDzwiek(mediaPlayer);

                PhotoEvaluator.ratePhoto(pathToJpg, rating.getProgress(), quality.getProgress());

                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

