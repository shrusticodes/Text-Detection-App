package com.example.textdetection;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {
    private ImageView iv;
    private TextView resu;
    private Button snapbtn, detbtn;
    private Bitmap imgbitmap;
    static final int RES_I = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        iv = findViewById(R.id.capture);
        resu = findViewById(R.id.tV2);
        snapbtn = findViewById(R.id.snap);
        detbtn = findViewById(R.id.detect);
        detbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();

            }
        });
        snapbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    captureImage();
                } else
                    reqPermission();
            }
        });
    }

    private void detectText() {
        InputImage ip = InputImage.fromBitmap(imgbitmap, 0);
        TextRecognizer r = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = r.process(ip).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String bt = block.getText();
                    Point[] bp = block.getCornerPoints();
                    Rect br = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lt = line.getText();
                        Point[] lp = line.getCornerPoints();
                        Rect lr = line.getBoundingBox();

                        for (Text.Element element : line.getElements()) {
                            String el = element.getText();
                            result.append(el);
                        }
                        resu.setText(bt);

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Failed to detect text" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkPermission() {
        int camp = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return camp == PackageManager.PERMISSION_GRANTED;
    }

    private void reqPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean camera_permission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (camera_permission) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                captureImage();
            } else
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RES_I && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                imgbitmap = (Bitmap) extras.get("data");
                iv.setImageBitmap(imgbitmap);
            }

        }



        private void captureImage () {
            Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePic.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePic, RES_I);
            }
        }
    }
