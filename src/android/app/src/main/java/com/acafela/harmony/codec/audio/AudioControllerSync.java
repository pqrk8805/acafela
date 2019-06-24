package com.acafela.harmony.codec.audio;

public class AudioControllerSync {
    private static final String TAG = AudioControllerSync.class.getName();

    AudioMediaFormat mAudioMediaFormat = new AudioMediaFormat();
    AudioCodecSync mAudioEncoder = new AudioCodecSync(true);
    AudioCodecSync mAudioDecoder = new AudioCodecSync(false);

    public void start() {
        mAudioEncoder.start(mAudioMediaFormat.getAudioMediaFormat());
        mAudioDecoder.start(mAudioMediaFormat.getAudioMediaFormat());
    }

    public void stop() {
        mAudioEncoder.stop();
        mAudioDecoder.stop();
    }

    public byte[] encode(byte[] input) {
        return mAudioEncoder.handle(input);
    }

    public byte[] decode(byte[] input) {
        return mAudioDecoder.handle(input);
    }
}
