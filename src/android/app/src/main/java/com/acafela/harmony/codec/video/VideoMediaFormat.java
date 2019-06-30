package com.acafela.harmony.codec.video;

import android.media.MediaFormat;
import android.media.MediaCodecInfo;

public class VideoMediaFormat {
    private static final String TAG = VideoMediaFormat.class.getName();

    public static final String VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_VP8;
    public static final int VIDEO_FRAME_RATE = 20;               // 30fps
    public static final int VIDEO_IFRAME_INTERVAL = 1;  // TRADE-OFF
    public static final int VIDEO_BIT_RATE = 100000;
    public static final int VIDEO_WIDTH = 320;
    public static final int VIDEO_HEIGHT = 240;
    public static final int VIDEO_WIDTH_P2P = 640;
    public static final int VIDEO_HEIGHT_P2P = 480;

    public static final int VIDEO_QUEUE_BOUND = 100;

    private MediaFormat mFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE,
            VIDEO_WIDTH,
            VIDEO_HEIGHT);

    public VideoMediaFormat(Boolean useSerface) {
        if (useSerface) {
            mFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }
        else {
            mFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        }
        mFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        mFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFRAME_INTERVAL);
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BIT_RATE);
    }

    public MediaFormat getMediaFormat() {
        return mFormat;
    }


}
