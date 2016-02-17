package vkraevskiy.erminesoft.com.camerasample;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "CameraSample";

    private static final long RECORDING_TIME_MILLIS = 10000;

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mRecorder;

    private boolean isRecording;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(mPreview);

        isRecording = false;

        Button button = (Button) findViewById(R.id.capture_button);
        button.setOnClickListener(new OnClickListenerImpl());
    }

    private Camera getCameraInstance() {
        Camera camera = null;

        try {
            camera = Camera.open();
        } catch (Exception e) {
            // Camera not available
        }

        return camera;
    }
    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), TAG);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
    }

    private final class OnClickListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (isRecording) {
                Toast.makeText(getApplicationContext(), "Still recording", Toast.LENGTH_SHORT).show();
                return;
            }

            mRecorder = new MediaRecorder();

            mCamera.unlock();
            mRecorder.setCamera(mCamera);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

            final String path = Uri.fromFile(getOutputMediaFile()).getPath();
            mRecorder.setOutputFile(path);

            mRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                releaseMediaRecorder();
            }

            mRecorder.start();

            isRecording = true;

            Toast.makeText(getApplicationContext(), "Recording start", Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecorder.stop();
                    releaseMediaRecorder();

                    isRecording = false;

                    Log.i(TAG, path);
                }
            }, RECORDING_TIME_MILLIS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }
}
