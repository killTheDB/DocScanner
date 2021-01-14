package com.example.docscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    ListView lv_pdf;
    public static ArrayList<File> filelist = new ArrayList<File>();
    PDFAdapter obj_Adapter;
    File dir;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 9999;
    Uri imageUri;

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
    }

    private void init() {
        lv_pdf = (ListView)findViewById(R.id.lv_pdf);
        dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"DocScannerStorage");
        getfile(dir);

        obj_Adapter = new PDFAdapter(getApplicationContext(),filelist);
        lv_pdf.setAdapter(obj_Adapter);

        lv_pdf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),PDFActivity.class);
                intent.putExtra("position",1);
                startActivity(intent);
            }
        });
    }

    private ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if(listFile != null && listFile.length > 0){
            for(int i = 0;i < listFile.length;++i){
                if(listFile[i].isDirectory()){
                    getfile(listFile[i]);
                }
                else {
                    boolean booleanpdf = false;
                    if(listFile[i].getName().endsWith(".pdf")){
                        for(int j = 0;j < filelist.size();++j){
                            if(filelist.get(j).getName().equals(listFile[i].getName())){
                                booleanpdf = true;
                            }
                        }
                        if(booleanpdf){
                            booleanpdf = false;
                        }
                        else {
                            filelist.add(listFile[i]);
                        }
                    }
                }
            }
        }
        return filelist;
    }

    public void check(View v){
//        Toast.makeText(this, "working", Toast.LENGTH_SHORT).show();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission,PERMISSION_CODE);
            }
            else{
                OpenCamera();
            }
        }
        else{
            OpenCamera();
        }
    }

    private void OpenCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Image");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From Cam");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
//        startActivity(cameraIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent i = new Intent(HomeActivity.this,ScanActivity.class);
            i.setData(imageUri);
            startActivity(i);

        }
    }
}
