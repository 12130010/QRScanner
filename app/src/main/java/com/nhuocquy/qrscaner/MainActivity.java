package com.nhuocquy.qrscaner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private Button barcodeInfo;
    private BarcodeDetector barcodeDetector;
    private boolean isScanned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                init();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isScanned = false;
            }
        });


        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeInfo = (Button) findViewById(R.id.code_info);

        init();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                                               @Override
                                               public void surfaceCreated(SurfaceHolder holder) {
                                                   if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                       return;
                                                   }
                                                   try {
                                                       if (cameraSource != null) {
                                                           cameraSource.start(cameraView.getHolder());
                                                       }
                                                   } catch (IOException e) {
                                                       e.printStackTrace();
                                                   }
                                               }

                                               @Override
                                               public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                               }

                                               @Override
                                               public void surfaceDestroyed(SurfaceHolder holder) {
                                                   if (cameraSource != null)
                                                       cameraSource.stop();
                                               }
                                           }

        );

        barcodeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScanned)
                Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        barcodeInfo.setText("Scanning...");

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        startCamera();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            StringBuilder sb = new StringBuilder();
                            barcodeInfo.setText(
                                    barcodes.valueAt(0).displayValue
                            ); // Update the TextView
                            stopCamera();
                            isScanned = true;
//                            Snackbar.make(cameraView, "Click to continue...", 5000)
//                                    .setAction("Go", new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }).show();
                        }
                    });
                }

            }
        });
    }


    private void stopCamera() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }
    }

    private void startCamera() {

        if (cameraSource == null) {
            cameraSource = new CameraSource
                    .Builder(this, barcodeDetector)
                    .setRequestedPreviewSize(480, 320)
                    .build();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
    }
}
