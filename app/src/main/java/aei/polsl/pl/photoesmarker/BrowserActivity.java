package aei.polsl.pl.photoesmarker;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class BrowserActivity extends AppCompatActivity {

    SortingTag sortingTag = SortingTag.AVERAGE_RATING_AND_QUALITY;
    boolean reverseSorting = false;
    int checkedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkedItem = 0;
        setContentView(R.layout.activity_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                takePhoto();
            }
        });

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter imageAdapter = new ImageAdapter(this);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
//                Toast.makeText(BrowserActivity.this, "" + position,
//                        Toast.LENGTH_SHORT).show();
                Toast.makeText(BrowserActivity.this, listOfImages.get(position),
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), PhotoViewerActivity.class);

                i.putExtra(getString(R.string.image_path_string_to_show_in_viewer_activity),listOfImages.get(position));
                startActivity(i);
            }
        });

        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                createAlertDialogWithDetails(position);
                return true;
            }
        });

        sManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
    }

//    @Override
//    public void onStart(){
//        super.onStart();
//        updateGridView();
//    }

    private List<String> listOfImages;

    void updateGridView(){

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter imageAdapter = new ImageAdapter(this);
        listOfImages = PathAcquirer.getListOfJPGFilesFromDir(this);

        //Tu bedzie sortowanie
        try {
            listOfImages = PhotoesSorter.sortListOfPhotoes(listOfImages, sortingTag, reverseSorting);
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageAdapter.updateImageList(listOfImages);
        imageAdapter.getCount();
        imageAdapter.calculateMiniatures();
        //imageAdapter.notifyDataSetChanged();
        gridview.setAdapter(imageAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_browser, menu);
        return true;
    }

    //final Activity activityForButton = this;

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.action_choose_directory:
                Intent fileExploreIntent = new Intent(
                        FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                        null,
                        this,
                        FileBrowserActivity.class
                );
                //  fileExploreIntent.putExtra(
                //  	ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter,
                //      "/sdcard"
                //  );//Here you can add optional start directory parameter, and file browser will start from that directory.
                startActivityForResult(
                        fileExploreIntent,
                        REQUEST_CODE_PICK_DIR
                );
                //updateGridView();
                return true;
            case R.id.action_sorting_options:
                createAlertDialogWithRadioButtonGroup();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void takePhoto()
    {
        CameraManager cameraManager = new CameraManager();
        cameraManager.takePhoto(this, this);
        cameraManager.moveFileToWorkingLocationAndMarkIfExists(this, azimuth, pitch, roll);
    }

    //Poniżej elementy potrzebne do wyboru katalogu
    private final int REQUEST_CODE_PICK_DIR = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_CODE_PICK_DIR) {
            if(resultCode == this.RESULT_OK) {
                String newDir = data.getStringExtra(
                        FileBrowserActivity.returnDirectoryParameter);
                Toast.makeText(
                        this,
                        "Obecny katalog:"+newDir,
                        Toast.LENGTH_LONG
                ).show();
                PathAcquirer.updateCurrentPath(this, newDir);
                updateGridView();
            }
            else {//if(resultCode == this.RESULT_OK) {
                Toast.makeText(
                        this,
                        "Nie wybrano nowego katalogu, obecny katalog: " + PathAcquirer.getCurrentPathStr(this),
                        Toast.LENGTH_LONG)
                        .show();
            }//END } else {//if(resultCode == this.RESULT_OK) {
        }//if (requestCode == REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Okienko ze szczegółami
    AlertDialog alertDialogDetails;

    CharSequence[] detailsTitles = {
            "Nazwa",
            "Orientacja",
            "Data",
            "Szerokość zdjęcia",
            "Wysokość zdjęcia",
            "Szerokość geograficzna",
            "Długość geograficzna",
            "Ocena",
            "Jakość",
            "Średnia oceny i jakości",
            "Żyroskop X",
            "Żyroskop Y",
            "Żyroskop Z"
    };

    private void createAlertDialogWithDetails(int positionOfImageInGrid){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setAdapter(new DetailsWindowAdapter(positionOfImageInGrid), null);

        builder.setTitle("Szczegóły zdjęcia");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogDetails = builder.create();
        alertDialogDetails.show();
    }

    class DetailsWindowAdapter extends BaseAdapter
    {

        DetailsWindowAdapter(int pos){
            positionOfImageInGrid = pos;
        }

        private int positionOfImageInGrid;

        @Override
        public int getCount()
        {
            return detailsTitles.length;
        }

        @Override
        public Object getItem(int position)
        {
            return detailsTitles[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.details_window, null);
            }

            String imageFilePathString = listOfImages.get(positionOfImageInGrid);

            CharSequence[] detailsValues = {
                    PhotoMarker.getNameStr(imageFilePathString),
                    PhotoMarker.getOrientationStr(imageFilePathString),
                    PhotoMarker.getDateTimeStr(imageFilePathString),
                    PhotoMarker.getPhotoWidthStr(imageFilePathString),
                    PhotoMarker.getPhotoLengthStr(imageFilePathString),
                    PhotoMarker.getLatitudeStr(imageFilePathString),
                    PhotoMarker.getLongitudeStr(imageFilePathString),
                    PhotoMarker.getPhotoRatingStr(imageFilePathString),
                    PhotoMarker.getPhotoQualityStr(imageFilePathString),
                    PhotoMarker.getAverageOfRatingAndQualityStr(imageFilePathString),
                    PhotoMarker.getGyroXValueStr(imageFilePathString),
                    PhotoMarker.getGyroYValueStr(imageFilePathString),
                    PhotoMarker.getGyroZValueStr(imageFilePathString)
            };

            ((TextView) convertView.findViewById(R.id.details_windows_titles)).setText(detailsTitles[position]);
            ((TextView) convertView.findViewById(R.id.details_windows_values)).setText(detailsValues[position]);

            return convertView;
        }
    }

    //Okienko z wyborem sortowania
    AlertDialog alertDialogRadioButtons;

    CharSequence[] radioTitles = {
            " Nazwa rosnąco ",
            " Nazwa malejąco ",
            " Orientacja rosnąco ",
            " Orientacja malejąco ",
            " Data rosnąco ",
            " Data malejąco ",
            " Długość rosnąco ",
            " Długość malejąco ",
            " Szerokość rosnąco ",
            " Szerokość malejąco ",
            " Powierzchnia rosnąco ",
            " Powierzchnia malejąco ",
            " GPS rosnąco ",
            " GPS malejąco ",
            " Żyroskop rosnąco ",
            " Żyroskop malejąco ",
            " Ocena rosnąco ",
            " Ocena malejąco ",
            " Jakość rosnąco ",
            " Jakość malejąco ",
            " Średnia rosnąco ",
            " Średnia malejąco ",
    };

    private void createAlertDialogWithRadioButtonGroup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Metoda sortowania");

        builder.setSingleChoiceItems(radioTitles, checkedItem, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    //default:
                    case 0:
                        //Toast.makeText(BrowserActivity.this, "First Item Clicked", Toast.LENGTH_LONG).show();
                        sortingTag = SortingTag.NAME;
                        reverseSorting = false;
                        checkedItem = 0;
                        updateGridView();
                        break;
                    case 1:
                        sortingTag = SortingTag.NAME;
                        reverseSorting = true;
                        checkedItem = 1;
                        updateGridView();
                        break;
                    case 2:
                        sortingTag = SortingTag.ORIENTATION;
                        reverseSorting = false;
                        checkedItem = 2;
                        updateGridView();
                        break;
                    case 3:
                        sortingTag = SortingTag.ORIENTATION;
                        reverseSorting = true;
                        checkedItem = 3;
                        updateGridView();
                        break;
                    case 4:
                        sortingTag = SortingTag.DATE;
                        reverseSorting = false;
                        checkedItem = 4;
                        updateGridView();
                        break;
                    case 5:
                        sortingTag = SortingTag.DATE;
                        reverseSorting = true;
                        checkedItem = 5;
                        updateGridView();
                        break;
                    case 6:
                        sortingTag = SortingTag.PHOTO_LENGTH;
                        reverseSorting = false;
                        checkedItem = 6;
                        updateGridView();
                        break;
                    case 7:
                        sortingTag = SortingTag.PHOTO_LENGTH;
                        reverseSorting = true;
                        checkedItem = 7;
                        updateGridView();
                        break;
                    case 8:
                        sortingTag = SortingTag.PHOTO_WIDTH;
                        reverseSorting = false;
                        checkedItem = 8;
                        updateGridView();
                        break;
                    case 9:
                        sortingTag = SortingTag.PHOTO_WIDTH;
                        reverseSorting = true;
                        checkedItem = 9;
                        updateGridView();
                        break;
                    case 10:
                        sortingTag = SortingTag.PHOTO_AREA;
                        reverseSorting = false;
                        checkedItem = 10;
                        updateGridView();
                        break;
                    case 11:
                        sortingTag = SortingTag.PHOTO_AREA;
                        reverseSorting = true;
                        checkedItem = 11;
                        updateGridView();
                        break;
                    case 12:
                        sortingTag = SortingTag.GPS_POSITION;
                        reverseSorting = false;
                        checkedItem = 12;
                        updateGridView();
                        break;
                    case 13:
                        sortingTag = SortingTag.GPS_POSITION;
                        reverseSorting = true;
                        checkedItem = 13;
                        updateGridView();
                        break;
                    case 14:
                        sortingTag = SortingTag.GYROSCOPE;
                        reverseSorting = false;
                        checkedItem = 16;
                        updateGridView();
                        break;
                    case 15:
                        sortingTag = SortingTag.GYROSCOPE;
                        reverseSorting = true;
                        checkedItem = 17;
                        updateGridView();
                        break;
                    case 16:
                        sortingTag = SortingTag.RATING;
                        reverseSorting = false;
                        checkedItem = 18;
                        updateGridView();
                        break;
                    case 17:
                        sortingTag = SortingTag.RATING;
                        reverseSorting = true;
                        checkedItem = 19;
                        updateGridView();
                        break;
                    case 18:
                        sortingTag = SortingTag.QUALITY;
                        reverseSorting = false;
                        checkedItem = 20;
                        updateGridView();
                        break;
                    case 19:
                        sortingTag = SortingTag.QUALITY;
                        reverseSorting = true;
                        checkedItem = 21;
                        updateGridView();
                        break;
                    case 20:
                        sortingTag = SortingTag.AVERAGE_RATING_AND_QUALITY;
                        reverseSorting = false;
                        checkedItem = 22;
                        updateGridView();
                        break;
                    case 21:
                        sortingTag = SortingTag.AVERAGE_RATING_AND_QUALITY;
                        reverseSorting = true;
                        checkedItem = 23;
                        updateGridView();
                        break;
                }
                alertDialogRadioButtons.dismiss();
            }
        });
        alertDialogRadioButtons = builder.create();
        alertDialogRadioButtons.show();

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

    @Override
    protected void onPause() {
        super.onPause();
        sManager.unregisterListener(mySensorEventListener);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(mySensorEventListener, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        CameraManager cameraManager = new CameraManager();
        cameraManager.moveFileToWorkingLocationAndMarkIfExists(this, azimuth, pitch, roll);

        updateGridView();
    }
    ////////////////////////////////////////////////////////////////////////////////////

}
