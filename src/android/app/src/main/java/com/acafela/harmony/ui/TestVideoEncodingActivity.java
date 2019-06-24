package com.acafela.harmony.ui;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.acafela.harmony.R;
import com.acafela.harmony.ui.camera.CameraPreview;

public class TestVideoEncodingActivity extends AppCompatActivity implements
        TextureView.SurfaceTextureListener {
    private static final String TAG = TestVideoEncodingActivity.class.getName();

    private CameraPreview mPreview;
    private TextureView mTextureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video_encoding);

        mTextureView = findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface s = new Surface(surface);
        mPreview.setOutputSurface(s);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}