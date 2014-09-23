package cn.edu.fudan.ee.cameraview;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;

import java.io.*;
import java.net.*;



/**
 * Created by zxtxin on 2014/9/17.
 */
public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class LocalBinder extends Binder {
        SocketService getService(){
            return SocketService.this;
        }
    }
    private ServerSocket serverSocket = null;
    final int SERVER_PORT = 1111;
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(SERVER_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    while(true) {
                        try {
                            Socket socket = serverSocket.accept();
                            while (true) {
                                try {
                                    ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                                    Camera.Parameters params = (Camera.Parameters)(objIn.readObject());

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    break;
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
