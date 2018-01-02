package aei.polsl.pl.photoesmarker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFileActivity extends Activity {

    private String path;
    private File dir;
    SensorManager sManager;
    // azimuth, pitch and roll
    private volatile float azimuth;
    private volatile float pitch;
    private volatile float roll;

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
    @Override
    protected void onResume() {
        super.onResume();
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        CameraManager cameraManager = new CameraManager();
        cameraManager.moveFileToWorkingLocationAndMarkIfExists(this, azimuth, pitch, roll);
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
        setContentView(R.layout.grid_activity);
        List values = new ArrayList(); //docelowa lista przekazywana do grida
        List pictures = new ArrayList(); //lista z obrazkami
        List directories = new ArrayList(); //lista z folderami
        verifyStoragePermissions(this);
        sManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        // domyslna sciezka po uruchomieniu aplikacji
        path = "/sdcard";
        if (getIntent().hasExtra("path")) { //pobieranie wartosci sciezki z poprzedniej aktywnosci
            path = getIntent().getStringExtra("path");
        }
        setTitle(path);

        // Read all files sorted into the values-array

        dir = new File(path);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list(); // lista wszystkich plikow i folderow z biezacej sciezki
        if (list != null) {
            // selekcja plikow i folderow z biezacej sciezki
            for (File file : dir.listFiles()) {
                if(!file.getName().startsWith(".")) {
                    if(file.isDirectory()){
                       directories.add(file);
                    }
                    if(file.getAbsolutePath().contains(".jpg")){
                        pictures.add(file);
                    }
                }
            }
        } else { //jak nie ma sdcard to lecimi do roota
            Intent intent = new Intent(ListFileActivity.this, ListFileActivity.class);
            intent.putExtra("path", "/");
            startActivity(intent);
        }
        Collections.sort(values); // sortowanie listy
        values.addAll(directories);
        values.addAll(pictures);
        // Put the data into the list
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new GridAdapter(this, values));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                File filename = (File) gridview.getItemAtPosition(position);
                String filenameString = filename.getName();
                if (path.endsWith(File.separator)) {
                    filenameString = path + filenameString;
                } else {
                    filenameString = path + File.separator + filenameString;
                }
                if (new File(filenameString).isDirectory()) { //przechodzenie do wybranego folderu
                    Intent intent = new Intent(ListFileActivity.this, ListFileActivity.class);
                    intent.putExtra("path", filenameString);
                    startActivity(intent);
                } else { // wyswietlanie zdjecia
                    Intent intent = new Intent(ListFileActivity.this, ImageViewActivity.class);
                    intent.putExtra("pathToJpg", filenameString);
                    startActivity(intent);
                }
            }
        });
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //pokazywanie Exifu

            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long id) {
                Log.d("LongClick","in onLongClick");
                String str = gridview.getItemAtPosition(position).toString();

                Log.d("LongClick","long click : " +str);
                return true;
            }
        });
    }
    public void onClickDoGory(View v){ //przechodzenie do rodzica
        if(!path.equals("/")) {
            Intent intent = new Intent(ListFileActivity.this, ListFileActivity.class);
            intent.putExtra("path", dir.getParent());
            startActivity(intent);

        }
    }
    public void onClickSortowanie(View v){ //wyswietlanie sposobu sortowania

    }
    public void onClickWlaczenieAparatu(View v){ //wlaczenie aparatu
        CameraManager cameraManager = new CameraManager();
        cameraManager.takePhoto(this, this);
    }
}