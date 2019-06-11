package com.acafela.harmony.activity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.acafela.harmony.R;
import com.acafela.harmony.codec.AudioCodecSync;

public class TestEncodingActivity extends AppCompatActivity {
    private static final String TAG = TestEncodingActivity.class.getName();

    private static final int SPINNER_SPEAKER = 0;
    private static final int SPINNER_EARPIECE = 1;
    private static final int SPINNER_BLUETOOTH = 2;

    Button mStartButton;
    Button mStopButton;
    private AudioManager mAudioManager;
    AudioThread mAudioThread = new AudioThread();

    private AcousticEchoCanceler mAudioEchoCanceler;
    private int mAudioRecordSessionId;

    AudioCodecSync mAudioCodec = new AudioCodecSync();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_encoding);

        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (AcousticEchoCanceler.isAvailable())
            Log.i("Audio", "AEC enabled status");
        else
            Log.i("Audio", "AEC not enabled status");

        Spinner spinner = (Spinner) findViewById(R.id.audiopath_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.audioPath_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case SPINNER_SPEAKER:
                    default:
                        Toast.makeText(getApplicationContext(), "Speaker is Selected", Toast.LENGTH_SHORT).show();
                        mAudioManager.setBluetoothScoOn(false);
                        mAudioManager.stopBluetoothSco();
                        mAudioManager.setSpeakerphoneOn(true);
                        break;

                    case SPINNER_EARPIECE:
                        Toast.makeText(getApplicationContext(), "EarPiece is Selected", Toast.LENGTH_SHORT).show();
                        mAudioManager.setBluetoothScoOn(false);
                        mAudioManager.stopBluetoothSco();
                        mAudioManager.setSpeakerphoneOn(false);
                        break;

                    case SPINNER_BLUETOOTH:
                        Toast.makeText(getApplicationContext(), "Bluetooth is Selected", Toast.LENGTH_SHORT).show();
                        mAudioManager.setSpeakerphoneOn(false);
                        mAudioManager.setBluetoothScoOn(true);
                        mAudioManager.startBluetoothSco();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(SPINNER_SPEAKER);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAudioThread.interrupt();
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "Interrupted waiting for audio thread to finish");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAudioCodec.release();
    }

    public void onClickStartBtn(View v) {
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        mAudioThread = new AudioThread();
        mAudioThread.start();
        mAudioCodec.start();
    }

    public void onClickStopBtn(View v) {
        mAudioThread.interrupt();
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "Interrupted waiting for audio thread to finish");
        }
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
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

            mAudioRecordSessionId = recorder.getAudioSessionId();

            if (AcousticEchoCanceler.isAvailable()) {
                mAudioEchoCanceler = AcousticEchoCanceler.create(mAudioRecordSessionId);
                Log.i(TAG, "audio echo canceler enable");

                Log.i(TAG, "AEC is " + (mAudioEchoCanceler.getEnabled()?"enabled":"disabled"));

                if ( !mAudioEchoCanceler.getEnabled() )
                {
                    mAudioEchoCanceler.setEnabled(true);
                    Log.i(TAG, "AEC is " + (mAudioEchoCanceler.getEnabled()?"enabled":"disabled" +" after trying to disable"));
                }
            }

            AudioTrack track = new AudioTrack(AudioManager.STREAM_SYSTEM,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize,
                    AudioTrack.MODE_STREAM);

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

                    byte[] encodedBuf = mAudioCodec.encode(inBuf);
                    if (encodedBuf == null) {
                        continue;
                    }

                    byte[] decodedBuf = mAudioCodec.decode(encodedBuf);
                    if (decodedBuf == null) {
                        continue;
                    }

                    track.write(decodedBuf, 0, decodedBuf.length);
                }
            } finally {
                recorder.stop();
                recorder.release();
                track.stop();
                track.release();
                mAudioCodec.stop();
            }
        }
    }
}
