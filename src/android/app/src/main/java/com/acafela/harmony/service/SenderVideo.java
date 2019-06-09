package com.acafela.harmony.service;


import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SenderVideo implements DataSender,Camera.PreviewCallback {
    private static final String LOG_TAG = "SenderVideo";
    private static final int MAX_VIDEO_FRAME_SIZE =640*480*4;
    private DatagramSocket SendUdpSocket;
    private InetAddress RemoteIp;                   // Address to call
    private boolean IsRunning = false;
    @SuppressWarnings("FieldCanBeLocal")
    private SurfaceTexture mtexture;
    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private int frame;

    private  InetAddress mIpAddress;
    private int mPort;

    public boolean setSession(String ip,int port)
    {
        try {
            this.mIpAddress = InetAddress.getByName(ip);
            this.mPort = port;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception Answer Messagwe: " + e);
            return false;
        }
        return true;
    }

    public boolean startSender()
    {
        if (IsRunning) return (false);

        OpenCamera();
        IsRunning = true;
        return true;
    }
    public boolean endSender()
    {
        if (!IsRunning) return (true);
        Log.i(LOG_TAG, "Ending Viop Audio");
        IsRunning = false;
        CloseCamera();
        return (false);
    }

    private void OpenCamera()  {
        if (mCamera!=null) return;
        try {
            SendUdpSocket = new DatagramSocket();
        }
        catch (SocketException e){

            Log.e(LOG_TAG, "SocketException: " + e.toString());
        }
        frame=0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    mCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        mtexture = new SurfaceTexture(10);
        try {
            mCamera.setPreviewTexture(mtexture);
        } catch (IOException e1) {
            Log.e(LOG_TAG, e1.getMessage());
        }

        Camera.Parameters params = mCamera.getParameters();

        params.setPreviewSize(480, 640);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        mCamera.setPreviewCallbackWithBuffer(this);

        mCamera.addCallbackBuffer(new byte[MAX_VIDEO_FRAME_SIZE]);
        mCamera.addCallbackBuffer(new byte[MAX_VIDEO_FRAME_SIZE]);
        mCamera.addCallbackBuffer(new byte[MAX_VIDEO_FRAME_SIZE]);
        mCamera.startPreview();
    }
    private void CloseCamera()  {
        if (mCamera==null) return;
        mCamera.stopPreview();
        mCamera.setPreviewCallbackWithBuffer(null);
        mCamera.release();
        mCamera=null;
        SendUdpSocket.disconnect();
        SendUdpSocket.close();
        SendUdpSocket=null;
    }
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        frame++;
        if ((frame%2)!=0) {//only process every other frame;
            camera.addCallbackBuffer(data);
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        int format = parameters.getPreviewFormat();
        //YUV formats require more conversion
        if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {

            int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;

            // Get the YuV image
            YuvImage yuv_image = new YuvImage(data, format, w, h, null);

            // Convert YuV to Jpeg
            Rect rect = new Rect(0, 0, w, h);
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            yuv_image.compressToJpeg(rect, 40, output_stream);
            byte[] bytes = output_stream.toByteArray();
            UdpSend(bytes);
        }
        camera.addCallbackBuffer(data);
    }
    private void UdpSend(final byte[] bytes) {
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mIpAddress, mPort);
                    SendUdpSocket.send(packet);
                } catch (SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in UdpSend: " + e);
                } catch (IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in UdpSend: " + e);
                }
            }
        });
        replyThread.start();
    }

}

