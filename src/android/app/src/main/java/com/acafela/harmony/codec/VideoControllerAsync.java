package com.acafela.harmony.codec;

import android.view.Surface;

public class VideoControllerAsync {
    private static final String TAG = VideoControllerAsync.class.getName();

    VideoMediaFormat mVideoMediaFormatSync = new VideoMediaFormat(false);
    VideoMediaFormat mVideoMediaFormat = new VideoMediaFormat(true);
    VideoEncodeSync mVideoEncoder = new VideoEncodeSync(true);
    VideoDecodeAsync mVideoDecoder;
    boolean isStarted;

    public VideoControllerAsync() {
        isStarted = false;
    }

    public void start(Surface surface) {
        mVideoDecoder = new VideoDecodeAsync(false, surface);
        mVideoEncoder.start(mVideoMediaFormatSync.getMediaFormat());
        mVideoDecoder.start(mVideoMediaFormat.getMediaFormat());
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
