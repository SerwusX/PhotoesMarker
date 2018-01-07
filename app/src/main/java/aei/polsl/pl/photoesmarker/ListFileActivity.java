package aei.polsl.pl.photoesmarker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    private List values; //docelowa lista przekazywana do grida
    private List pictures; //lista z obrazkami
    private List directories; //lista z folderami
    GridAdapter gridAdapter;
    private SortingTag sortingTag;

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
        values = new ArrayList(); //docelowa lista przekazywana do grida
        pictures = new ArrayList(); //lista z obrazkami
        directories = new ArrayList(); //lista z folderami
        verifyStoragePermissions(this);
        sManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        // domyslna sciezka po uruchomieniu aplikacji
        path = PathAcquirer.getCurrentPathStr(ListFileActivity.this);
        if(path == null){
            path = "/sdcard";
        }
        if (getIntent().hasExtra("path")) { //pobieranie wartosci sciezki z poprzedniej aktywnosci
            path = getIntent().getStringExtra("path");
            PathAcquirer.updateCurrentPath(ListFileActivity.this, path);
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
        gridAdapter = new GridAdapter(this, values);
        gridview.setAdapter(gridAdapter);

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
                                           int position, long id) { //wyswietlanie Exifu
                File filename = (File) gridview.getItemAtPosition(position);
                String filenameString = filename.getName();
                if (path.endsWith(File.separator)) {
                    filenameString = path + filenameString;
                } else {
                    filenameString = path + File.separator + filenameString;
                }
                if (new File(filenameString).isDirectory()) { //przechodzenie do wybranego folderu
                    Toast.makeText(ListFileActivity.this, "Folder nie ma EXIFA!", Toast.LENGTH_LONG).show();
                } else { // wyswietlanie Exifu
                    ListFileActivity.exifDialog(ListFileActivity.this, filenameString);
                }
                return true;
            }
        });
    }

    private static List<String> exifGridViewBuilder(String filenameString) {
        List<String> tempList = new ArrayList<>();
        tempList.add("Nazwa");
        tempList.add(PhotoMarker.getNameStr(filenameString));
        tempList.add("Szerokość geograficzna");
        tempList.add(PhotoMarker.getLatitudeStr(filenameString));
        tempList.add("Długość geograficzna");
        tempList.add(PhotoMarker.getLongitudeStr(filenameString));
        tempList.add("Orientacja");
        tempList.add(PhotoMarker.getOrientationStr(filenameString));
        tempList.add("Data");
        tempList.add(PhotoMarker.getDateTimeStr(filenameString));
        tempList.add("Wysokość");
        tempList.add(PhotoMarker.getAltitudeStr(filenameString));
        tempList.add("Długość zdjęcia");
        tempList.add(PhotoMarker.getPhotoLengthStr(filenameString));
        tempList.add("Szerokość zdjęcia");
        tempList.add(PhotoMarker.getPhotoWidthStr(filenameString));
        tempList.add("Jakość");
        tempList.add(PhotoMarker.getPhotoQualityStr(filenameString));
        tempList.add("Ocena");
        tempList.add(PhotoMarker.getPhotoRatingStr(filenameString));
        tempList.add("Średnia oceny i jakości");
        tempList.add(PhotoMarker.getAverageOfRatingAndQualityStr(filenameString));
        tempList.add("Wartość żyroskopu z osi Z");
        tempList.add(PhotoMarker.getGyroZValueStr(filenameString));
        tempList.add("Wartość żyroskopu z osi Y");
        tempList.add(PhotoMarker.getGyroYValueStr(filenameString));
        tempList.add("Wartość żyroskopu z osi X");
        tempList.add(PhotoMarker.getGyroXValueStr(filenameString));

        return tempList;
    }
    public static void exifDialog(Context context, String filenameString){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exif_dialog);
        dialog.setTitle("EXIF");
        final GridView exifGridView = dialog.findViewById(R.id.exifGridView);
        List<String> listaStringow = exifGridViewBuilder(filenameString);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, listaStringow);
        exifGridView.setAdapter(adapter);
        final Button buttonExifOk = dialog.findViewById(R.id.buttonExifOk);

        buttonExifOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public void onClickDoGory(View v){ //przechodzenie do rodzica
        if(!path.equals("/")) {
            Intent intent = new Intent(ListFileActivity.this, ListFileActivity.class);
            intent.putExtra("path", dir.getParent());
            startActivity(intent);

        }
    }
    public void onClickSortowanie(View v){ //wyswietlanie sposobu sortowania
        final Dialog dialog = new Dialog(ListFileActivity.this);
        dialog.setContentView(R.layout.activity_sort);
        dialog.setTitle("Sortowanie");
        final RadioGroup radioSortowanie = dialog.findViewById(R.id.radioGroup);
        final Button buttonSortowanie = dialog.findViewById(R.id.sortowanieOk);
        buttonSortowanie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioSortowanie.check(R.id.radioName);
                int selectedId = radioSortowanie.getCheckedRadioButtonId();
                RadioButton wybranyButton = dialog.findViewById(selectedId);
                Toast.makeText(ListFileActivity.this,
                        wybranyButton.getContentDescription(), Toast.LENGTH_SHORT).show();
                try {
                    sortingTag = SortingTag.valueOf((String) wybranyButton.getContentDescription());
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                List stringPictures = convertListOfFilesToString(pictures);
                // sortowanie zdjec
                try {
                    stringPictures = PhotoesSorter.sortListOfPhotoes(stringPictures, sortingTag);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pictures = convertListOfStringsToFiles(stringPictures);
                values.clear();
                values.addAll(directories);
                values.addAll(pictures);

                gridAdapter.notifyDataSetChanged(); //refreshing grid view
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    private List<File> convertListOfStringsToFiles(List<String> stringPictures) {
        List<File> files = new ArrayList<>();
        for (String picture: stringPictures) {
           files.add(new File(picture));
        }
        return files;
    }

    private List<String> convertListOfFilesToString(List<File> pictures) {
        List<String> listOfStrings = new ArrayList<>();
        for (File pictureFile: pictures) {
            listOfStrings.add(pictureFile.getAbsolutePath());
        }
        return listOfStrings;
    }

    public void onClickWlaczenieAparatu(View v){ //wlaczenie aparatu
        CameraManager cameraManager = new CameraManager();
        cameraManager.takePhoto(this, this);
    }
}