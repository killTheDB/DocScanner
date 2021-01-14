package com.example.docscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.docscanner.Retrofit.IUploadApi;
import com.example.docscanner.Retrofit.RetrofitClient;
import com.example.docscanner.Utils.Common;
import com.example.docscanner.Utils.IUploadCallbacks;
import com.example.docscanner.Utils.ProgressRequestBody;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanActivity extends AppCompatActivity implements IUploadCallbacks {
    ImageView imageView,btnUpload,Save,Cancel;
    IUploadApi mService;
    Uri selectedFileUri;
    ProgressDialog dialog;

    public String image_name = "";

    BitmapDrawable bitmapDrawable;
    Bitmap bitmap;

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
        Save = findViewById(R.id.save);
        Cancel = findViewById(R.id.cancel);

        Uri image_uri = getIntent().getData();
        imageView.setImageURI(image_uri);
        selectedFileUri = image_uri;

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ScanActivity.this,HomeActivity.class);
                startActivity(i);
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                bitmap = bitmapDrawable.getBitmap();
                FileOutputStream outputStream = null;
                File sdCard = Environment.getExternalStorageDirectory();
                File directory = new File(sdCard.getAbsolutePath()+"/DocScannerStorage");
                directory.mkdir();
                String file_name = image_name + ".jpg";
                File outFile = new File(directory,file_name);

                try{
                    outputStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Document document = new Document();
                String directoryPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/DocScannerStorage";
                try{
                    PdfWriter.getInstance(document,new FileOutputStream(directoryPath + "/" + image_name + ".pdf"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

                document.open();

                Image image = null;
                try{
                    image = Image.getInstance(directoryPath + "/" + image_name + ".jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadElementException e) {
                    e.printStackTrace();
                }
                float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() -0) /image.getWidth())*100;
                image.scalePercent(scaler);;
                image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                try{
                    document.add(image);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                document.close();
                Toast.makeText(ScanActivity.this, "PDF Saved Successfully..!!", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(ScanActivity.this,HomeActivity.class);
                startActivity(i);

            }
        });

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
//            Toast.makeText(ScanActivity.this, "Test2", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ScanActivity.this, "Test2", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            Toast.makeText(ScanActivity.this, "Test2 "+file.toString(), Toast.LENGTH_SHORT).show();
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
                                        String image_processed_link = "http://54.167.216.49/" +
                                                response.body().replace("\"", "");

                                        Toast.makeText(ScanActivity.this, "Please wait,Processing the Image "+response.body(), Toast.LENGTH_SHORT).show();


                                        Picasso.get()
                                                .load(image_processed_link)
                                                .fit().centerInside()
                                                .rotate(90)
                                                .into(imageView);

                                        image_name += response.body().replace("\"","").split("/")[1];
                                        btnUpload.setVisibility(View.INVISIBLE);
                                        Save.setVisibility(View.VISIBLE);
                                        Cancel.setVisibility(View.VISIBLE);

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
