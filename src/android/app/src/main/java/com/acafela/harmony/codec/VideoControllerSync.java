package com.acafela.harmony.codec;

public class VideoControllerSync {
    private static final String TAG = VideoControllerSync.class.getName();

    VideoMediaFormat mVideoMediaFormat = new VideoMediaFormat(false);
    VideoEncodeSync mVideoEncoder = new VideoEncodeSync(true);
    VideoEncodeSync mVideoDecoder = new VideoEncodeSync(false);

    public void start() {
        mVideoEncoder.start(mVideoMediaFormat.getMediaFormat());
        mVideoDecoder.start(mVideoMediaFormat.getMediaFormat());
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
