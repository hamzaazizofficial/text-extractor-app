 package com.app.textrecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

 public class MainActivity extends AppCompatActivity {

     EditText Result;
     ImageView Preview;
     Button copyBtn;
     //Button floatBtn;

     private static final int CAMERA_REQUEST_CODE = 200;
     private static final int STORAGE_REQUEST_CODE = 400;
     private static final int IMAGE_PICK_GALLERY_CODE = 1000;
     private static final int IMAGE_PICK_CAMERA_CODE = 1001;

     String[] cameraPermission;
     String[] storagePermission;
     Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setSubtitle("Click + Button to insert Image");

        Result = findViewById(R.id.output);
        Preview = findViewById(R.id.image1);
        copyBtn = findViewById(R.id.copyBtn);
       // floatBtn = findViewById(R.id.addImg2);



        //Function for Copy text from Result menu
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Result",Result.getText().toString());
                clipboard.setPrimaryClip(clipData);

                //toast for showing Copied to Clipboard
                Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }

     //floating btn to perfrom task
     //public void floatTask(View view){
       //  Toast.makeText(this, "Toast", Toast.LENGTH_SHORT).show();
     //}

    //actionBar Menu

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
         getMenuInflater().inflate(R.menu.menu_main,menu);
         return true;
     }
     //handle action bar clicks
     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.addImg2){
            showImageImportDialog();
        }
        if(id==R.id.settings){
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        }
         return super.onOptionsItemSelected(item);
     }

     private void showImageImportDialog() {
        //items to display in dialog
        String[] items = {" Camera", " Gallery"};
         AlertDialog.Builder dialog = new AlertDialog.Builder(this);
         //set title
         dialog.setTitle("Select Image");
         dialog.setItems(items, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //camera option clicked
                    //permission for camera and storage
                    if (!checkCameraPermission()){
                        //camera permission not allowed, request it
                        requestCameraPermission();
                    }
                    else{
                        //permission allowed, open camera and take pic
                        pickCamera();
                    }
                }
                if (which==1){
                    //galery option clicked
                    if (!checkStoragePermission()){
                        //Storage permission not allowed, request it
                        requestStoragePermission();
                    }
                    else{
                        //permission allowed, open gallery and browse pics
                        pickGallery();
                    }
                }
             }
         });
         dialog.create().show(); //showing dialog
    }

     private void pickGallery() {
        //intent to pick image from gallery
         Intent intent = new Intent(Intent.ACTION_PICK);
         //set intent type to image
         intent.setType("image/*");
         startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
     }

     private void pickCamera() {
        //intent to take image from camera, it'll also be save to storage to get higher quality img
         ContentValues values = new ContentValues();
         values.put(MediaStore.Images.Media.TITLE,"NewPic"); //title of the pic
         values.put(MediaStore.Images.Media.DESCRIPTION,"Text Recognizer");
         image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

         Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
         startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

     private void requestStoragePermission() {
         ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE);
     }

     private boolean checkStoragePermission() {
         boolean output = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 == (PackageManager.PERMISSION_GRANTED);
        return output;
    }

     private void requestCameraPermission() {
         ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
     }

     private boolean checkCameraPermission() {
        //check camera permsn and return output
        boolean output = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean output1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return output && output1;
     }

     //handle permission result
     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode){
                case CAMERA_REQUEST_CODE:
                    if (grantResults.length>0){
                        boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (cameraAccepted && writeStorageAccepted){
                            pickCamera();
                        }
                        else{
                            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case STORAGE_REQUEST_CODE:
                    if (grantResults.length>0){
                        boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (writeStorageAccepted){
                            pickGallery();
                        }
                        else{
                            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
     }

     //handle image result

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK) {
             if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                 //got image from gallery now crop it
                 CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON)//enable image guidelines
                         .start(this);
             }
             if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                 //got img from camera now crop it
                 CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON)//enable image guidelines
                         .start(this);
             }
         }
         //getting cropped img
         if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode == RESULT_OK) {
                 Uri resultUri = result.getUri(); ///getting img uri
                 //set img to imageView
                 Preview.setImageURI(resultUri);

                 //get drawable bitmap for text recognition
                 BitmapDrawable bitmapDrawable = (BitmapDrawable) Preview.getDrawable();
                 Bitmap bitmap = bitmapDrawable.getBitmap();

                 TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                 if (!recognizer.isOperational()) {
                     Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
                 } else {
                     Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                     SparseArray<TextBlock> items = recognizer.detect(frame);
                     StringBuilder stringBuilder = new StringBuilder();

                     //getting text from stringbuilder until there is no text
                     for (int i = 0; i < items.size(); i++) {
                         TextBlock myItem = items.valueAt(i);
                         stringBuilder.append(myItem.getValue());
                         stringBuilder.append("\n");
                     }
                     //set text to editText
                     Result.setText(stringBuilder.toString());
                 }
             } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                 //if any err then show it in Toast
                 Exception error = result.getError();
                 Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
             }
         }
     }
 }
