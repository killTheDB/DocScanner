package com.example.docscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.docscanner.Retrofit.IUploadApi;
import com.example.docscanner.Retrofit.RetrofitClient;
import com.example.docscanner.Utils.Common;
import com.example.docscanner.Utils.IUploadCallbacks;
import com.example.docscanner.Utils.ProgressRequestBody;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanActivity extends AppCompatActivity implements IUploadCallbacks {
    ImageView imageView,btnUpload;
    IUploadApi mService;
    Uri selectedFileUri;
    ProgressDialog dialog;

    private IUploadApi getApiUpload(){
        return RetrofitClient.getClient().create(IUploadApi.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mService = getApiUpload();
        imageView = findViewById(R.id.image_view);
        btnUpload = findViewById(R.id.button_upload);

        Uri image_uri = getIntent().getData();
        imageView.setImageURI(image_uri);
        selectedFileUri = image_uri;

//        btnUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(ScanActivity.this, "Test", Toast.LENGTH_SHORT).show();
//                uploadFile();
//
//            }
//        });

//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    }

    private void uploadFile(){
        Toast.makeText(ScanActivity.this, "Test1", Toast.LENGTH_SHORT).show();
        if(selectedFileUri != null){
            Toast.makeText(ScanActivity.this, "Test2", Toast.LENGTH_SHORT).show();
            dialog = new ProgressDialog(ScanActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Uploading....");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();

            File file = null;
            try {
                file = new File(Common.getFilePath(this,selectedFileUri));
            }
            catch (URISyntaxException e){
                e.printStackTrace();
            }
            if(file != null){
                final ProgressRequestBody requestBody =new ProgressRequestBody(this,file);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("image",file.getName(),requestBody);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mService.uploadFile(body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        String image_processed_link = "http://54.167.216.49:80" +
                                                response.body().replace("\"", "");

                                        Toast.makeText(ScanActivity.this, "Please wait,Processing the Image", Toast.LENGTH_SHORT).show();

                                        Picasso.get()
                                                .load(image_processed_link)
                                                .fit().centerInside()
                                                .rotate(90)
                                                .into(imageView);

                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        Toast.makeText(ScanActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).start();
            }

        }
        else {
            Toast.makeText(this, "Cannot Upload this file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    public void upbutton(View view) {
        Toast.makeText(ScanActivity.this, "Test"+selectedFileUri.toString(), Toast.LENGTH_SHORT).show();
                uploadFile();
    }
}
