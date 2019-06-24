package com.acafela.harmony.codec.video;

import android.view.Surface;

public class VideoControllerAsync {
    private static final String TAG = VideoControllerAsync.class.getName();

    VideoEncodeSync mVideoEncoder = new VideoEncodeSync(true);
    VideoDecodeAsyncSurface mVideoDecoder;
    boolean isStarted;

    public VideoControllerAsync() {
        isStarted = false;
    }

    public void start(Surface surface) {
        mVideoDecoder = new VideoDecodeAsyncSurface(surface);
        mVideoEncoder.start();
        mVideoDecoder.start();
        isStarted = true;
    }

    public void stop() {
        mVideoEncoder.stop();
        mVideoDecoder.stop();
    }

    public byte[] encode(byte[] input) {
        if (!isStarted) {
            return null;
        }
        return mVideoEncoder.handle(input);
    }

    public void enqueueEncodedBytes(byte[] encodedBytes) {
        mVideoDecoder.enqueueInputBytes(encodedBytes);
    }
}
