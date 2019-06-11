package com.acafela.harmony.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.acafela.harmony.codec.AudioCodecSync.MIME_TYPE;
import static com.acafela.harmony.codec.AudioCodecSync.QUEUE_TIMEOUT_US;

public class AudioDecoderSync {
    private static final String TAG = AudioDecoderSync.class.getName();

    private MediaCodec mDecoder;

    public AudioDecoderSync() {
        try {
            mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(MediaFormat format) {
        mDecoder.configure(
                format,
                null /* surface */,
                null /* crypto */,
                0);

        mDecoder.start();
    }

    public void stop() {
        mDecoder.stop();
    }

    public void release() {
        mDecoder.release();
    }

    public byte[] decode(byte[] input) {
        int inputBufIndex = mDecoder.dequeueInputBuffer(QUEUE_TIMEOUT_US);
        if (inputBufIndex >= 0) {
            ByteBuffer dstBuf = mDecoder.getInputBuffer(inputBufIndex);
            dstBuf.clear();
            dstBuf.put(input);
            mDecoder.queueInputBuffer(inputBufIndex,
                    0,
                    input.length,
                    0,
                    0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mDecoder.dequeueOutputBuffer(bufferInfo, QUEUE_TIMEOUT_US);

        if (outputBufferId < 0) {
            Log.i(TAG, "outputBufferId: " + outputBufferId);
            return null;
        }
        ByteBuffer outputBuffer = mDecoder.getOutputBuffer(outputBufferId);
        byte[] outData = new byte[bufferInfo.size];
        outputBuffer.get(outData);

        mDecoder.releaseOutputBuffer(outputBufferId, false);
        return outData;
    }

}

