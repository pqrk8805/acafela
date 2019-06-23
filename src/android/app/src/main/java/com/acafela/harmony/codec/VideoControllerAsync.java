package com.acafela.harmony.codec;

import com.acafela.harmony.codec.VideoCodecAsync.VideoCallback;

public class VideoControllerAsync {
    private static final String TAG = VideoControllerAsync.class.getName();

    VideoMediaFormat mVideoMediaFormat = new VideoMediaFormat(true);
    VideoCodecAsync mVideoEncoder = new VideoCodecAsync(true);
    VideoCodecAsync mVideoDecoder = new VideoCodecAsync(false);

    public void start() {
        mVideoEncoder.start(mVideoMediaFormat.getVideoMediaFormat());
        mVideoDecoder.start(mVideoMediaFormat.getVideoMediaFormat());
    }

    public void stop() {
        mVideoEncoder.stop();
        mVideoDecoder.stop();
    }

    public void enqueueRawBytes(byte[] rawBytes) {
        mVideoEncoder.enqueueInputBytes(rawBytes);
    }

    public void enqueueEncodedBytes(byte[] encodedBytes) {
        mVideoDecoder.enqueueInputBytes(encodedBytes);
    }

    public void setEncoderCallback(VideoCallback callback) {
        mVideoEncoder.setCallback(callback);
    }

    public void setDecoderCallback(VideoCallback callback) {
        mVideoDecoder.setCallback(callback);
    }
}
