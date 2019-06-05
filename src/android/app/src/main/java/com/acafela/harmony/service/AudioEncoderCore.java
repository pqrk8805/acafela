package com.acafela.harmony.service;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;


public class AudioEncoderCore {
    private static final String TAG = AudioEncoderCore.class.getName();
    private static final boolean VERBOSE = false;

    // TODO: these ought to be configurable as well
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;    // H.264 Advanced Video Coding
    private static final int KEY_SAMPLE_RATE  = 8000;               // 30fps
    private static final int KEY_CHANNEL_COUNT = 1;           // 5 seconds between I-frames

    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;

    public AudioEncoderCore()
            throws IOException {

        MediaFormat format  = new MediaFormat();

        final int kBitRates[] =
                { 4750, 5150, 5900, 6700, 7400, 7950, 10200, 12200 };
        for (int j = 0; j < kBitRates.length; ++j) {

            format.setString(MediaFormat.KEY_MIME, MIME_TYPE);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, KEY_SAMPLE_RATE);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, KEY_CHANNEL_COUNT);
            format.setInteger(MediaFormat.KEY_BIT_RATE, kBitRates[j]);
            //formats.push(format);
        }

        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(
                format,
                null /* surface */,
                null /* crypto */,
                MediaCodec.CONFIGURE_FLAG_ENCODE);

        mEncoder.start();
//        ByteBuffer[] codecInputBuffers = mEncoder.getInputBuffers();
//        ByteBuffer[] codecOutputBuffers = mEncoder.getOutputBuffers();
    }
}

