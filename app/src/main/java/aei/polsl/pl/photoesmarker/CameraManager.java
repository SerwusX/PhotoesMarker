package aei.polsl.pl.photoesmarker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import aei.polsl.pl.photoesmarker.PathAcquirer;
import aei.polsl.pl.photoesmarker.PhotoMarker;

/**
 * Created by Andrzej on 2017-12-28.
 */

public class CameraManager {
    static final int REQUEST_TAKE_PHOTO = 3;

    static String mCurrentPhotoPath;

    private File createImageFile(Context context) throws IOException {
        Date currentTime = Calendar.getInstance().getTime();
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(currentTime);
        String imageFileName = "PHOTO_" + timeStamp;
        //PathAcquirer.resetCurrentPath(context);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(Context context, Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(context);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "aei.polsl.pl.photoesmarker.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void takePhoto(Context context, Activity activity){
        dispatchTakePictureIntent(context, activity);
    }

    public void moveFileToWorkingLocationAndMarkIfExists(Context context, double azimuth, double pitch, double roll){
        if(!TextUtils.isEmpty(mCurrentPhotoPath)){
            File from = new File(mCurrentPhotoPath);

            if(from.length() > 0){

                PhotoMarker photoMarker = new PhotoMarker();
                photoMarker.addMarkersToPhoto(context, from.getAbsolutePath(), azimuth, pitch, roll);

                File to = new File(PathAcquirer.getCurrentPathStr(context) + "/" + from.getName());

                from.renameTo(to);

                Log.d("Sciezka zdjecia: ", from.getAbsolutePath());

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(from);
                mediaScanIntent.setData(contentUri);
                context.sendBroadcast(mediaScanIntent);
                mCurrentPhotoPath = null;
            }
            else{
                from.delete();
            }
        }
    }
}
