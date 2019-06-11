package com.acafela.harmony.codec;

import android.media.MediaFormat;

public class AudioCodecSync {
    private static final String TAG = AudioCodecSync.class.getName();

    protected static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;
    protected static final int KEY_SAMPLE_RATE  = 8000;
    protected static final int KEY_CHANNEL_COUNT = 1;
    protected static final int KEY_BIT_RATE = 4750;
    protected static final int QUEUE_TIMEOUT_US = 10000;

    protected MediaFormat mFormat = new MediaFormat();
    AudioEncoderSync mAudioEncoder = new AudioEncoderSync();
    AudioDecoderSync mAudioDecoder = new AudioDecoderSync();

    public AudioCodecSync() {
        mFormat.setString(MediaFormat.KEY_MIME, MIME_TYPE);
        mFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, KEY_SAMPLE_RATE);
        mFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, KEY_CHANNEL_COUNT);
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE);
    }

    public void start() {
        mAudioEncoder.start(mFormat);
        mAudioDecoder.start(mFormat);
    }

    public void stop() {
        mAudioEncoder.stop();
        mAudioDecoder.stop();
    }

    public void release() {
        mAudioEncoder.release();
        mAudioDecoder.release();
    }

    public byte[] encode(byte[] input) {
        return mAudioEncoder.encode(input);
    }

    public byte[] decode(byte[] input) {
        return mAudioDecoder.decode(input);
    }
}
