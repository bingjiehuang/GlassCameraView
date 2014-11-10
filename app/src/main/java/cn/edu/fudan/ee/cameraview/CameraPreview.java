package cn.edu.fudan.ee.cameraview;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.edu.fudan.ee.glasscamera.CameraParams;
import java.io.File;


/**
 * Created by zxtxin on 2014/9/2.
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    static Handler myHandler;
    Camera mCamera;
    private Camera.Parameters params;
    static CameraParams myParams;// 用于初始化相机参数、接收服务端socket通信发送的相机参数、用户改变相机参数时保存参数
    static CameraParams receiveParams;// 用于在handler中保存接收的相机参数
    String filePath = Environment.getExternalStorageDirectory().getPath()+"/savedInitialParams.ser";// 保存相机参数的文件
    FileOperation fileOperation = new FileOperation();// 文件操作类
    // 各种相机效果
    private String[] effect_WhiteBalance = new String[]{params.WHITE_BALANCE_AUTO, params.WHITE_BALANCE_DAYLIGHT, params.WHITE_BALANCE_CLOUDY_DAYLIGHT,
            "tungsten", params.WHITE_BALANCE_FLUORESCENT, params.WHITE_BALANCE_INCANDESCENT, "horizon", "sunset",params.WHITE_BALANCE_SHADE,
            params.WHITE_BALANCE_TWILIGHT, params.WHITE_BALANCE_WARM_FLUORESCENT};
    private String[] effect_AntiBanding = new String[]{params.ANTIBANDING_AUTO, params.ANTIBANDING_50HZ, params.ANTIBANDING_60HZ,
            params.ANTIBANDING_OFF};

    public CameraPreview(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // 处理接收的消息
                super.handleMessage(msg);
                receiveParams = (CameraParams)msg.obj;
                Log.i("receive Parameters","received");

                // 相机参数修改
                // applyEffect(int type_effect, int choose)
                // type_effect:
                // 0 -> Zoom, choose -> [0:1:60]
                // 1 -> WhiteBalance, choose -> [0, 10]
                // 2 -> ExposureCompensation, choose -> [-30:0.1:30]
                // 3 -> Antibanding, choose -> [0, 3]
                applyEffect(0, receiveParams.params1);// Zoom
                applyEffect(1, receiveParams.params2);// WhiteBalance
                applyEffect(2, receiveParams.params3);// ExposureCompensation

                // 相机参数修改生效
                mCamera.setParameters(params);
                Log.i("Change camera parameters","changed");
                Log.i("Handler中AutoExposureLock的状态",""+params.getAutoExposureLock());

                // 相机参数改变，如果glass与server已经建立了socket连接，则告诉服务端让其自动改变界面控件的值；
                // 如果未建立连接，仅仅是单独使用glass，则不需通知server
                // 此动作具体实现是在SocketService.java中执行
                if(SocketService.socket != null)
                {
                    try
                    {
                        SocketService.objOut.writeObject(receiveParams);
                        Log.i("writeObject","OK ");
                        Log.i("send params1 to server", ""+receiveParams.params1);
                        Log.i("send params2 to server", ""+receiveParams.params2);
                        Log.i("send params3 to server", ""+receiveParams.params3);
                        SocketService.objOut.reset();// writeObject后，一定要reset()
                    }
                    catch (ClassCastException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                // 每次接收到相机参数，立即保存相机参数到glass的内存中
                fileOperation.saveParamsToFile(filePath, receiveParams);
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
        params.setAutoWhiteBalanceLock(true);
        params.setAutoExposureLock(false);
        params.setPreviewFpsRange(30000, 30000);
        params.setPreviewSize(640, 360);


        // 启动相机后加载内存保存的相机初始参数
        myParams = fileOperation.createOrLoadParamsFromFile(filePath);
        // 初始设置相机显示效果参数
        applyEffect(0, myParams.params1);
        applyEffect(1, myParams.params2);
        applyEffect(2, myParams.params3);

        // 获取相机支持的参数
        Log.i("isAutoExposureLockSupported", ""+params.isAutoExposureLockSupported());
        Log.i("Supported Antibanding", params.getSupportedAntibanding().toString());
        Log.i("Antibanding", params.getAntibanding());
        Log.i("Color Effect",params.getColorEffect());
        Log.i("Exposure Compensation",""+params.getExposureCompensation());
        Log.i("Min Exposure Compensation Step",""+params.getMinExposureCompensation());
        Log.i("Max Exposure Compensation",""+params.getMaxExposureCompensation());
        Log.i("Exposure Compensation Step",""+params.getExposureCompensationStep());
        Log.i("Flash Mode",params.getFlashMode());
        Log.i("Supported Flash Mode",params.getSupportedFlashModes().toString());
        Log.i("Scene Mode",params.getSceneMode());
        Log.i("Supported Scene Mode",params.getSupportedSceneModes().toString());
        Log.i("Zoom Supported",""+params.isZoomSupported());
        Log.i("Max Zoom",""+params.getMaxZoom());
        Log.i("Zoom",""+ params.getZoom());
        Log.i("SupportedWhiteBalance",""+params.getSupportedWhiteBalance());
        Log.i("WhiteBalance",""+params.getWhiteBalance());

        mCamera.setParameters(params);
        Log.i("surfaceCreated后AutoExposureLock的状态",""+params.getAutoExposureLock());
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

    // 测试各种效果的参数
    // type_effect为效果类型，choose为选择该效果的哪种类型
    public void applyEffect(int type_effect, int choose)
    {
        switch (type_effect)
        {
            case 0:
                // Zoom
                Zoom(choose);
                break;
            case 1:
                // WhiteBalance
                WhiteBalance(choose);
                break;
            case 2:
                // ExposureCompensation
                ExposureCompensation(choose);
                break;
            case 3:
                // Antibanding
                Antibanding(choose);
                break;
            default:
                break;
        }
    }

    // Zoom
    public void Zoom(int choose)
    {
        Log.i("former Zoom",""+params.getZoom());
        params.setZoom(choose);
        Log.i("later Zoom",""+params.getZoom());
    }

    // WhiteBalance
    // The range of the choose is limited to [0, 10]
    public void WhiteBalance(int choose)
    {
        Log.i("former WhiteBalance",""+params.getWhiteBalance());
        params.setWhiteBalance(effect_WhiteBalance[choose]);
        Log.i("later WhiteBalance",""+params.getWhiteBalance());
    }

    // ExposureCompensation
    public void ExposureCompensation(int ExposureCompensationValue)
    {
        Log.i("former ExposureCompensation",""+params.getExposureCompensation());
        params.setExposureCompensation(ExposureCompensationValue);
        Log.i("later ExposureCompensation",""+params.getExposureCompensation());
    }

    // Antibanding
    // The range of the choose is limited to [0, 3]
    public void Antibanding(int choose)
    {
        Log.i("former Antibanding",""+params.getAntibanding());
        params.setAntibanding(effect_AntiBanding[choose]);
        Log.i("later Antibanding",""+params.getAntibanding());
    }
}