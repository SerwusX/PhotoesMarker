package aei.polsl.pl.photoesmarker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

public class PhotoViewerActivity extends AppCompatActivity {

    private String imagePathString;
    private String rateOfPhotoStr = "";
    private String qualityOfPhotoStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo_viewer);


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imagePathString = extras.getString(getString(R.string.image_path_string_to_show_in_viewer_activity));
            //The key argument here must match that used in the other activity
            Log.d("Co dostałem", imagePathString);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_viewer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
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

    private void createAlertDialogWithSeekbars(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View Viewlayout = inflater.inflate(R.layout.seek_bars_window,
                (ViewGroup) findViewById(R.id.layout_dialog));

        final TextView item1 = (TextView)Viewlayout.findViewById(R.id.txtItem1); // txtItem1
        final TextView item2 = (TextView)Viewlayout.findViewById(R.id.txtItem2); // txtItem2

        if(!rateOfPhotoStr.isEmpty()){
            item1.setText("Ocena: " + rateOfPhotoStr);
        }
        else {
            item1.setText("Ocena: brak");
        }

        if(!rateOfPhotoStr.isEmpty()){
            item2.setText("Jakość: " + qualityOfPhotoStr);
        }
        else {
            item2.setText("Jakość: brak");
        }

        popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Ewaluacja zdjęcia");
        popDialog.setView(Viewlayout);

        //  seekBar1
        final SeekBar seek1 = (SeekBar) Viewlayout.findViewById(R.id.rate_seekbar);
        try {
            seek1.setProgress(Integer.valueOf(rateOfPhotoStr));
        } catch (NumberFormatException e){
            seek1.setProgress(0);
        }
        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Do something here with new value
                item1.setText("Ocena: " + progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        //  seekBar2
        final SeekBar seek2 = (SeekBar) Viewlayout.findViewById(R.id.quality_seekbar);
        try {
            seek2.setProgress(Integer.valueOf(qualityOfPhotoStr));
        } catch (NumberFormatException e){
            seek2.setProgress(0);
        }
        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Do something here with new value
                item2.setText("Jakość: " + progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });


        // Button OK
        popDialog.setPositiveButton("OK",
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
