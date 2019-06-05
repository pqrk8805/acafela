package com.acafela.harmony;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

public class TestEncodingActivity extends AppCompatActivity {
    private static final String LOG_TAG = TestEncodingActivity.class.getName();

    Button mStartButton;
    Button mStopButton;
    AudioThread mAudioThread = new AudioThread();
    private boolean mIsStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_encoding);

        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);
    }

    public void onClickStartBtn(View v) {


        mIsStarted = true;

        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        mAudioThread = new AudioThread();
        mAudioThread.start();
    }

    public void onClickStopBtn(View v) {
        mAudioThread.interrupt();
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            Log.w(LOG_TAG, "Interrupted waiting for audio thread to finish");
        }
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);

        mIsStarted = false;
    }

    private class AudioThread extends Thread {
        // Sample rate must be one supported by Opus.
        static final int SAMPLE_RATE = 8000;

        // Number of samples per frame is not arbitrary,
        // it must match one of the predefined values, specified in the standard.
        static final int FRAME_SIZE = 160;

        // 1 or 2
        static final int NUM_CHANNELS = 1;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);


            int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);

            // initialize audio recorder
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize);


            // init audio track
            AudioTrack track = new AudioTrack(AudioManager.STREAM_SYSTEM,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize,
                    AudioTrack.MODE_STREAM);

            // start
            recorder.startRecording();
            track.play();

            byte[] inBuf = new byte[FRAME_SIZE * NUM_CHANNELS * 2];

            try {
                while (!Thread.interrupted()) {
                    // Encoder must be fed entire frames.
                    int to_read = inBuf.length;
                    int offset = 0;
                    while (to_read > 0) {
                        int read = recorder.read(inBuf, offset, to_read);
                        if (read < 0) {
                            throw new RuntimeException("recorder.read() returned error " + read);
                        }
                        to_read -= read;
                        offset += read;
                    }

                    track.write(inBuf, 0, inBuf.length);
                }
            } finally {
                recorder.stop();
                recorder.release();
                track.stop();
                track.release();
            }
        }
    }
}
