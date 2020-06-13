package com.zabih.chatBuzz.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zabih.chatBuzz.R;

import java.util.UUID;

public class UpoadFIle extends AppCompatActivity {
    CardView SelectFile,Upload;
    TextView notification;
    Uri csvUri;
    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upoad_file);
        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();

        SelectFile=findViewById(R.id.selectFile);
        notification=findViewById(R.id.notification);
        Upload=findViewById(R.id.UploadBtn);

        SelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(UpoadFIle.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

                    selectCSV();
                }
                else
                    ActivityCompat.requestPermissions(UpoadFIle.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
            }
        });

        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(csvUri!=null)
                    uploadFile(csvUri);
                else
                    Toast.makeText(UpoadFIle.this, "Select a File..", Toast.LENGTH_SHORT).show();
            }
        });
    }
    String filename =System.currentTimeMillis()+"";
    private void uploadFile(Uri csvUri) {

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading File...");
        progressDialog.setProgress(0);
        progressDialog.show();

        StorageReference storageReference=storage.getReference();
        storageReference.child("Uploads").child(filename).putFile(csvUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String url=taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                DatabaseReference reference=database.getReference();
                reference.child(filename).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(UpoadFIle.this, "File Successfully Uploaded!", Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(UpoadFIle.this, "File Not Successfully Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                e.getMessage();
                Toast.makeText(UpoadFIle.this, "File Not Successfully Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            selectCSV();
        }
        else
            Toast.makeText(UpoadFIle.this,"Please provide Permission..",Toast.LENGTH_SHORT).show();
    }

    private void selectCSV() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("text/comma-separated-values");
        startActivityForResult(intent,86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            csvUri = data.getData();
            notification.setText("File Selected: " + data.getData().getLastPathSegment());

        } else
            Toast.makeText(UpoadFIle.this, "Please Select a file", Toast.LENGTH_SHORT).show();
    }
}
