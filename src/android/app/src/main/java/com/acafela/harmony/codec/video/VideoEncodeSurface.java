package com.acafela.harmony.codec.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_QUEUE_TIMEOUT_US;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_MIME_TYPE;


public class VideoEncodeSurface {
    private static final String TAG = VideoEncodeSurface.class.getName();

    private MediaCodec mCodec;
    private Surface mInputSurface;
    private MediaCodec.BufferInfo mBufferInfo;

    VideoEncodeSurface.VideoCallback mVideoCallback;

    public VideoEncodeSurface() {
        mBufferInfo = new MediaCodec.BufferInfo();
    }

    public void start(MediaFormat format) {
        try {
            mCodec = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCodec.configure(
                format,
                null,
                null /* crypto */,
                MediaCodec.CONFIGURE_FLAG_ENCODE);

        mInputSurface = mCodec.createInputSurface();
        mCodec.start();
    }

    public void stop() {
        mCodec.stop();
        mCodec.release();
    }

    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void setCallback(VideoEncodeSurface.VideoCallback callback) {
        mVideoCallback = callback;
    }

    public void drainEncoder() {
        int outputBufferId = mCodec.dequeueOutputBuffer(mBufferInfo, AUDIO_QUEUE_TIMEOUT_US);

        if (outputBufferId < 0) {
            Log.i(TAG, "outputBufferId: " + outputBufferId);
            return;
        }
        ByteBuffer outputBuffer = mCodec.getOutputBuffer(outputBufferId);
        byte[] outputBytes = new byte[mBufferInfo.size];
        outputBuffer.get(outputBytes, 0, mBufferInfo.size);
        if (mVideoCallback != null) {
            mVideoCallback.onOutputBytesAvailable(outputBytes);
        }
        mCodec.releaseOutputBuffer(outputBufferId, false);
        return;
    }

    public static abstract class VideoCallback {
        public abstract void onOutputBytesAvailable(byte[] outputBytes);
    }
}
