package aei.polsl.pl.photoesmarker;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onResume() {
        super.onResume();
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        CameraManager cameraManager = new CameraManager();
        cameraManager.moveFileToWorkingLocationAndMarkIfExists(this, azimuth, pitch, roll);

        SoundPlayer.setContextAndSound(this, R.raw.fake_tutorial);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sManager.unregisterListener(mySensorEventListener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        sManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

    }

    ///////////////////////////////////// ŻYROSKOP /////////////////////////////////////
    SensorManager sManager;

    // azimuth, pitch and roll
    private volatile float azimuth;
    private volatile float pitch;
    private volatile float roll;

    private SensorEventListener mySensorEventListener = new SensorEventListener() {
        // Gravity rotational data
        float gravity[];
        // Magnetic rotational data
        float magnetic[]; //for magnetic rotational data
        float accels[] = new float[3];
        float mags[] = new float[3];
        float[] values = new float[3];

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mags = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    accels = event.values.clone();
                    break;
            }

            if (mags != null && accels != null) {
                gravity = new float[9];
                magnetic = new float[9];
                SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
                float[] outGravity = new float[9];
                SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
                SensorManager.getOrientation(outGravity, values);

                //Zamiana z radianów na stopnie
                azimuth = values[0] * 57.2957795f;
                pitch =values[1] * 57.2957795f;
                roll = values[2] * 57.2957795f;

                mags = null;
                accels = null;
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////



    public void onClick(View v) {
        //SoundPlayer.playSoundOrStopPlayingIfAlreadyPlaying();
        //CameraManager cameraManager = new CameraManager();
        //cameraManager.takePhoto(this, this);
        test();
    }

    public void test(){
        String currentPath = PathAcquirer.getCurrentPathStr(this);
        List<String> pathStrings;
        pathStrings = FilesLister.getListOfPathStringsOfFilesInDir(currentPath);
        Tester.logPathStrings(pathStrings);
        try{
            Log.d("Sortowanie", "Ocena i jakosc");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.AVERAGE_RATING_AND_QUALITY);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Jakosc");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.QUALITY);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Ocena");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.RATING);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Zyroskop");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.GYROSCOPE);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Poziom morza");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.GPS_SEA_LEVEL);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "GPS");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.GPS_POSITION);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Powierzchnia zdjecia");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.PHOTO_AREA);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Szerokosc zdjecia");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.PHOTO_WIDTH);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Dlugosc zdjecia");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.PHOTO_LENGTH);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Data");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.DATE);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Nazwa");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.NAME);
            Tester.logPathStrings(pathStrings);
            Log.d("Sortowanie", "Orientacja");
            pathStrings = PhotoesSorter.sortListOfPhotoes(pathStrings, SortingTag.ORIENTATION);
            Tester.logPathStrings(pathStrings);
        }catch(IOException e){
            Log.d("Wyjatek w sortowaniu", e.getMessage());
        }
    }

}

