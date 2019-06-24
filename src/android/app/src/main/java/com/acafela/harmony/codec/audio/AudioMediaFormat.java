package com.acafela.harmony.codec.audio;

import android.media.MediaFormat;

public class AudioMediaFormat {
    private static final String TAG = AudioMediaFormat.class.getName();

    public static final String AUDIO_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;
    public static final int AUDIO_SAMPLE_RATE = 8000;
    public static final int AUDIO_CHANNEL_COUNT = 1;
    public static final int AUDIO_BIT_RATE = 4750;
    public static final int AUDIO_QUEUE_TIMEOUT_US = 10000;
    public static final int AUDIO_FRAME_BYTE = 160* AUDIO_CHANNEL_COUNT *2;
    public static final int AUDIO_QUEUE_BOUND = 100;

    private MediaFormat mFormat = new MediaFormat();

    public AudioMediaFormat() {
        mFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);
        mFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SAMPLE_RATE);
        mFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AUDIO_CHANNEL_COUNT);
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
    }

    public MediaFormat getAudioMediaFormat() {
        return mFormat;
    }
}
