package com.acafela.harmony.codec.audio;

import android.media.MediaFormat;

public class AudioMediaFormat {
    private static final String TAG = AudioMediaFormat.class.getName();

    public static final String AUDIO_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;
    public static final int AUDIO_SAMPLE_RATE = 8000;
    public static final int AUDIO_CHANNEL_COUNT = 1;
    public static final int AUDIO_BIT_RATE_4750 = 4750;
    public static final int AUDIO_BIT_RATE_5900 = 5900;
    public static final int AUDIO_BIT_RATE_6700 = 6700;
    public static final int AUDIO_BIT_RATE_7950 = 7950;
    public static final int AUDIO_BIT_RATE_12200 = 12200;

    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_INTERVAL = 20;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sample
    public static final int RAW_BUFFER_SIZE = AUDIO_SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    public static final int AUDIO_QUEUE_TIMEOUT_US = 10000;
    public static final int AUDIO_QUEUE_BOUND = 100;

    private MediaFormat mFormat = new MediaFormat();

    public AudioMediaFormat() {
        mFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);
        mFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SAMPLE_RATE);
        mFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AUDIO_CHANNEL_COUNT);
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE_12200);
    }

    public MediaFormat getAudioMediaFormat() {
        return mFormat;
    }
}
