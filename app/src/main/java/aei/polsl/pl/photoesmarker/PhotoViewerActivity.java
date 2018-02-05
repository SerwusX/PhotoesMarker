//Klasa aktywności do wyświetlania konkretnego zdjęcia

package aei.polsl.pl.photoesmarker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

public class PhotoViewerActivity extends AppCompatActivity {

    //zmienna przechowująca ścieżkę aktualnie wybranego pliku
    private String imagePathString;

    //zmienna przechowująca ocenę zdjęcia, wykorzystywana w seekbarze
    private String rateOfPhotoStr = "";

    //zmienna przechowująca jakość zdjęcia, wykorzystywana w seekbarze
    private String qualityOfPhotoStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Wyłączenie paska statusu
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo_viewer);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //Pobranie z poprzedniej aktywności ścieżki wybranego zdjęcia
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imagePathString = extras.getString(getString(R.string.image_path_string_to_show_in_viewer_activity));
            Log.d("Co dostałem", imagePathString);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_viewer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogWithSeekbars();
            }
        });

        File imgFile = new  File(imagePathString);

        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView myImage = (ImageView) findViewById(R.id.imageView);

            myImage.setImageBitmap(myBitmap);
        }

        rateOfPhotoStr = PhotoMarker.getPhotoRatingStr(imagePathString);
        qualityOfPhotoStr = PhotoMarker.getPhotoQualityStr(imagePathString);

    }

    //Funkcja tworząca okno dialogowe z ocenianiem
    private void createAlertDialogWithSeekbars(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View Viewlayout = inflater.inflate(R.layout.seek_bars_window,
                (ViewGroup) findViewById(R.id.layout_dialog));

        final TextView item1 = (TextView)Viewlayout.findViewById(R.id.txtItem1); // txtItem1
        final TextView item2 = (TextView)Viewlayout.findViewById(R.id.txtItem2); // txtItem2

        if(!rateOfPhotoStr.isEmpty()){
            item1.setText(getString(R.string.rate_colon) + rateOfPhotoStr);
        }
        else {
            item1.setText(R.string.rate_colon_empty);
        }

        if(!rateOfPhotoStr.isEmpty()){
            item2.setText(getString(R.string.quality_colon) + qualityOfPhotoStr);
        }
        else {
            item2.setText(R.string.quality_colon_empty);
        }

        popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle(R.string.photo_evaluation);
        popDialog.setView(Viewlayout);

        //seekbar z oceną
        final SeekBar seek1 = (SeekBar) Viewlayout.findViewById(R.id.rate_seekbar);
        try {
            seek1.setProgress(Integer.valueOf(rateOfPhotoStr));
        } catch (NumberFormatException e){
            seek1.setProgress(0);
        }
        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                item1.setText(getString(R.string.rate_colon_space) + progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //seekbar z jakością
        final SeekBar seek2 = (SeekBar) Viewlayout.findViewById(R.id.quality_seekbar);
        try {
            seek2.setProgress(Integer.valueOf(qualityOfPhotoStr));
        } catch (NumberFormatException e){
            seek2.setProgress(0);
        }
        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Do something here with new value
                item2.setText(getString(R.string.quality_colon_space) + progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        //przycisk OK
        popDialog.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        PhotoEvaluator.ratePhoto(imagePathString, seek1.getProgress(), seek2.getProgress());

                        rateOfPhotoStr = String.valueOf(seek1.getProgress());
                        qualityOfPhotoStr = String.valueOf(seek2.getProgress());

                        dialog.dismiss();
                    }

                });


        popDialog.create();
        popDialog.show();

    }

}
