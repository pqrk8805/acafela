package com.acafela.harmony.codec.video;

import android.media.MediaFormat;
import android.media.MediaCodecInfo;
import android.util.Log;

public class VideoMediaFormat {
    private static final String TAG = VideoMediaFormat.class.getName();

    private static VideoMediaFormat INSTANCE;

    public static final String VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_VP8;
    public static final int VIDEO_FRAME_RATE = 20;               // 30fps
    public static final int VIDEO_IFRAME_INTERVAL = 1;  // TRADE-OFF
    public static final int VIDEO_BIT_RATE_LOW = 30000;
    public static final int VIDEO_BIT_RATE_HIGH = 50000;
    public static final int VIDEO_WIDTH_HIGHQ = 320;
    public static final int VIDEO_WIDTH_LOWQ = 320;
    public static final int VIDEO_HEIGHT_HIGHQ = 240;
    public static final int VIDEO_HEIGHT_LOWQ = 240;
    public static final int VIDEO_QUEUE_BOUND = 100;

    private int mWidth;
    private int mHeight;
    private int mBitRate;

    public synchronized static VideoMediaFormat getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VideoMediaFormat();
        }
        return INSTANCE;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHighQuality(Boolean highQuality) {
        if (highQuality) {
            Log.i(TAG, "Set HIGH Quality Video Codec!!!");
            mWidth = VIDEO_WIDTH_HIGHQ;
            mHeight = VIDEO_HEIGHT_HIGHQ;
            mBitRate = VIDEO_BIT_RATE_HIGH;
        }
        else {
            Log.i(TAG, "Set LOW Quality Video Codec!!!");
            mWidth = VIDEO_WIDTH_LOWQ;
            mHeight = VIDEO_HEIGHT_LOWQ;
            mBitRate = VIDEO_BIT_RATE_LOW;
        }
    }

    public MediaFormat getMediaFormat(Boolean useSerface) {
        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE,
                mWidth,
                mHeight);

        if (useSerface) {
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }
        else {
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        }
        format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFRAME_INTERVAL);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);

        return format;
    }


}
