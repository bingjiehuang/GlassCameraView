package cn.edu.fudan.ee.cameraview;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * Created by zxtxin on 2014/9/17.
 */

public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public class LocalBinder extends Binder
    {
        SocketService getService()
        {
          return SocketService.this;
        }
    }

    private ServerSocket serverSocket = null;
    static Socket socket = null;
    final int SERVER_PORT = 22222;
    private ObjectInputStream objIn = null;// 用于socket通信
    static ObjectOutputStream objOut = null;// 用于socket通信
    CameraParams cameraParams = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    serverSocket = new ServerSocket(SERVER_PORT);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                while(true)
                {
                    try
                    {
                        socket = serverSocket.accept();
                        Log.d("SocketServer", "Accepted");

                        objIn = new ObjectInputStream(socket.getInputStream());
                        Log.i("objIn","initialed");
                        objOut = new ObjectOutputStream(socket.getOutputStream());
                        Log.i("objOut","initialed");

                        while (true)
                        {
                            try
                            {
                                // Input
                                Object obj = objIn.readObject();
                                cameraParams = (CameraParams)obj;
                                Log.i("readObject","OK ");
                                Log.i("receive myParams.1 from server","params1 : "+cameraParams.params1);
                                // 测试各种参数
                                Log.i("receive myParams.2 from server","params2 : "+cameraParams.params2);
                                Log.i("receive myParams.3 from server","params3 : "+cameraParams.params3);
                                Message msg = new Message();
                                msg.obj = obj;
                                CameraGLSurfaceView.myHandler.sendMessage(msg);
                                Log.i("Send received message from server  to handler","sent");

                                // Output在CameraPreview.java的handler中实现
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                Log.i("Wrong","objIn");
                                break;
                            }
                            catch (ClassNotFoundException e)
                            {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                        Log.i("Wrong","socket");
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
