package com.acafela.harmony.codec;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.acafela.harmony.codec.AudioCodecSync.MIME_TYPE;
import static com.acafela.harmony.codec.AudioCodecSync.QUEUE_TIMEOUT_US;

public class AudioEncoderSync {
	private static final String TAG = AudioEncoderSync.class.getName();

	private MediaCodec mEncoder;

	public AudioEncoderSync() {
		try {
			mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(MediaFormat format) {
		mEncoder.configure(
				format,
				null /* surface */,
				null /* crypto */,
				MediaCodec.CONFIGURE_FLAG_ENCODE);

		mEncoder.start();
	}

	public void stop() {
		mEncoder.stop();
	}

	public void release() {
		mEncoder.release();
	}

	public byte[] encode(byte[] input) {
		int inputBufferId = mEncoder.dequeueInputBuffer(QUEUE_TIMEOUT_US);
		if (inputBufferId >= 0) {
			ByteBuffer inputBuffer = mEncoder.getInputBuffer(inputBufferId);
			inputBuffer.clear();
			inputBuffer.put(input);
			mEncoder.queueInputBuffer(
					inputBufferId,
					0,
					input.length,
					0,
					0);
		}

		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferId = mEncoder.dequeueOutputBuffer(bufferInfo, QUEUE_TIMEOUT_US);

		if (outputBufferId < 0) {
			Log.i(TAG, "outputBufferId: " + outputBufferId);
			return null;
		}
		ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferId);
		byte[] outData = new byte[bufferInfo.size];
		outputBuffer.get(outData, 0, 13);
		mEncoder.releaseOutputBuffer(outputBufferId, false);
		return outData;
	}
}

