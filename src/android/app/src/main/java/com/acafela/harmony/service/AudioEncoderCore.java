package com.acafela.harmony.service;


import android.media.AudioRecord;
import android.media.AudioFormat;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class AudioEncoderCore {
    private static final String TAG = AudioEncoderCore.class.getName();
    private static final boolean VERBOSE = false;
	private static final byte NULL = 0;

	private final int DEQUEUE_TIMEOUT = 20;

    // TODO: these ought to be configurable as well
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;    // H.264 Advanced Video Coding
    private static final int KEY_SAMPLE_RATE  = 8000;               // 30fps
    private static final int KEY_CHANNEL_COUNT = 1;           // 5 seconds between I-frames

    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;

	private MediaFormat mOutputFormat;

//	AudioRecord mRecorder;
//  private AudioThread mAudioThread = null;

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

        // 일단 sync 로 구현해달라는 요청이 있음.. 그래서 sync 로 변경하고, async 는 잠시 묻어 둠.
		/*
		mEncoder.setCallback(new MediaCodec.Callback() {
		  @Override
		  public final void onInputBufferAvailable(MediaCodec codec, int index) {
			ByteBuffer inputBuffer = mEncoder.getInputBuffer(index);
			// fill inputBuffer with valid data

			//

			// mEncoder.queueInputBuffer(inputBufferId, ��);
		  }

		  @Override
		  public final void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
		    ByteBuffer outputBuffer = mEncoder.getOutputBuffer(index);
		    MediaFormat bufferFormat = mEncoder.getOutputFormat(index); // option A
		    // bufferFormat is equivalent to mOutputFormat
		    // outputBuffer is ready to be processed or rendered.
			//
			
		    //mEncoder.releaseOutputBuffer(outputBufferId, ��);
		  }

		  @Override
		  public final void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
		    // Subsequent data will conform to new format.
		    // Can ignore if using getOutputFormat(outputBufferId)
		    // mOutputFormat = format; // option B
		  }

		  @Override
		  public final void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
		    //
		  }

		 });
		*/
		
        mEncoder.configure(
                format,
                null /* surface */,
                null /* crypto */,
                MediaCodec.CONFIGURE_FLAG_ENCODE);

/*
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, KEY_SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				AudioRecord.getMinBufferSize(KEY_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
*/

		mOutputFormat = mEncoder.getOutputFormat();

        mEncoder.start();
    }


	public void queueInputBuffer(byte[] input) {
		int nIndex = 0;

		nIndex = mEncoder.dequeueInputBuffer(DEQUEUE_TIMEOUT);
		if (nIndex >= 0) {
			ByteBuffer inputBuffer = mEncoder.getInputBuffer(nIndex);
			inputBuffer.clear();
			inputBuffer.put(input);
			mEncoder.queueInputBuffer(nIndex, 0, input.length, 0, 0);
		}
	}

	public byte[] dequeueOutputBuffer() {
		int nIndex = 0;
		byte[] output = new byte[10240000];
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

		nIndex = mEncoder.dequeueOutputBuffer(info, DEQUEUE_TIMEOUT);

		if (nIndex >= 0) {
			ByteBuffer outputBuffer = mEncoder.getOutputBuffer(nIndex);
			MediaFormat bufferFormat = mEncoder.getOutputFormat(nIndex);
			if (outputBuffer != null) {
				outputBuffer.get(output);
			}
		}
		else {
			Log.e(TAG, "Check the encoder output buffer :" + nIndex);
			return null;
		}

		//mEncoder.releaseOutputBuffer(nIndex, true);

		return output;
	}

	public void startEncoding() {

    	/*
		// create and execute audio capturing thread using internal mic
		if (mAudioThread == null) {
	        mAudioThread = new AudioThread();
			mAudioThread.start();
		}
		*/
	}


	public void release() {

		mEncoder.stop();
		mEncoder.release();
//		mRecorder.stop();
//		mRecorder.release();

	}

}

