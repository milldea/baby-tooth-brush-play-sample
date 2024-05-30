package com.example.toothbrush;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.annotation.KeepName;
import com.example.toothbrush.facemeshdetector.FaceMeshDetectorProcessor;

import java.io.IOException;

/** Live preview demo for ML Kit APIs. */
@KeepName
public final class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "LivePreviewActivity";

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    private ImageButton buttonA;
    private ImageButton buttonI;
    private ParticleView particleView;
    private boolean selectedA;

    private ImageView imageA;
    private ImageView imageI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.preview_view);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        imageA = findViewById(R.id.imageView);
        imageI = findViewById(R.id.imageView2);

        buttonA = findViewById(R.id.button_a);
        buttonI = findViewById(R.id.button_i);
        ImageButton buttonCamera = findViewById(R.id.button_camera);
        particleView = findViewById(R.id.particleView);
        particleView.setAlpha(0);

        buttonA.setOnClickListener(v -> {
            setButtonStatus(buttonA, buttonI, imageA, imageI);
            selectedA = true;
        });
        buttonI.setOnClickListener(v -> {
            setButtonStatus(buttonI, buttonA, imageI, imageA);
            selectedA = false;
        });
        setButtonStatus(buttonA, buttonI, imageA, imageI);
        selectedA = true;

        buttonCamera.setOnClickListener(v -> {
            View rootView = getWindow().getDecorView().getRootView();
            ScreenshotUtils.takeScreenshot(this, rootView);
        });

        requestPermission();
        createCameraSource();

        // receiver
        UpdateReceiver receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("DO_ACTION");
        registerReceiver(receiver, filter);
    }

    private void setButtonStatus(ImageButton pressedBtn,
                                 ImageButton otherBtn,
                                 ImageView pressedView,
                                 ImageView otherView){
        pressedBtn.setScaleX(0.9f);
        pressedBtn.setScaleY(0.9f);
        pressedBtn.setAlpha(1f);
        pressedBtn.setAlpha(1f);

        otherBtn.setScaleX(1f);
        otherBtn.setScaleY(1f);
        otherBtn.setAlpha(0.5f);
        otherBtn.setAlpha(0.5f);

        pressedView.setAlpha(1f);
        otherView.setAlpha(0f);
    }

    private void requestPermission(){
        requestPermissions(
                new String[] { Manifest.permission.CAMERA,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },
                9999);
    }

    protected class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            Bundle extras = intent.getExtras();
            String msg = extras.getString("message");
            if ((msg.equals("あ") && selectedA) || (msg.equals("い") && !selectedA)) {
                particleView.setAlpha(1);
            } else {
                particleView.setAlpha(0);
            }
            Log.d("tooth brush", msg);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
        }

        try {
            cameraSource.setMachineLearningFrameProcessor(new FaceMeshDetectorProcessor(this));
        } catch (RuntimeException e) {
            Toast.makeText(
                            getApplicationContext(),
                            "Can not create image processor: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createCameraSource();
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}
