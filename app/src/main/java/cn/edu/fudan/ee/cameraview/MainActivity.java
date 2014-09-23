package cn.edu.fudan.ee.cameraview;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;
/**
 * Created by zxtxin on 2014/9/2.
 */
public class MainActivity extends Activity {
    private CameraPreview mPreview;
    private SocketService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreview = new CameraPreview(this);
        FrameLayout layout = new FrameLayout(this);
        layout.addView(mPreview);
        layout.addView(new View(this));
        layout.setKeepScreenOn(true);
        setContentView(layout);
        Intent intent = new Intent(MainActivity.this, SocketService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
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

}
