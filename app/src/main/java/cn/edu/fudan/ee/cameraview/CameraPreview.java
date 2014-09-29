package cn.edu.fudan.ee.cameraview;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by zxtxin on 2014/9/2.
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.Parameters params;
    public static Handler myHandler;
    public CameraPreview(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                CameraParams rawParams = (CameraParams)msg.obj;
                params.setRotation(rawParams.params1);
                mCamera.setParameters(params);
            }
        };
    }


    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
    	/* 启动Camera */
        mCamera = Camera.open();
        params = mCamera.getParameters();
        params.setPreviewFpsRange(30000, 30000);
        params.setPreviewSize(640, 360);
        Log.i("Antibanding", params.getAntibanding());
        Log.i("Color Effect",params.getColorEffect());
        Log.i("Exposure Compensation",""+params.getExposureCompensation());
        Log.i("Max Exposure Compensation",""+params.getExposureCompensation());
        Log.i("Exposure Compensation Step",""+params.getExposureCompensationStep());
        Log.i("Flash Mode",params.getFlashMode());
        Log.i("Supported Flash Mode",params.getSupportedFlashModes().toString());
        Log.i("Scene Mode",params.getSceneMode());
        Log.i("Supported Scene Mode",params.getSupportedSceneModes().toString());
        Log.i("Zoom Supported",""+params.isZoomSupported());
        Log.i("Max Zoom",""+params.getMaxZoom());
        params.setZoom(60);
        Log.i("Zoom",""+ params.getZoom());
        mCamera.setParameters(params);
        try
        {
            mCamera.setPreviewDisplay(arg0);
            mCamera.startPreview();
        }
        catch (IOException e)
        {
        	/* 释放mCamera */
            mCamera.release();
            mCamera = null;
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
    	/* 停止预览 */
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

    }

}