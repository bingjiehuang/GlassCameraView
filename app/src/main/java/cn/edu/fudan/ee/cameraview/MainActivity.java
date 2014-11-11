package cn.edu.fudan.ee.cameraview;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by zxtxin on 2014/9/2.
 */
public class MainActivity extends Activity {
    private CameraGLSurfaceView glSurfaceView;
    private SocketService mBoundService;
    private GestureDetector mGestureDetector;// 手势检测器
    private int initialParams1;// 手指触摸触摸屏时的初始相机zoom倍数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new CameraGLSurfaceView(this);
        setContentView(glSurfaceView);
        glSurfaceView.setKeepScreenOn(true);

        // 手势检测
        mGestureDetector = createGestureDetector(MainActivity.this);

        Intent intent = new Intent(MainActivity.this, SocketService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    private ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBoundService = ((SocketService.LocalBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundService = null;
        }
    };

    // 实例化手势检测器，并为相应手势做出处理
    // onFling()是甩，这个甩的动作是在一个MotionEvent.ACTION_UP(手指抬起)发生时执行，
    // onScroll()只要手指移动就会执行,不会执行MotionEvent.ACTION_UP。
    // onFling通常用来实现翻页效果，onScroll通常用来实现放大缩小和移动。
    private GestureDetector createGestureDetector(Context context)
    {
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                // 按下
                Log.i("Gesture", "onDown");
                initialParams1 = CameraGLSurfaceView.myParams.params1;
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {
                // down事件发生而move或者up还没发生前触发该事件
                Log.i("Gesture", "onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                // 手指离开触摸屏
                Log.i("Gesture", "onSingleTapUp");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                // 手指在触摸屏上滑动
                Log.i("Gesture", "onScroll");
                float x = motionEvent.getX();
                float x2 = motionEvent2.getX();
                Log.i("motionEvent.getX()", ""+x);
                Log.i("motionEvent2.getX()", ""+x2);
                if(x2 >= x)
                {
                    CameraGLSurfaceView.myParams.params1 = initialParams1+(int)((60.0f-initialParams1+1.0f)/1366*(x2-x));
                    Log.i("放大倍数", ""+CameraGLSurfaceView.myParams.params1);
                }
                else
                {
                    CameraGLSurfaceView.myParams.params1 = initialParams1-(int)((initialParams1-0.0f+1.0f)/1366*(x-x2));
                    Log.i("缩小倍数", ""+CameraGLSurfaceView.myParams.params1);
                }
                //两种方式都可以
//                mPreview.params.setZoom(CameraPreview.myParams.params1);
//                mPreview.mCamera.setParameters(mPreview.params);
                Message msg = new Message();
                msg.obj = CameraGLSurfaceView.myParams;
                CameraGLSurfaceView.myHandler.sendMessage(msg);

                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                // 手指按下一段时间，并且没有松开
                Log.i("Gesture", "onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                // 手指在触摸屏上迅速移动，并松开的动作
                Log.i("Gesture", "onFling");
                return false;
            }
        });
        return gestureDetector;
    };

    // 将事件发送到手势检测器
    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if(mGestureDetector != null)
        {
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }
}
