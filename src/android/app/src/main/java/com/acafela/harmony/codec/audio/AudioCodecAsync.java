package com.acafela.harmony.codec.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_MIME_TYPE;
import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_QUEUE_BOUND;

public class AudioCodecAsync {
	private static final String TAG = AudioCodecAsync.class.getName();

	private HandlerThread mHandlerThread;
	private Handler mHandler;

	private MediaCodec mCodec;
	private boolean mIsEncoder;

	LinkedBlockingQueue<byte[]> mInputBytesQueue = new LinkedBlockingQueue<>(AUDIO_QUEUE_BOUND);
	AudoiCallback mAudioCallback;

	public AudioCodecAsync(boolean isEncoder) {
		mIsEncoder = isEncoder;
	}

	public void start(MediaFormat format) {
		mHandlerThread = new HandlerThread(mIsEncoder?
				"AudioEncoderHandler":"AudioDecoderHandler");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());

		try {
			mCodec = mIsEncoder?
					MediaCodec.createEncoderByType(AUDIO_MIME_TYPE):
					MediaCodec.createDecoderByType(AUDIO_MIME_TYPE);
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
//				Log.e(TAG, "onOutputBufferAvailable");
				ByteBuffer outputBuffer = mCodec.getOutputBuffer(index);
				byte[] outputBytes = new byte[info.size];
				outputBuffer.get(outputBytes, 0, info.size);
				if (mAudioCallback != null) {
					mAudioCallback.onOutputBytesAvailable(outputBytes);
				}
				mCodec.releaseOutputBuffer(index, false);
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

		mCodec.configure(
				format,
				null /* surface */,
				null /* crypto */,
				mIsEncoder?
						MediaCodec.CONFIGURE_FLAG_ENCODE:
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

	public void setCallback(AudoiCallback callback) {
		mAudioCallback = callback;
	}

	public static abstract class AudoiCallback {
		public abstract void onOutputBytesAvailable(byte[] outputBytes);
	}
}

