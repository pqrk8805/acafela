package com.acafela.harmony.ui;

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
import com.acafela.harmony.codec.audio.AudioCodecAsync.AudoiCallback;
import com.acafela.harmony.codec.audio.AudioControllerAsync;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_CHANNEL_COUNT;
import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_SAMPLE_RATE;
import static com.acafela.harmony.codec.audio.AudioMediaFormat.RAW_BUFFER_SIZE;

public class TestAsyncEncodingActivity extends AppCompatActivity {
    private static final String TAG = TestAsyncEncodingActivity.class.getName();

    private static final int SPINNER_SPEAKER = 0;
    private static final int SPINNER_EARPIECE = 1;
    private static final int SPINNER_BLUETOOTH = 2;

    Button mStartButton;
    Button mStopButton;
    private AudioManager mAudioManager;
    AudioThread mAudioThread = new AudioThread();

    private AcousticEchoCanceler mAudioEchoCanceler;
    private int mAudioRecordSessionId;

    AudioControllerAsync mAudioControllerAsync;
    AudioTrack mTrack;
    int mMinBufSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_encoding);

        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

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

        mAudioControllerAsync = new AudioControllerAsync();
        mAudioControllerAsync.setEncoderCallback(new AudoiCallback() {
            @Override
            public void onOutputBytesAvailable(byte[] outputBytes) {
                mAudioControllerAsync.enqueueEncodedBytes(outputBytes);
            }
        });

        mAudioControllerAsync.setDecoderCallback(new AudoiCallback() {
            @Override
            public void onOutputBytesAvailable(byte[] outputBytes) {
                mTrack.write(outputBytes, 0, outputBytes.length);
            }
        });

        mMinBufSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL_COUNT == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        mTrack = new AudioTrack(AudioManager.STREAM_SYSTEM,
                AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL_COUNT == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mMinBufSize,
                AudioTrack.MODE_STREAM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTrack.release();
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

    public void onClickStartBtn(View v) {
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        mAudioThread = new AudioThread();
        mAudioThread.start();
        mAudioControllerAsync.start();
        mTrack.play();
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
        mAudioControllerAsync.stop();
        mTrack.stop();
    }

    private class AudioThread extends Thread {

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

            // initialize audio recorder
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    AUDIO_SAMPLE_RATE,
                    AUDIO_CHANNEL_COUNT == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    mMinBufSize);

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

            recorder.startRecording();

            byte[] inBuf = new byte[RAW_BUFFER_SIZE];

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

                    mAudioControllerAsync.enqueueRawBytes(inBuf);
                }
            } finally {
                recorder.stop();
                recorder.release();
                mAudioEchoCanceler.release();
            }
        }
    }
}
