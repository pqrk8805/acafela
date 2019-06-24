package com.acafela.harmony.codec.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_MIME_TYPE;
import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_QUEUE_TIMEOUT_US;

public class AudioCodecSync {
	private static final String TAG = AudioCodecSync.class.getName();

	private MediaCodec mCodec;
	private boolean mIsEncoder;

	public AudioCodecSync(boolean isEncoder) {
		mIsEncoder = isEncoder;
	}

	public void start(MediaFormat format) {
		try {
			mCodec = mIsEncoder?
					MediaCodec.createEncoderByType(AUDIO_MIME_TYPE):
					MediaCodec.createDecoderByType(AUDIO_MIME_TYPE);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		mCodec.release();
	}

	public byte[] handle(byte[] input) {
		int inputBufferId = mCodec.dequeueInputBuffer(AUDIO_QUEUE_TIMEOUT_US);
		if (inputBufferId >= 0) {
			ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferId);
			inputBuffer.clear();
			inputBuffer.put(input);
			mCodec.queueInputBuffer(
					inputBufferId,
					0,
					input.length,
					0,
					0);
		}

		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferId = mCodec.dequeueOutputBuffer(bufferInfo, AUDIO_QUEUE_TIMEOUT_US);

		if (outputBufferId < 0) {
			Log.i(TAG, "outputBufferId: " + outputBufferId);
			return null;
		}
		ByteBuffer outputBuffer = mCodec.getOutputBuffer(outputBufferId);
		byte[] outData = new byte[bufferInfo.size];
		outputBuffer.get(outData, 0, bufferInfo.size);
		mCodec.releaseOutputBuffer(outputBufferId, false);
		return outData;
	}
}

