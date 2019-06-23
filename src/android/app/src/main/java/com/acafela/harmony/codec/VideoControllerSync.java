package com.acafela.harmony.codec;

public class VideoControllerSync {
    private static final String TAG = VideoControllerSync.class.getName();

    VideoMediaFormat mVideoMediaFormat = new VideoMediaFormat(false);
    VideoCodecSync mVideoEncoder = new VideoCodecSync(true);
    VideoCodecSync mVideoDecoder = new VideoCodecSync(false);

    public void start() {
        mVideoEncoder.start(mVideoMediaFormat.getVideoMediaFormat());
        mVideoDecoder.start(mVideoMediaFormat.getVideoMediaFormat());
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
