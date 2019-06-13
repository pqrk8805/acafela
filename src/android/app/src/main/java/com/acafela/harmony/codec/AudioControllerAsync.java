package com.acafela.harmony.codec;

import com.acafela.harmony.codec.AudioCodecAsync.AudoiCallback;

public class AudioControllerAsync {
    private static final String TAG = AudioControllerAsync.class.getName();

    AudioMediaFormat mAudioMediaFormat = new AudioMediaFormat();
    AudioCodecAsync mAudioEncoder = new AudioCodecAsync(true);
    AudioCodecAsync mAudioDecoder = new AudioCodecAsync(false);

    public void start() {
        mAudioEncoder.start(mAudioMediaFormat.getAudioMediaFormat());
        mAudioDecoder.start(mAudioMediaFormat.getAudioMediaFormat());
    }

    public void stop() {
        mAudioEncoder.stop();
        mAudioDecoder.stop();
    }

    public void enqueueRawBytes(byte[] rawBytes) {
        mAudioEncoder.enqueueInputBytes(rawBytes);
    }

    public void enqueueEncodedBytes(byte[] encodedBytes) {
        mAudioDecoder.enqueueInputBytes(encodedBytes);
    }

    public void setEncoderCallback(AudoiCallback callback) {
        mAudioEncoder.setCallback(callback);
    }

    public void setDecoderCallback(AudoiCallback callback) {
        mAudioDecoder.setCallback(callback);
    }
}