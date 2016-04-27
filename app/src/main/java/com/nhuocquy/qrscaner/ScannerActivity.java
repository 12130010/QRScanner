package com.nhuocquy.qrscaner;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScannerActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ACCOUNT = 1;
    public static final String MY_DATA = "my_data";
    public static final String ACCOUNT_NAME = "accountName";

    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private Button btnURL;
    private Button btnAcc;
    private BarcodeDetector barcodeDetector;
    private boolean isScanned;
    private SharedPreferences ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ref = getSharedPreferences(MY_DATA, MODE_PRIVATE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                init();
                if (ActivityCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
        btnURL = (Button) findViewById(R.id.btnURL);

        btnAcc = (Button) findViewById(R.id.btnAcc);
        btnAcc.setText(ref.getString(ACCOUNT_NAME,"Please Choose Account..."));

        init();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                                               @Override
                                               public void surfaceCreated(SurfaceHolder holder) {
                                                   Log.e("NhuocQuyaaa", "surfaceCreated" + this);
                                                   if (ActivityCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                                                   Log.e("NhuocQuyaaa", "surfaceDestroyed" + this);
                                                   if (cameraSource != null)
                                                       cameraSource.stop();
                                               }
                                           }

        );

        btnURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanned)
                    Toast.makeText(ScannerActivity.this, "Click", Toast.LENGTH_SHORT).show();
            }
        });
        btnAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
                startActivityForResult(googlePicker, REQUEST_CODE_ACCOUNT);
            }
        });
    }

    private void init() {
        btnURL.setText("Scanning...");

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
                    btnURL.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            StringBuilder sb = new StringBuilder();
                            btnURL.setText(
                                    barcodes.valueAt(0).displayValue
                            ); // Update the TextView
                            stopCamera();
                            isScanned = true;
//                            Snackbar.make(cameraView, "Click to continue...", 5000)
//                                    .setAction("Go", new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            Toast.makeText(ScannerActivity.this, "Click", Toast.LENGTH_SHORT).show();
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

    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == REQUEST_CODE_ACCOUNT && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            btnAcc.setText(accountName);
            SharedPreferences.Editor editor = ref.edit();
            editor.putString(ACCOUNT_NAME, accountName);
            editor.commit();
        }
    }
}
