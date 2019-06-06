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

    // TODO: these ought to be configurable as well
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AMR_NB;    // H.264 Advanced Video Coding
    private static final int KEY_SAMPLE_RATE  = 8000;               // 30fps
    private static final int KEY_CHANNEL_COUNT = 1;           // 5 seconds between I-frames

    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;
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
/*
			@Override
			public final void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
				try {
					onOutputFormatChangedSafe(mediaCodec, mediaFormat);
				} catch (Exception exception) {
					handleException(exception);
				}
			}
*/

		  @Override
		  public final void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
		    //
		  }

		 });

		
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

        mEncoder.start();
 //       mRecorder.startRecording();

    }



	protected void startEncoding() {

    	/*
		// create and execute audio capturing thread using internal mic
		if (mAudioThread == null) {
	        mAudioThread = new AudioThread();
			mAudioThread.start();
		}
		*/
	}


	protected void release() {

		mEncoder.stop();
		mEncoder.release();
//		mRecorder.stop();
//		mRecorder.release();

	}

	/*
private class AudioThread extends Thread {
	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		try {
			final int min_buffer_size = AudioRecord.getMinBufferSize(
					KEY_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
			int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
			if (buffer_size < min_buffer_size)
				buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

			AudioRecord audioRecord = null;
			for (final int source : AUDIO_SOURCES) {
				try {
					audioRecord = new AudioRecord(
						source, KEY_SAMPLE_RATE,
						AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
					if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
						audioRecord = null;
				} catch (final Exception e) {
					audioRecord = null;
				}
				if (audioRecord != null) break;
			}
			if (audioRecord != null) {
				try {
					if (mIsCapturing) {
						Log.v(TAG, "AudioThread:start audio recording");
						final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
						int readBytes;
						audioRecord.startRecording();
						try {
							for (; mIsCapturing && !mRequestStop && !mIsEOS ;) {
								// read audio data from internal mic
								buf.clear();
								readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
								if (readBytes > 0) {
									// set audio data to encoder
									buf.position(readBytes);
									buf.flip();
									encode(buf, readBytes, getPTSUs());
									frameAvailableSoon();
								}
							}
							frameAvailableSoon();
						} finally {
							audioRecord.stop();
						}
					}
				} finally {
					audioRecord.release();
				}
			} else {
				Log.e(TAG, "failed to initialize AudioRecord");
			}
		} catch (final Exception e) {
			Log.e(TAG, "AudioThread#run", e);
		}
		//if (DEBUG) Log.v(TAG, "AudioThread:finished");
	}
}
*/

}

