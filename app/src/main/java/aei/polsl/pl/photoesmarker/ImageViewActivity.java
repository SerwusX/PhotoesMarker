package aei.polsl.pl.photoesmarker;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {
    String pathToJpg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    }
    public void onClickPowrot(View v) {
        Intent intent = new Intent(ImageViewActivity.this, ListFileActivity.class);
        intent.putExtra("path", new File(pathToJpg).getParent());
        startActivity(intent);
    }
    public void onClickOcenianie(View v){ //ocenianie zdjecia
        final Dialog dialog = new Dialog(ImageViewActivity.this);
        dialog.setContentView(R.layout.activity_assessment);
        dialog.setTitle("Oceń zdjęcia");
        final SeekBar seekBar1 = dialog.findViewById(R.id.seekBar1);
        final SeekBar seekBar2 = dialog.findViewById(R.id.seekBar2);
        final TextView ocena1 =  dialog.findViewById(R.id.ocena2);
        final TextView ocena2 =  dialog.findViewById(R.id.ocena1);
        final Button buttonOk =  dialog.findViewById(R.id.buttonOk);
        seekBar1.setProgress(50);
        seekBar2.setProgress(50);

        ocena1.setText(String.valueOf(seekBar1.getProgress()));
        ocena2.setText(String.valueOf(seekBar1.getProgress()));

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                ocena1.setText(String.valueOf(progress));

            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                ocena2.setText(String.valueOf(progress));

            }
        });
        buttonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Wartosc pierwszej oceny", String.valueOf(seekBar1.getProgress()));
                Log.d("Wartosc drugiej oceny", String.valueOf(seekBar2.getProgress()));
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

