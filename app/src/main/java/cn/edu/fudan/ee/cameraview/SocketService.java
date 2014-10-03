package cn.edu.fudan.ee.cameraview;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.fudan.ee.glasscamera.CameraParams;

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
    final int SERVER_PORT = 22222;
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
                            Log.d("SocketServer", "Accepted");


                            ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                            Log.i("objIn","initialed");

                            while (true) {
                                try {



                                    Object obj = objIn.readObject();
                                    CameraParams test = (CameraParams)obj;
                                    Log.i("test.1",""+test.params1);
                                    Log.i("readObject","OK ");
                                    Message msg = new Message();
                                    msg.obj = obj;

                                    CameraPreview.myHandler.sendMessage(msg);
                                    Log.i("Message","sent");

                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                    Log.i("Wrong","objIn");
                                    break;
                                }
                                catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }catch(IOException e) {
                            e.printStackTrace();
                            Log.i("Wrong","socket");
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
