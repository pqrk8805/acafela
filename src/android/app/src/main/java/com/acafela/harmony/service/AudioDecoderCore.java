package com.acafela.harmony.service;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;


public class AudioDecoderCore {
    private static final String TAG = AudioDecoderCore.class.getName();

    // TODO: these ought to be configurable as well
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;    // H.264 Advanced Video Coding
    private static final int KEY_SAMPLE_RATE  = 8000;               // 30fps
    private static final int KEY_CHANNEL_COUNT = 1;           // 5 seconds between I-frames
    private static final int TIMEOUT_US = 100000;

    private MediaCodec mDecoder;
    private MediaCodec.BufferInfo mBufferInfo;

    public AudioDecoderCore()
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

        mDecoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mDecoder.configure(
                format,
                null /* surface */,
                null /* crypto */,
                MediaCodec.CONFIGURE_FLAG_ENCODE);

        mDecoder.start();
    }
        public void encode(byte[] src) {
            int inputBufferId = mDecoder.dequeueInputBuffer(TIMEOUT_US);
            if (inputBufferId >= 0) {
                ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferId);
                inputBuffer.put(src);
                mDecoder.queueInputBuffer(inputBufferId, 0, src.length, TIMEOUT_US, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            if (outputBufferId >= 0) {
                ByteBuffer outputBuffer = mDecoder.getOutputBuffer(outputBufferId);
                MediaFormat bufferFormat = mDecoder.getOutputFormat(outputBufferId); // option A
                // bufferFormat is identical to outputFormat
                // outputBuffer is ready to be processed or rendered.

                mDecoder.releaseOutputBuffer(outputBufferId, false);
            }
            else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // Can ignore if using getOutputFormat(outputBufferId)
                MediaFormat  outputFormat = mDecoder.getOutputFormat(); // option B
            }
        }
}

