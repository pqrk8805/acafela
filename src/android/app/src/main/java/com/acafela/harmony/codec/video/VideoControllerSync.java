package com.acafela.harmony.codec.video;

public class VideoControllerSync {
    private static final String TAG = VideoControllerSync.class.getName();

    VideoEncodeSync mVideoEncoder = new VideoEncodeSync(true);
    VideoEncodeSync mVideoDecoder = new VideoEncodeSync(false);

    public void start() {
        mVideoEncoder.start();
        mVideoDecoder.start();
    }

    public void stop() {
        mVideoEncoder.stop();
        mVideoDecoder.stop();
    }

    public byte[] encode(byte[] input) {
        return mVideoEncoder.handle(input);
    }

    public byte[] decode(byte[] input) {
        return mVideoDecoder.handle(input);
    }
}
