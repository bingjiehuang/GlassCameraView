package cn.edu.fudan.ee.cameraview;

import android.content.Context;
import android.hardware.Camera;
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
    public CameraPreview(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

		/*      List<Integer> supportedFormats = params.getSupportedPreviewFormats();
    	int i;
        for(i=0;i<supportedFormats.size();i++)
        {
        	Log.i("Supported Formats", supportedFormats.get(i).toString());
        }
   */
//        params.setPreviewFormat(ImageFormat.YV12);

        params.setAutoWhiteBalanceLock(false);
        Log.i("White Balance", params.getWhiteBalance());
//        params.setColorEffect(Camera.Parameters.EFFECT_AQUA);
        params.setPreviewFpsRange(30000, 30000);
        params.setPreviewSize(640,360);
//        params.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
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