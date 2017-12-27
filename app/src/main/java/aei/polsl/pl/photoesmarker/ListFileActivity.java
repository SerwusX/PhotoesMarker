package aei.polsl.pl.photoesmarker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
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

        // domyslna sciezka po uruchomieniu aplikacji
        path = "/sdcard";
        if (getIntent().hasExtra("path")) { //pobieranie wartosci sciezki z poprzedniej aktywnosci
            path = getIntent().getStringExtra("path");
        }
        setTitle(path);

        // Read all files sorted into the values-array
        List values = new ArrayList(); //docelowa lista przekazywana do grida
        dir = new File(path);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list(); // lista wszystkich plikow i folderow z biezacej sciezki
        if (list != null) {
            for (File file : dir.listFiles()) { // selekcja plikow i folderow z biezacej sciezki
                if ((!file.getName().startsWith(".")) && (isPictureOrDirectory(file))) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values); // sortowanie listy

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
                if (new File(filenameString).isDirectory()) {
                    Intent intent = new Intent(ListFileActivity.this, ListFileActivity.class);
                    intent.putExtra("path", filenameString);
                    startActivity(intent);
                } else {
                    Toast.makeText(ListFileActivity.this, filenameString + " is not a directory", Toast.LENGTH_LONG).show();
                }
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
    private boolean isPictureOrDirectory(File file) {
        String[] allowedExtensions = {".jpg", ".png", ".bmp", ".gif", ".webp"};

        if(file.isDirectory()){
            return true;
        }
        if(file.isFile()){
            String path = file.getName(); //przekazana nazwa pliku lub folderu
                for(String allowedExtension: allowedExtensions){
                    if(path.contains(allowedExtension)){
                       return true;
                    }
                }

        }
        return false;
    }
}