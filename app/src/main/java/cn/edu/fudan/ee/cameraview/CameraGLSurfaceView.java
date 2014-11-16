package cn.edu.fudan.ee.cameraview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.edu.fudan.ee.glasscamera.CameraParams;

public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "Google Glass";

    Context mContext;
    SurfaceTexture mSurface;
    int[] mTextureID = new int[1];
    DirectDrawer mDirectDrawer;
    Camera mCamera;
    Camera.Parameters params;
    public static Handler myHandler;
    CameraParams receiveParams;// 用于在handler中保存接收的相机参数
    FileOperation fileOperation = FileOperation.getInstance();// SharedPreferences操作类
    // 各种相机效果
    private String[] effect_WhiteBalance = new String[]{params.WHITE_BALANCE_AUTO, params.WHITE_BALANCE_DAYLIGHT, params.WHITE_BALANCE_CLOUDY_DAYLIGHT,
            "tungsten", params.WHITE_BALANCE_FLUORESCENT, params.WHITE_BALANCE_INCANDESCENT, "horizon", "sunset",params.WHITE_BALANCE_SHADE,
            params.WHITE_BALANCE_TWILIGHT, params.WHITE_BALANCE_WARM_FLUORESCENT};
    private String[] effect_AntiBanding = new String[]{params.ANTIBANDING_AUTO, params.ANTIBANDING_50HZ, params.ANTIBANDING_60HZ,
            params.ANTIBANDING_OFF};
    public CameraGLSurfaceView(Context context) {
        super(context);

        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
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

                fileOperation.myParams = receiveParams;

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
                        Log.i("send params1 from handler to server", ""+receiveParams.params1);
                        Log.i("send params2 from handler  to server", ""+receiveParams.params2);
                        Log.i("send params3 from handler  to server", ""+receiveParams.params3);
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
                try {
                    fileOperation.saveMyParams(receiveParams);
                }
                catch(Throwable e)
                {
                    e.printStackTrace();
                }
            }
        };
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
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        Log.i(TAG, "onSurfaceCreated...");
        createTextureID();
        mSurface = new SurfaceTexture(mTextureID[0]);
        mSurface.setOnFrameAvailableListener(this);
        mDirectDrawer = new DirectDrawer(mTextureID[0]);

        mCamera = Camera.open();
        params = mCamera.getParameters();
        params.setPreviewFpsRange(30000, 30000);
        params.setPreviewSize(640, 360);

        // 初始设置相机显示效果参数
        applyEffect(0, fileOperation.myParams.params1);
        applyEffect(1, fileOperation.myParams.params2);
        applyEffect(2, fileOperation.myParams.params3);

        mCamera.setParameters(params);
        try {
            mCamera.setPreviewTexture(mSurface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();


    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        Log.i(TAG, "onSurfaceChanged...");
        GLES20.glViewport(0, 0, width, height);



    }
    @Override
    public void onDrawFrame(GL10 gl) {

//		Log.i(TAG, "onDrawFrame...");
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurface.updateTexImage();
        mDirectDrawer.draw();
    }

    @Override
    public void onPause() {

        super.onPause();
        //	CameraInterface.getInstance().doStopCamera();
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
    private void createTextureID()
    {
        GLES20.glGenTextures(1, mTextureID, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
    }
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

//		Log.i(TAG, "onFrameAvailable...");
        requestRender();
    }

}