package com.acafela.harmony.codec.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_MIME_TYPE;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_QUEUE_BOUND;


public class VideoDecodeAsync {
    private static final String TAG = VideoDecodeAsync.class.getName();

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private MediaCodec mCodec;
    private Surface mSurface;

    LinkedBlockingQueue<byte[]> mInputBytesQueue = new LinkedBlockingQueue<>(VIDEO_QUEUE_BOUND);

    public VideoDecodeAsync(Surface surface) {
        mSurface = surface;
    }

    public void start() {
        mHandlerThread = new HandlerThread("VideoDecoderHandler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        try {
            mCodec = MediaCodec.createDecoderByType(VIDEO_MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
//				Log.e(TAG, "onInputBufferAvailable");
                byte[] inputBytes = null;
                try {
//					Log.i(TAG, "mInputBytesQueue: " + mInputBytesQueue.size());
                    inputBytes = mInputBytesQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                ByteBuffer inputBuffer = mCodec.getInputBuffer(index);
                inputBuffer.clear();
                if (inputBytes != null) {
                    inputBuffer.put(inputBytes);
                }
                mCodec.queueInputBuffer(index, 0, inputBytes.length, 0, 0);
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                mCodec.releaseOutputBuffer(index, true);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                e.printStackTrace();
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                Log.e(TAG, format.toString());
            }
        }, mHandler);

        VideoMediaFormat format = new VideoMediaFormat(true);
        mCodec.configure(
                format.getMediaFormat(),
                mSurface,
                null /* crypto */,
                0);

        mCodec.start();
    }

    public void stop() {
        mCodec.stop();
        mHandlerThread.interrupt();
        mHandlerThread.quit();
        mCodec.release();
    }

    public void enqueueInputBytes(byte[] rawBytes) {
        mInputBytesQueue.offer(rawBytes);
    }
}
