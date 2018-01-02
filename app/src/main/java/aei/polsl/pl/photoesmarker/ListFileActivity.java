package aei.polsl.pl.photoesmarker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_activity);
        List values = new ArrayList(); //docelowa lista przekazywana do grida
        List pictures = new ArrayList(); //lista z obrazkami
        List directories = new ArrayList(); //lista z folderami
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

    }
}